package com.anexya.app.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTagReadRequest
{
    @NotBlank
    private String siteName;

    @NotBlank
    private String epc;

    @NotBlank
    private String referenceCode;

    @NotBlank
    private String location;

    @NotNull
    private Double rssi;

    @NotNull
    private Instant readAt;
}
