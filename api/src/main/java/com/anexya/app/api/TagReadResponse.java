package com.anexya.app.api;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record TagReadResponse(UUID id, String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt) {
}
