package dev.efullstack.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Table("tasks")
public class Task {
    @Id
    private Long taskId;
    private Long userId;
    private String title;
    private String description;
    private Task.TaskStatus status = Task.TaskStatus.PENDING;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd")
    private LocalDate date;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "HH:mm:ss")
    private LocalTime time;
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
