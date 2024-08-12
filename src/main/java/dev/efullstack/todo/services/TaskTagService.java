package dev.efullstack.todo.services;

import dev.efullstack.todo.models.Task;
import dev.efullstack.todo.models.TaskTag;
import dev.efullstack.todo.repositories.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskTagService {
    final TaskTagRepository taskTagRepository;
    final TaskService taskService;

    public Mono<List<Task>> findTagsByTagId(Long userId, Long tagId) {
        //collect the task ids
        //call taskService.findByTaskId for each task parallelly and collect the results
        return Mono.fromCallable(() -> taskTagRepository.findTaskTagByTagId(tagId)
                        .stream().map(TaskTag::getTaskId)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll))
                .flatMapMany(Flux::fromIterable)
                .cast(Long.class)
                .flatMap(taskId -> taskService.findByTaskId(userId, taskId))
                .collectList();
    }
}
