package com.anexya.app.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.anexya.app.domain.TagRead;

public interface TagReadService
{
    TagRead get(UUID id);

    TagRead create(String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt);

    TagRead update(UUID id, String siteName, String epc, String referenceCode, String location, Double rssi,
            Instant readAt);

    void delete(UUID id);

    List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName);
}
