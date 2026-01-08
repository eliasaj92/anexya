package com.anexya.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.anexya.app.domain.TagRead;

@Repository
@Profile({"default", "inmemory"})
public class InMemoryTagReadRepository implements TagReadRepository {

    private final ConcurrentMap<UUID, TagRead> store = new ConcurrentHashMap<>();

    @Override
    public List<TagRead> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<TagRead> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public TagRead save(TagRead tx) {
        store.put(tx.getId(), tx);
        return tx;
    }
}
