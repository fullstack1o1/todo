package dev.efullstack.todo.services;

import dev.efullstack.todo.TaskNotFoundException;
import dev.efullstack.todo.models.Tag;
import dev.efullstack.todo.models.Task;
import dev.efullstack.todo.models.TaskTag;
import dev.efullstack.todo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.*;

@Service
@RequiredArgsConstructor
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    final TaskRepository taskRepository;

    public Mono<Task> newTask(Long userId, Task task) {
        assert nonNull(task);
        task.setUserId(userId);
        return Mono.fromCallable(() -> taskRepository.save(task));
    }

    public Mono<Task> findByTaskId(Long userId, Long taskId) {
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId, taskId).orElseThrow(TaskNotFoundException::new));
    }

    public Mono<Task> patchTask(Long userId, Long taskId, Task task) {
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId, taskId).orElseThrow(TaskNotFoundException::new))
                .zipWith(Mono.just(task), (dbTask, requestTask) -> {
                    if (nonNull(requestTask.getTitle())) {
                        dbTask.setTitle(requestTask.getTitle());
                    }

                    if (nonNull(requestTask.getDescription())) {
                        dbTask.setDescription(requestTask.getDescription());
                    }

                    if (nonNull(requestTask.getStatus()) && !dbTask.getStatus().equals(requestTask.getStatus())) {
                        dbTask.setStatus(requestTask.getStatus());
                    }

                    if (nonNull(requestTask.getDate())) {
                        dbTask.setDate(requestTask.getDate());
                    }

                    if (nonNull(requestTask.getTime())) {
                        dbTask.setTime(requestTask.getTime());
                    }

                    if (!requestTask.getTags().isEmpty() && !dbTask.getTags().equals(requestTask.getTags())) {
                        dbTask.setTags(requestTask.getTags());
                    }
                    return dbTask;
                })
                .map(taskRepository::save);
    }

    public Mono<List<Task>> allTasks(Long userId) {
        return Mono.fromCallable(() -> taskRepository.findTasksByUserId(userId));
    }

    public Mono<Void> deleteTask(Long userId, Long taskId) {
        return Mono.fromRunnable(() -> taskRepository.deleteById(taskId));
    }

    public Mono<Task> updateTask(Long userId, Long taskId, Task task) {
        task.setUserId(userId);
        return Mono.fromCallable(() -> taskRepository.findByUserIdAndTaskId(userId, taskId).orElseThrow(TaskNotFoundException::new))
                .publishOn(Schedulers.boundedElastic())
                .map(dbTask -> taskRepository.save(task));
    }
}
