package com.anexya.app.api;

import com.anexya.app.service.TagSummary;

import lombok.Value;

@Value
public class TagSummaryResponse {
    String epc;
    long totalReadCount;
    double averageRssi;
    double peakRssi;
    long locationCount;
    String mostDetectedLocation;
    String firstSeen;
    String lastSeen;

    public static TagSummaryResponse from(TagSummary s) {
        return new TagSummaryResponse(
                s.epc(),
                s.totalReadCount(),
                s.averageRssi(),
                s.peakRssi(),
                s.locationCount(),
                s.mostDetectedLocation(),
                s.firstSeen() != null ? s.firstSeen().toString() : null,
                s.lastSeen() != null ? s.lastSeen().toString() : null);
    }
}
