package com.anexya.app.cloud.noop;

import java.util.Map;

import com.anexya.app.cloud.CloudLogger;

public class NoOpCloudLogger implements CloudLogger
{
    @Override
    public void log(String event, Map<String, String> fields)
    {
        // no-op
    }
}
