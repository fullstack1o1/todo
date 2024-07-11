package dev.efullstack.todo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
						.GET("/tasks/{taskId}", todoHandler::taskByTaskId)
						.GET("/tasks", todoHandler::tasks)
						.PATCH("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.DELETE("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.GET("/tags/{tagId}", request -> ServerResponse.noContent().build())
						.GET("/tags", todoHandler::tags)
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

	public Mono<ServerResponse> taskByTaskId(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		var taskId = Long.valueOf(request.pathVariable("taskId"));
		return ServerResponse.ok().bodyValue(taskService.findByTaskId(userId, taskId));
	}

	public Mono<ServerResponse> tasks(ServerRequest request) {
		return ServerResponse.noContent().build();
	}

	public Mono<ServerResponse> tags(ServerRequest request) {
		var userId = Long.valueOf(request.pathVariable("userId"));
		return ServerResponse.ok().bodyValue(tagService.findAll(userId));
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
interface UserRepository extends ListCrudRepository<User, Long> {}

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

	//need to implement a method to get all tags for a task
	@MappedCollection(idColumn = "task_id")
	private Set<Tag> tags;

	public enum TaskStatus {
		PENDING,
		IN_PROGRESS,
		COMPLETED
	}
}

@ResponseStatus(reason = "Task not found", code = HttpStatus.NOT_FOUND)
class TaskNotFoundException extends RuntimeException {
	public TaskNotFoundException() {}
	public TaskNotFoundException(String message) {
		super(message);
	}
}

interface TaskRepository extends ListCrudRepository<Task, Long> {
	Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId);
}

@Service
@RequiredArgsConstructor
class TaskService {
	final TaskRepository taskRepository;

	public Mono<Task> newTask(Long userId, Task task) {
		assert Objects.nonNull(task);
		task.setUserId(userId);
		if(Objects.nonNull(task.getTags()) && !task.getTags().isEmpty()) {
			task.getTags().forEach(tag -> tag.setUserId(userId));
		}
		return Mono.fromCallable(() -> taskRepository.save(task));
	}

	public Task findByTaskId(Long userId, Long taskId) {
		return taskRepository.findByUserIdAndTaskId(userId,taskId).orElseThrow( () -> new TaskNotFoundException());
	}
}

@Data
@Table("tags")
class Tag {
	@Id
	private Long id;
	private Long userId;
	private Long taskId;
	private String name;
}

interface TagRepository extends ListCrudRepository<Tag, Long> {
	List<Tag> findAllByUserId(Long userId);
}

@Service
@RequiredArgsConstructor
class TagService {
	final TagRepository tagRepository;

	public List<Tag> findAll(Long userId) {
		return tagRepository.findAllByUserId(userId);
	}
}