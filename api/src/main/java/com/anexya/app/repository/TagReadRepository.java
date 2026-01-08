package com.anexya.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.anexya.app.domain.TagRead;

public interface TagReadRepository
{
    Optional<TagRead> findById(UUID id);

    TagRead save(TagRead tx);

    void deleteById(UUID id);

    List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName);
}
