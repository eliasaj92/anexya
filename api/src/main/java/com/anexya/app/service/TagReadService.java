package com.anexya.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.anexya.app.domain.TagRead;
import com.anexya.app.service.model.TagReadCreate;
import com.anexya.app.service.model.TagReadUpdate;

public interface TagReadService {
    TagRead get(UUID id);

    TagRead create(TagReadCreate create);

    TagRead update(UUID id, TagReadUpdate update);

    void delete(UUID id);

    List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName);
}
