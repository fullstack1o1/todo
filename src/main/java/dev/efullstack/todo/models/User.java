package dev.efullstack.todo.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("todo_users")
public class User {
    @Id
    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
}
