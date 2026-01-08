package com.anexya.app.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.anexya.app.cloud.CloudServiceFactory;
import com.anexya.app.domain.TagRead;
import com.anexya.app.repository.TagReadRepository;
import com.anexya.app.web.TagReadNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultTagReadService implements TagReadService
{

    private final TagReadRepository repository;
    private final CloudServiceFactory cloudFactory;
    @Override
    public TagRead get(UUID id)
    {
        return repository.findById(id).orElseThrow(() -> new TagReadNotFoundException(id));
    }

    @Override
    public TagRead create(String siteName, String epc, String referenceCode, String location, Double rssi,
            Instant readAt)
    {
        TagRead tx = TagRead.builder().id(UUID.randomUUID()).siteName(siteName).epc(epc).referenceCode(referenceCode)
                .location(location).rssi(rssi).readAt(readAt != null ? readAt : Instant.now()).build();
        TagRead saved = repository.save(tx);
        cloudFactory.logger().log("tag_read_created",
                Map.of("site", siteName, "epc", epc, "location", location, "referenceCode", referenceCode));
        cloudFactory.metrics().increment("tag_reads.created", 1.0, Map.of("site", siteName, "epc", epc));
        return saved;
    }

    @Override
    public TagRead update(UUID id, String siteName, String epc, String referenceCode, String location, Double rssi,
            Instant readAt)
    {
        TagRead existing = get(id);
        TagRead updated = existing.toBuilder().siteName(siteName).epc(epc).referenceCode(referenceCode)
                .location(location).rssi(rssi).readAt(readAt != null ? readAt : existing.getReadAt()).build();
        TagRead saved = repository.save(updated);
        cloudFactory.logger().log("tag_read_updated",
                Map.of("site", siteName, "epc", epc, "location", location, "referenceCode", referenceCode));
        cloudFactory.metrics().increment("tag_reads.updated", 1.0, Map.of("site", siteName, "epc", epc));
        return saved;
    }

    @Override
    public void delete(UUID id)
    {
        get(id); // ensures 404 if not found
        repository.deleteById(id);
        cloudFactory.logger().log("tag_read_deleted", Map.of("id", id.toString()));
        cloudFactory.metrics().increment("tag_reads.deleted", 1.0, Map.of());
    }

    @Override
    public List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName)
    {
        return repository.findByFilters(epc, location, siteName);
    }
}
