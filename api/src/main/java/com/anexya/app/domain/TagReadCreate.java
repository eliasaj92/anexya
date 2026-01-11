package com.anexya.app.domain;

import java.time.Instant;

import lombok.Builder;

@Builder
public record TagReadCreate(String siteName, String epc, String referenceCode, String location, Double rssi, Instant readAt) {
}