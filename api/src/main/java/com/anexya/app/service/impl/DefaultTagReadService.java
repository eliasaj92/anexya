package com.anexya.app.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.anexya.app.cloud.CloudServiceFactory;
import com.anexya.app.domain.TagRead;
import com.anexya.app.domain.TagReadCreate;
import com.anexya.app.domain.TagReadUpdate;
import com.anexya.app.repository.TagReadRepository;
import com.anexya.app.service.TagReadService;
import com.anexya.app.web.TagReadNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultTagReadService implements TagReadService {
    private final TagReadRepository repository;
    private final ObjectProvider<CloudServiceFactory> cloudFactory;

    @Override
    public TagRead get(UUID id) {
        return repository.findById(id)
                         .orElseThrow(() -> new TagReadNotFoundException(id));
    }

    @Override
    public TagRead create(TagReadCreate create) {
        final TagRead tx = TagRead.builder()
                                  .id(UUID.randomUUID())
                                  .siteName(create.siteName())
                                  .epc(create.epc())
                                  .referenceCode(create.referenceCode())
                                  .location(create.location())
                                  .rssi(create.rssi())
                                  .readAt(create.readAt() != null ? create.readAt() : Instant.now())
                                  .build();
        final TagRead saved = repository.save(tx);
        final CloudServiceFactory cloud = cloudFactory.getIfAvailable();
        if (cloud != null) {
            cloud.logger()
                 .ifPresent(logger -> logger.log("tag_read_created",
                                                 Map.of("site", create.siteName(), "epc", create.epc(), "location", create.location(), "referenceCode", create.referenceCode())));
            cloud.metrics()
                 .ifPresent(metrics -> metrics.increment("tag_reads.created", 1.0, Map.of("site", create.siteName(), "epc", create.epc())));
        }
        return saved;
    }

    @Override
    public TagRead update(UUID id, TagReadUpdate update) {
        final TagRead existing = get(id);
        final TagRead updated = existing.toBuilder()
                                        .siteName(update.siteName())
                                        .epc(update.epc())
                                        .referenceCode(update.referenceCode())
                                        .location(update.location())
                                        .rssi(update.rssi())
                                        .readAt(update.readAt() != null ? update.readAt() : existing.readAt())
                                        .build();
        final TagRead saved = repository.save(updated);
        final CloudServiceFactory cloud = cloudFactory.getIfAvailable();
        if (cloud != null) {
            cloud.logger()
                 .ifPresent(logger -> logger.log("tag_read_updated",
                                                 Map.of("site", update.siteName(), "epc", update.epc(), "location", update.location(), "referenceCode", update.referenceCode())));
            cloud.metrics()
                 .ifPresent(metrics -> metrics.increment("tag_reads.updated", 1.0, Map.of("site", update.siteName(), "epc", update.epc())));
        }
        return saved;
    }

    @Override
    public void delete(UUID id) {
        get(id); // ensures 404 if not found
        repository.deleteById(id);
        final CloudServiceFactory cloud = cloudFactory.getIfAvailable();
        if (cloud != null) {
            cloud.logger()
                 .ifPresent(logger -> logger.log("tag_read_deleted", Map.of("id", id.toString())));
            cloud.metrics()
                 .ifPresent(metrics -> metrics.increment("tag_reads.deleted", 1.0, Map.of()));
        }
    }

    @Override
    public List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName) {
        return repository.findByFilters(epc, location, siteName);
    }
}
