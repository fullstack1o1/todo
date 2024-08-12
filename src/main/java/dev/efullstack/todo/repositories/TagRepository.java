package dev.efullstack.todo.repositories;

import dev.efullstack.todo.models.Tag;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends ListCrudRepository<Tag, Long> {
    List<Tag> findAllByUserId(Long userId);

    Optional<Tag> findByUserIdAndId(Long userId, Long tagId);
}
