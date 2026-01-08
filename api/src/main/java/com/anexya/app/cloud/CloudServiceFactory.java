package com.anexya.app.cloud;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CloudServiceFactory
{

    private final CloudLogger cloudLogger;
    private final MetricsPublisher metricsPublisher;
    private final KeyManagementService keyManagementService;
    private final Environment environment;

    public CloudLogger logger()
    {
        return cloudLogger;
    }

    public MetricsPublisher metrics()
    {
        return metricsPublisher;
    }

    public KeyManagementService kms()
    {
        return keyManagementService;
    }

    public CloudProvider provider()
    {
        boolean isAws = Arrays.asList(environment.getActiveProfiles()).contains("aws");
        return isAws ? CloudProvider.AWS : CloudProvider.NONE;
    }
}
