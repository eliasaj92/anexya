package com.anexya.app.service;

import java.time.Instant;

public record TagSummary(String epc, long totalReadCount, double averageRssi, double peakRssi, long locationCount,
        String mostDetectedLocation, Instant firstSeen, Instant lastSeen) {
}
