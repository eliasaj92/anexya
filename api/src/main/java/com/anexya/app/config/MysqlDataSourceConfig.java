package com.anexya.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"mysql", "MYSQL"})
public class MysqlDataSourceConfig {
    // No custom beans needed; Spring Boot auto-configures DataSource based on
    // application properties for the 'mysql' profile.
}
