package com.anexya.app.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateTagReadRequest(@NotBlank String siteName,
                                   @NotBlank String epc,
                                   @NotBlank String referenceCode,
                                   @NotBlank String location,
                                   @NotNull Double rssi,
                                   @NotNull Instant readAt) {
}
