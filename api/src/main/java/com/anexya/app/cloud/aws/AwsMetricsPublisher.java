package com.anexya.app.cloud.aws;

import java.time.Duration;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.anexya.app.cloud.MetricsPublisher;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class AwsMetricsPublisher implements MetricsPublisher
{

    private final MeterRegistry meterRegistry;

    @Override
    public void increment(String metricName, double amount, Map<String, String> tags)
    {
        meterRegistry.counter(metricName, tagsToArray(tags)).increment(amount);
    }

    @Override
    public void recordDuration(String metricName, Duration duration, Map<String, String> tags)
    {
        meterRegistry.timer(metricName, tagsToArray(tags)).record(duration);
    }

    private String[] tagsToArray(Map<String, String> tags)
    {
        return tags.entrySet().stream().flatMap(e -> java.util.stream.Stream.of(e.getKey(), e.getValue()))
                .toArray(String[]::new);
    }
}
