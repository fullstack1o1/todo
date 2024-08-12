package dev.efullstack.todo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;

@SpringBootApplication
@Slf4j
public class TodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(TodoHandler todoHandler) {
		return RouterFunctions
				.route()
				.before(request -> {
					log.info("REQUEST {} {}", request.method(), request.path());
					return request;
				})
				.path("/todo/{userId}", builder -> builder
						.POST("/tasks", todoHandler::createTask)
						.GET("/tasks/{taskId}", todoHandler::findTaskById)
						.GET("/tasks", todoHandler::tasks)
						.PATCH("/tasks/{taskId}", todoHandler::updateTask)
						.DELETE("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.POST("/tags", todoHandler::newTags)
						.GET("/tags/{tagId}", todoHandler::tagById)
						.GET("/tags/{tagId}/tasks", todoHandler::tasksByTagId)
						.PATCH("/tags/{tagId}", todoHandler::updateTag)
						.GET("/tags", request -> ServerResponse.noContent().build())
						.DELETE("/tags/{tagId}", request -> ServerResponse.noContent().build())
				)
				.after((request, response) -> {
					log.info("RESPONSE {} {} {}", request.method(), request.path(), response.statusCode());
					return response;
				})
				.build();
	}
}

@Component
@RequiredArgsConstructor
class TodoHandler {
	final TaskService taskService;
	final TagService tagService;
	final TaskTagService taskTagService;

	public Mono<ServerResponse> createTask(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		return request
				.bodyToMono(Task.class)
				.flatMap(task -> taskService.newTask(userId, task))
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> newTags(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		return request
				.bodyToMono(Tag.class)
				.flatMap(tag -> tagService.newTag(userId, tag))
				.flatMap(tag -> ServerResponse.ok().bodyValue(tag));
	}

	public Mono<ServerResponse> updateTask(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var taskId = Long.valueOf(request.pathVariable("taskId"));
		return request
				.bodyToMono(Task.class)
				.flatMap(task -> taskService.updateTask(userId, taskId, task))
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> tasks(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		return ServerResponse.ok().bodyValue(taskService.allTask(userId));
	}

	public Mono<ServerResponse> tagById(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var tagId = Long.valueOf(request.pathVariable("tagId"));
		return tagService.tagById(userId, tagId)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> updateTag(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var tagId = Long.valueOf(request.pathVariable("tagId"));
		return request
				.bodyToMono(Tag.class)
				.flatMap(tag -> tagService.updateTag(userId, tagId, tag))
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> findTaskById(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var taskId = Long.valueOf(request.pathVariable("taskId"));
		return taskService.findByTaskId(userId, taskId)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> tasksByTagId(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var tagId = Long.valueOf(request.pathVariable("tagId"));

		return taskTagService.findTagsByTagId(userId, tagId)
				.flatMap(ServerResponse.ok()::bodyValue);
	}
}


@Data
@Table("todo_users")
class User {
	@Id
	private Long userId;
	private String username;
	private String email;
	private String passwordHash;
}

@Data
@Table("tasks")
class Task {
	@Id
	private Long taskId;
	private Long userId;
	private String title;
	private String description;
	private TaskStatus status = TaskStatus.PENDING;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime dueDate;
	@JsonIgnore
	@ReadOnlyProperty
	private LocalDateTime createdAt;
	@JsonIgnore
	@ReadOnlyProperty
	private LocalDateTime updatedAt;

	@MappedCollection(idColumn = "task_id", keyColumn = "tag_id")
	private Set<TaskTag> tags = Set.of();

	public enum TaskStatus {
		PENDING,
		IN_PROGRESS,
		COMPLETED
	}
}

@Data
@Table("tags")
class Tag {
	@Id
	private Long id;
	private Long userId;
	private String name;
}

@Data
@Table("task_tags")
@AllArgsConstructor
class TaskTag {
	private Long taskId;
	private Long tagId;
}

interface UserRepository extends ListCrudRepository<User, Long> {}
interface TaskRepository extends ListCrudRepository<Task, Long> {
	Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId);
	List<Task> findTasksByUserId(Long userId);
}
interface TagRepository extends ListCrudRepository<Tag, Long> {
	List<Tag> findAllByUserId(Long userId);
	Optional<Tag> findByUserIdAndId(Long userId, Long tagId);
}
interface TaskTagRepository extends ListCrudRepository<TaskTag, Long> {
	List<TaskTag> findTaskTagByTagId(Long tagId);
}

@ResponseStatus(reason = "Task not found", code = HttpStatus.NOT_FOUND)
class TaskNotFoundException extends RuntimeException {
	public TaskNotFoundException() {}
	public TaskNotFoundException(String message) {
		super(message);
	}
}

@ResponseStatus(reason = "Tag not found", code = HttpStatus.NOT_FOUND)
class TagNotFoundException extends RuntimeException {
	public TagNotFoundException() {}
	public TagNotFoundException(String message) {
		super(message);
	}
}

@Service
@RequiredArgsConstructor
class TaskService {
	final TaskRepository taskRepository;

	public Mono<Task> newTask(Long userId, Task task) {
		assert nonNull(task);
		task.setUserId(userId);
		return Mono.fromCallable(() -> taskRepository.save(task));
	}

	public Mono<Task> findByTaskId(Long userId, Long taskId) {
		return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId,taskId).orElseThrow(TaskNotFoundException::new));
	}

	public Mono<Task> updateTask(Long userId, Long taskId, Task task) {
		return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId,taskId).orElseThrow(TaskNotFoundException::new))
				.zipWith(Mono.just(task), (dbTask, requestTask) -> {
					if (nonNull(requestTask.getTitle())) {
						dbTask.setTitle(requestTask.getTitle());
					}
					if (nonNull(requestTask.getDescription())) {
						dbTask.setDescription(requestTask.getDescription());
					}
					if (nonNull(requestTask.getStatus())) {
						dbTask.setStatus(requestTask.getStatus());
					}
					if (nonNull(requestTask.getDueDate())) {
						dbTask.setDueDate(requestTask.getDueDate());
					}
					if (nonNull(requestTask.getTags())) {
						dbTask.setTags(requestTask.getTags());
					}
					return dbTask;
				})
				.map(taskRepository::save);
	}

	public List<Task> allTask(Long userId) {
		return taskRepository.findTasksByUserId(userId);
	}
}

@Service
@RequiredArgsConstructor
class TagService {
	final TagRepository tagRepository;

	public Mono<Tag> newTag(Long userId, Tag tag) {
		tag.setUserId(userId);
		return Mono.fromCallable(() -> tagRepository.save(tag));
	}

	public Mono<Tag> tagById(Long userId, Long tagId) {
		return Mono.fromCallable(() -> tagRepository.findByUserIdAndId(userId,tagId).orElseThrow(TagNotFoundException::new));
	}

	public Mono<Tag> updateTag(Long userId, Long tagId, Tag tag) {
		return tagById(userId, tagId)
				.zipWith(Mono.just(tag),(dbTag, requestTag) -> {
					if (nonNull(requestTag.getName())) {
						dbTag.setName(requestTag.getName());
					}
					return dbTag;
				});
	}
}

@Service
@RequiredArgsConstructor
class TaskTagService {
	final TaskTagRepository taskTagRepository;
	final TaskService taskService;

	public Mono<List<Task>> findTagsByTagId(Long userId, Long tagId) {
		//collect the task ids
		//call taskService.findByTaskId for each task parallelly and collect the results
		return Mono.fromCallable(() -> taskTagRepository.findTaskTagByTagId(tagId)
				.stream().map(TaskTag::getTaskId)
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll))
				.flatMapMany(Flux::fromIterable)
				.cast(Long.class)
				.flatMap(taskId -> taskService.findByTaskId(userId, taskId))
				.collectList();
	}
}