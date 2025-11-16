package com.zoom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration de Flyway pour Railway (PostgreSQL).
 * 
 * Cette configuration est activée UNIQUEMENT lorsque le profil "railway" est actif.
 * Flyway est maintenant géré par Spring Boot auto-configuration avec les propriétés
 * appropriées dans application-railway.properties, ce qui évite les dépendances circulaires.
 */
@Configuration
@Profile("railway")
public class FlywayConfiguration {
    // Configuration vide - Flyway est géré par auto-configuration Spring Boot
}
