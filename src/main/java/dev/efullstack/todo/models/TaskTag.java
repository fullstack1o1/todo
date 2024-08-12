package dev.efullstack.todo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("task_tags")
@AllArgsConstructor
public class TaskTag {
    private Long taskId;
    private Long tagId;
}
