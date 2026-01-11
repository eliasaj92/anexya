package com.anexya.app.cloud;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CloudServiceFactory {
    private final ObjectProvider<CloudLogger> cloudLogger;
    private final ObjectProvider<MetricsPublisher> metricsPublisher;
    private final ObjectProvider<KeyManagementService> keyManagementService;
    private final Environment environment;

    public Optional<CloudLogger> logger() {
        return Optional.ofNullable(cloudLogger.getIfAvailable());
    }

    public Optional<MetricsPublisher> metrics() {
        return Optional.ofNullable(metricsPublisher.getIfAvailable());
    }

    public Optional<KeyManagementService> kms() {
        return Optional.ofNullable(keyManagementService.getIfAvailable());
    }

    public CloudProvider provider() {
        return environment.matchesProfiles("aws") ? CloudProvider.AWS : CloudProvider.NONE;
    }
}
