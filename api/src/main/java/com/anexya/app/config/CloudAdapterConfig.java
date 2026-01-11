package com.anexya.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
public class CloudAdapterConfig {
    @Configuration
    @Profile("aws")
    static class AwsConfig {
        @Bean
        KmsClient kmsClient() {
            // Uses default credential and region providers (env/instance
            // profile/container
            // creds)
            return KmsClient.builder()
                            .region(Region.of(System.getenv()
                                                    .getOrDefault("AWS_REGION", "us-east-1")))
                            .build();
        }
    }
}
