package com.anexya.app.cloud.noop;

import java.time.Duration;
import java.util.Map;

import com.anexya.app.cloud.MetricsPublisher;

public class NoOpMetricsPublisher implements MetricsPublisher
{
    @Override
    public void increment(String metricName, double amount, Map<String, String> tags)
    {
        // no-op
    }

    @Override
    public void recordDuration(String metricName, Duration duration, Map<String, String> tags)
    {
        // no-op
    }
}
