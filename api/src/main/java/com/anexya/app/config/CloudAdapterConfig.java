package com.anexya.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.anexya.app.cloud.noop.NoOpCloudLogger;
import com.anexya.app.cloud.noop.NoOpKeyManagementService;
import com.anexya.app.cloud.noop.NoOpMetricsPublisher;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
public class CloudAdapterConfig
{

    @Configuration
    @Profile("aws")
    static class AwsConfig
    {
        @Bean
        KmsClient kmsClient()
        {
            // Uses default credential and region providers (env/instance profile/container
            // creds)
            return KmsClient.builder().region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
                    .build();
        }
    }

    @Configuration
    @Profile("!aws")
    static class DefaultConfig
    {
        @Bean
        NoOpCloudLogger noOpCloudLogger()
        {
            return new NoOpCloudLogger();
        }
        @Bean
        NoOpMetricsPublisher noOpMetricsPublisher()
        {
            return new NoOpMetricsPublisher();
        }
        @Bean
        NoOpKeyManagementService noOpKeyManagementService()
        {
            return new NoOpKeyManagementService();
        }
    }
}
