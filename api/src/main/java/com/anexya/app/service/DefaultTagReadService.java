package com.anexya.app.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.anexya.app.domain.TagRead;
import com.anexya.app.repository.TagReadRepository;
import com.anexya.app.web.TagReadNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultTagReadService implements TagReadService {

    private final TagReadRepository repository;

    @Override
    public List<TagRead> findAll() {
        return repository.findAll();
    }

    @Override
    public TagRead get(UUID id) {
    return repository.findById(id).orElseThrow(() -> new TagReadNotFoundException(id));
    }

    @Override
    public TagRead create(String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt) {
        TagRead tx = TagRead.builder()
                .id(UUID.randomUUID())
                .siteName(siteName)
                .epc(epc)
                .referenceCode(referenceCode)
                .location(location)
                .rssi(rssi)
                .readAt(readAt != null ? readAt : Instant.now())
                .build();
        return repository.save(tx);
    }
}
