package com.anexya.app.service.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record TagSummary(String epc,
                         long totalReadCount,
                         double averageRssi,
                         double peakRssi,
                         long locationCount,
                         String mostDetectedLocation,
                         Instant firstSeen,
                         Instant lastSeen) {
}
