package dev.efullstack.todo.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("tags")
public class Tag {
    @Id
    private Long id;
    private Long userId;
    private String name;
}
