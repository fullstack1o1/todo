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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
						.GET("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.GET("/tasks", request -> ServerResponse.noContent().build())
						.PATCH("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.DELETE("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.POST("/tags", todoHandler::newTags)
						.GET("/tags/{tagId}", request -> ServerResponse.noContent().build())
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
}
interface TagRepository extends ListCrudRepository<Tag, Long> {
	List<Tag> findAllByUserId(Long userId);
}
interface TaskTagRepository extends ListCrudRepository<TaskTag, Long> {}

@ResponseStatus(reason = "Task not found", code = HttpStatus.NOT_FOUND)
class TaskNotFoundException extends RuntimeException {
	public TaskNotFoundException() {}
	public TaskNotFoundException(String message) {
		super(message);
	}
}

@Service
@RequiredArgsConstructor
class TaskService {
	final TaskRepository taskRepository;

	public Mono<Task> newTask(Long userId, Task task) {
		assert Objects.nonNull(task);
		task.setUserId(userId);
		return Mono.fromCallable(() -> taskRepository.save(task));
	}

	public Task findByTaskId(Long userId, Long taskId) {
		return taskRepository.findByUserIdAndTaskId(userId,taskId).orElseThrow(TaskNotFoundException::new);
	}
}

@Service
@RequiredArgsConstructor
class TagService {
	final TagRepository tagRepository;

	public List<Tag> findAll(Long userId) {
		return tagRepository.findAllByUserId(userId);
	}

	public Mono<Tag> newTag(Long userId, Tag tag) {
		tag.setUserId(userId);
		return Mono.fromCallable(() -> tagRepository.save(tag));
	}
}