package com.zoom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Empty configuration placeholder.
 * Flyway is disabled on Railway in favor of Hibernate schema management.
 */
@Configuration
@Profile("railway")
public class FlywayConfiguration {
    // Flyway disabled - Hibernate manages schema on Railway
}
