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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
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
                    //TODO fix me :: When request is missing , the model has default value which is not null - It might not work time to time
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

    public Mono<List<Task>> allTasksByFilter(Long userId, String filter) {
        return switch (filter) {
            case "TODAY_TODO" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateIs(
                    userId,
                    LocalDate.now())
            );
            case "TOMORROW_TODO" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateIs(
                    userId,
                    LocalDate.now().plusDays(1))
            );
            case "CURRENT_WEEK_TODO" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    LocalDate.now().with(DayOfWeek.MONDAY),
                    LocalDate.now().with(DayOfWeek.SUNDAY))
            );
            case "NEXT_WEEK_TODO" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1),
                    LocalDate.now().with(DayOfWeek.SUNDAY).plusWeeks(1))
            );
            case "LAST_WEEK_TODO" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1),
                    LocalDate.now().with(DayOfWeek.SUNDAY).minusWeeks(1))
            );
            case "LAST_MONTH" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    YearMonth.from(LocalDate.now()).minusMonths(1).atDay(1),
                    YearMonth.from(LocalDate.now()).minusMonths(1).atEndOfMonth())
            );
            case "CURRENT_MONTH" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    YearMonth.from(LocalDate.now()).atDay(1),
                    YearMonth.from(LocalDate.now()).atEndOfMonth())
            );
            case "NEXT_MONTH" -> Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateBetween(
                    userId,
                    YearMonth.from(LocalDate.now()).plusMonths(1).atDay(1),
                    YearMonth.from(LocalDate.now()).plusMonths(1).atEndOfMonth())
            );
            default -> Mono.error(() -> new IllegalStateException("Unexpected value: " + filter));
        };
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

    public Mono<List<Task>> notification(Long userId) {
        //Grab all the tasks for the user which are due today and which are from past date but status not equal to COMPLETE
        var dueToday = Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateIs(userId, LocalDate.now()));
        var dueYesterday = Mono.fromCallable(() -> taskRepository.findTasksByUserIdAndDateIs(userId, LocalDate.now().minusDays(1)));

        return dueToday.zipWith(dueYesterday, (dT, dY) -> {
                    var tasks = new HashSet<Task>();
                    tasks.addAll(dT);
                    tasks.addAll(dY);
                    return tasks;
                })
                .flatMap(tasks -> {
                    if (isEmpty(tasks)) {
                        return Mono.just(List.of());
                    }
                    return Flux.fromIterable(tasks)
                            .filter(task -> task.getStatus().equals(Task.TaskStatus.PENDING))
                            .collectList();
                });
    }
}
