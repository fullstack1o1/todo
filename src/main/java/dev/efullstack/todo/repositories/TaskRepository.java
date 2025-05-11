package dev.efullstack.todo.repositories;

import dev.efullstack.todo.models.Task;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends ListCrudRepository<Task, Long> {
    Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId);

    List<Task> findTasksByUserId(Long userId);

    List<Task> findTasksByUserIdAndDateIs(Long userId, LocalDate date);
    List<Task> findTasksByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Task> findTasksByUserIdAndDateIsLessThan(Long userId, LocalDate date);
}
