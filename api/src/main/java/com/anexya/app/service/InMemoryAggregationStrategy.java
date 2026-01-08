package com.anexya.app.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.anexya.app.repository.TagReadRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile(
{"default", "inmemory"})
@RequiredArgsConstructor
public class InMemoryAggregationStrategy implements AggregationStrategy
{

    private final TagReadRepository repository;

    @Override
    public List<TagSummary> summarizeByTag(Instant startDate, Instant endDate, Optional<String> siteName,
            Optional<String> epc)
    {
        List<com.anexya.app.domain.TagRead> filtered = repository.findByFilters(epc, Optional.empty(), siteName)
                .stream().filter(t -> !t.getReadAt().isBefore(startDate) && !t.getReadAt().isAfter(endDate)).toList();

        Map<String, List<com.anexya.app.domain.TagRead>> byEpc = filtered.stream()
                .collect(Collectors.groupingBy(com.anexya.app.domain.TagRead::getEpc));

        return byEpc.entrySet().stream().map(entry -> {
            String tag = entry.getKey();
            List<com.anexya.app.domain.TagRead> txs = entry.getValue();
            long total = txs.size();
            double avgRssi = txs.stream().mapToDouble(com.anexya.app.domain.TagRead::getRssi).average().orElse(0.0);
            double peakRssi = txs.stream().mapToDouble(com.anexya.app.domain.TagRead::getRssi).max().orElse(0.0);
            long locationCount = txs.stream().map(com.anexya.app.domain.TagRead::getLocation).distinct().count();
            String mostDetectedLocation = txs.stream()
                    .collect(Collectors.groupingBy(com.anexya.app.domain.TagRead::getLocation, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey).orElse(null);
            Instant firstSeen = txs.stream().map(com.anexya.app.domain.TagRead::getReadAt)
                    .min(Comparator.naturalOrder()).orElse(null);
            Instant lastSeen = txs.stream().map(com.anexya.app.domain.TagRead::getReadAt).max(Comparator.naturalOrder())
                    .orElse(null);
            return new TagSummary(tag, total, avgRssi, peakRssi, locationCount, mostDetectedLocation, firstSeen,
                    lastSeen);
        }).toList();
    }
}
