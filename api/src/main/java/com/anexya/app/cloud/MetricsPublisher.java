package com.anexya.app.cloud;

import java.time.Duration;
import java.util.Map;

public interface MetricsPublisher
{
    void increment(String metricName, double amount, Map<String, String> tags);

    void recordDuration(String metricName, Duration duration, Map<String, String> tags);
}
