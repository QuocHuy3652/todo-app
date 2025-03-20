package com.huydevcorn.todo_app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Configuration class for scheduling tasks.
 */
@Configuration
public class SchedulerConfig {
    /**
     * Configures and returns a ScheduledExecutorService bean with a thread pool of size 5.
     *
     * @return the configured ScheduledExecutorService
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(5);
    }

    /**
     * Configures and returns a DateTimeFormatter bean with the pattern "yyyy-MM-dd HH:mm:ss".
     *
     * @return the configured DateTimeFormatter
     */
    @Bean
    DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}
