package dev.efullstack.todo;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@SpringBootApplication
public class TodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
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