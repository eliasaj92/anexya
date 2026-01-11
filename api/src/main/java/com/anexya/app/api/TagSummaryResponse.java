package com.anexya.app.api;

import lombok.Builder;

@Builder
public record TagSummaryResponse(String epc,
                                 long totalReadCount,
                                 double averageRssi,
                                 double peakRssi,
                                 long locationCount,
                                 String mostDetectedLocation,
                                 String firstSeen,
                                 String lastSeen) {
}
