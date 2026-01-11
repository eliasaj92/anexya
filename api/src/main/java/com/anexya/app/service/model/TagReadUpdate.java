package com.anexya.app.service.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record TagReadUpdate(String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt) {
}
