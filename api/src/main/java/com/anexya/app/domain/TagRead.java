package com.anexya.app.domain;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record TagRead(UUID id, String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt) {
}
