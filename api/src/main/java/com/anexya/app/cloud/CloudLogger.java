package com.anexya.app.cloud;

import java.util.Map;

public interface CloudLogger
{
    void log(String event, Map<String, String> fields);
}
