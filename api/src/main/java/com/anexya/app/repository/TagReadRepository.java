package com.anexya.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.anexya.app.domain.TagRead;

public interface TagReadRepository {
    List<TagRead> findAll();

    Optional<TagRead> findById(UUID id);

    TagRead save(TagRead tx);
}
