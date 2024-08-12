package dev.efullstack.todo.repositories;

import dev.efullstack.todo.models.TaskTag;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface TaskTagRepository extends ListCrudRepository<TaskTag, Long> {
    List<TaskTag> findTaskTagByTagId(Long tagId);
}
