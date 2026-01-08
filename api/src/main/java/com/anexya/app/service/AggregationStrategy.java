package com.anexya.app.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AggregationStrategy
{
    List<TagSummary> summarizeByTag(Instant startDate, Instant endDate, Optional<String> siteName,
            Optional<String> epc);
}
