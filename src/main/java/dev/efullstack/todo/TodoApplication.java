package dev.efullstack.todo;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;

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
	private LocalDateTime createdAt;
}

interface UserRepository extends ListCrudRepository<User, Long> { }

@Data
@Table("tasks")
class Task {
	@Id
	private Long taskId;
	private Long userId;
	private String title;
	private String description;
	@Column("status")
	private TaskStatus status;
	private LocalDateTime dueDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Transient
	private User user;

	public enum TaskStatus {
		PENDING,
		IN_PROGRESS,
		COMPLETED
	}
}

interface TaskRepository extends ListCrudRepository<Task, Long> { }

@Data
@Table("tags")
class Tag {
	@Id
	private Long tagId;
	private String name;
}

interface TagRepository extends ListCrudRepository<Tag, Long> { }

@Data
@Table("task_tags")
class TaskTag {
	@Id
	private TaskTagId id;
	@Data
	public static class TaskTagId {
		private Long taskId;
		private Long tagId;
	}
}

interface TaskTagRepository extends ListCrudRepository<TaskTag, TaskTag.TaskTagId> { }