package dev.efullstack.todo.services;


import dev.efullstack.todo.TagNotFoundException;
import dev.efullstack.todo.models.Tag;
import dev.efullstack.todo.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class TagService {
    final TagRepository tagRepository;

    public Mono<Tag> newTag(Long userId, Tag tag) {
        tag.setUserId(userId);
        return Mono.fromCallable(() -> tagRepository.save(tag));
    }

    public Mono<Tag> tagById(Long userId, Long tagId) {
        return Mono.fromCallable(() -> tagRepository.findByUserIdAndId(userId, tagId).orElseThrow(TagNotFoundException::new));
    }

    public Mono<Tag> patchTag(Long userId, Long tagId, Tag tag) {
        return tagById(userId, tagId)
                .zipWith(Mono.just(tag), (dbTag, requestTag) -> {
                    if (nonNull(requestTag.getName())) {
                        dbTag.setName(requestTag.getName());
                    }
                    return dbTag;
                });
    }

    public Mono<Tag> updateTag(Long userId, Long tagId, Tag tag) {
        tag.setUserId(userId);
        return tagById(userId, tagId)
                .publishOn(Schedulers.boundedElastic())
                .map(dbTag -> tagRepository.save(tag));
    }

    public Mono<List<Tag>> allTag(Long userId) {
        return Mono.fromCallable(() -> tagRepository.findAllByUserId(userId));
    }

    public Mono<Void> deleteTag(Long userId, Long tagId) {
        return tagById(userId, tagId)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(tagRepository::delete)
                .then();
    }
}
