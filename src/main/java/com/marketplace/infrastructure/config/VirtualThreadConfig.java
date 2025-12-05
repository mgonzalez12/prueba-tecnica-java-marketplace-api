package com.marketplace.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central configuration for virtual thread usage across the application.
 *
 * This keeps the hexagonal architecture intact:
 * - Domain layer is unaware of threading concerns.
 * - Application and infrastructure layers can run blocking / IO-bound work
 *   on lightweight virtual threads.
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * Global ExecutorService backed by Java virtual threads (Java 21).
     *
     * Use this executor for:
     * - External API calls that block (e.g. WebClient.block())
     * - Concurrent batch processing of external products
     *
     * The container will manage its lifecycle and shut it down on context close.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}


