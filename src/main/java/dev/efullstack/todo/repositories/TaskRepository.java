package dev.efullstack.todo.repositories;

import dev.efullstack.todo.models.Task;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends ListCrudRepository<Task, Long> {
    Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId);

    List<Task> findTasksByUserId(Long userId);
}
