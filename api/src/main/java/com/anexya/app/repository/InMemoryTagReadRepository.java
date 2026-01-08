package com.anexya.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.anexya.app.domain.TagRead;

@Repository
@Profile(
{"default", "inmemory"})
public class InMemoryTagReadRepository implements TagReadRepository
{

    private final ConcurrentMap<UUID, TagRead> store = new ConcurrentHashMap<>();

    @Override
    public Optional<TagRead> findById(UUID id)
    {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public TagRead save(TagRead tx)
    {
        store.put(tx.getId(), tx);
        return tx;
    }

    @Override
    public void deleteById(UUID id)
    {
        store.remove(id);
    }

    @Override
    public List<TagRead> findByFilters(Optional<String> epc, Optional<String> location, Optional<String> siteName)
    {
        Stream<TagRead> stream = store.values().stream();
        if (epc.isPresent())
        {
            String epcVal = epc.get();
            stream = stream.filter(t -> epcVal.equals(t.getEpc()));
        }
        if (location.isPresent())
        {
            String locVal = location.get();
            stream = stream.filter(t -> locVal.equals(t.getLocation()));
        }
        if (siteName.isPresent())
        {
            String siteVal = siteName.get();
            stream = stream.filter(t -> siteVal.equals(t.getSiteName()));
        }
        return stream.toList();
    }
}
