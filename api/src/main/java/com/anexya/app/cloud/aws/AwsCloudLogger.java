package com.anexya.app.cloud.aws;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.anexya.app.cloud.CloudLogger;

@Component
@Profile("aws")
public class AwsCloudLogger implements CloudLogger {
    private static final Logger log = LoggerFactory.getLogger(AwsCloudLogger.class);

    @Override
    public void log(String event, Map<String, String> fields) {
        log.info("[AWS-CloudWatch] event={} fields={}", event, fields);
    }
}
