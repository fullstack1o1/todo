package dev.efullstack.todo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;

@SpringBootApplication
public class TodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(TodoHandler todoHandler) {
		return RouterFunctions
				.route()
				.path("/todo/{userId}", builder -> builder
						.POST("/tasks", todoHandler::createTask)
						.GET("/tasks/{taskId}", todoHandler::taskByTaskId)
						.GET("/tasks", todoHandler::tasks)
						.PATCH("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.DELETE("/tasks/{taskId}", request -> ServerResponse.noContent().build())
						.GET("/tags/{tagId}", request -> ServerResponse.noContent().build())
						.GET("/tags", request -> ServerResponse.noContent().build())
						.DELETE("/tags/{tagId}", request -> ServerResponse.noContent().build())
				)
				.build();
	}
}

@Component
@RequiredArgsConstructor
class TodoHandler {
	final TaskService taskService;
	final TagService tagService;

	public Mono<ServerResponse> createTask(ServerRequest request) {
		var userId = request.pathVariable("userId");
		return request
				.bodyToMono(Task.class)
				.flatMap(taskService::newTask)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	public Mono<ServerResponse> taskByTaskId(ServerRequest request) {
		return null;
	}

	public Mono<ServerResponse> tasks(ServerRequest request) {
		return null;
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
	private TaskStatus status;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime dueDate;
	private LocalDateTime createdAt;
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
interface TaskRepository extends ListCrudRepository<Task, Long> {}
@Service
@RequiredArgsConstructor
class TaskService {
	final TaskRepository taskRepository;

	public Mono<Task> newTask(Task task) {
		return Mono.fromCallable(() -> taskRepository.save(task));
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

interface TagRepository extends ListCrudRepository<Tag, Long> {}

@Service
@RequiredArgsConstructor
class TagService {
	final TagRepository tagRepository;

}