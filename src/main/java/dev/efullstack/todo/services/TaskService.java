package dev.efullstack.todo.services;

import dev.efullstack.todo.TaskNotFoundException;
import dev.efullstack.todo.models.Task;
import dev.efullstack.todo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class TaskService {
    final TaskRepository taskRepository;

    public Mono<Task> newTask(Long userId, Task task) {
        assert nonNull(task);
        task.setUserId(userId);
        return Mono.fromCallable(() -> taskRepository.save(task));
    }

    public Mono<Task> findByTaskId(Long userId, Long taskId) {
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId, taskId).orElseThrow(TaskNotFoundException::new));
    }

    public Mono<Task> updateTask(Long userId, Long taskId, Task task) {
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId, taskId).orElseThrow(TaskNotFoundException::new))
                .zipWith(Mono.just(task), (dbTask, requestTask) -> {
                    if (nonNull(requestTask.getTitle())) {
                        dbTask.setTitle(requestTask.getTitle());
                    }
                    if (nonNull(requestTask.getDescription())) {
                        dbTask.setDescription(requestTask.getDescription());
                    }
                    if (nonNull(requestTask.getStatus())) {
                        dbTask.setStatus(requestTask.getStatus());
                    }
                    if (nonNull(requestTask.getDueDate())) {
                        dbTask.setDueDate(requestTask.getDueDate());
                    }
                    if (nonNull(requestTask.getTags())) {
                        dbTask.setTags(requestTask.getTags());
                    }
                    return dbTask;
                })
                .map(taskRepository::save);
    }

    public List<Task> allTask(Long userId) {
        return taskRepository.findTasksByUserId(userId);
    }
}