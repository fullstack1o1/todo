package dev.efullstack.todo.repositories;

import dev.efullstack.todo.models.User;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<User, Long> {
}
