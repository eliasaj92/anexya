package com.anexya.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

@SpringBootApplication
public class AnexyaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnexyaApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public ExecutorService applicationVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public TaskExecutor taskExecutor(ExecutorService applicationVirtualThreadExecutor) {
        return new TaskExecutorAdapter(applicationVirtualThreadExecutor);
    }
}
