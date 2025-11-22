package com.zoom.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint de diagnostic pour vérifier la configuration de la base de données
 * Accessible à GET /api/health/database-config (développement uniquement)
 *
 * ⚠️ IMPORTANT: À utiliser uniquement en développement/débogage
 * En production: Désactiver cet endpoint ou requérir une authentification ADMIN
 */
@RestController
@RequestMapping("/api/health")
public class DatabaseConfigController {

    private final Environment environment;

    @Autowired
    public DatabaseConfigController(Environment environment) {
        this.environment = environment;
    }

    /**
     * Vérifie si le mode développement est activé via variable d'environnement
     */
    private boolean isDevelopmentMode() {
        String debugMode = environment.getProperty("DEBUG_DATABASE_CONFIG", "false");
        return "true".equalsIgnoreCase(debugMode);
    }

    @GetMapping("/database-config")
    public ResponseEntity<Map<String, Object>> getDatabaseConfig() {
        // ⚠️ SÉCURITÉ: Cet endpoint doit être désactivé en production
        if (!isDevelopmentMode()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Database config endpoint disabled in production"));
        }
        Map<String, Object> config = new HashMap<>();

        // Variables d'environnement
        String databaseUrl = System.getenv("DATABASE_URL");
        String jdbcDatabaseUrl = System.getenv("JDBC_DATABASE_URL");
        String springProfiles = String.join(", ", environment.getActiveProfiles());

        config.put("database_url_set", databaseUrl != null);
        if (databaseUrl != null) {
            config.put("database_url_format", databaseUrl.startsWith("postgres://") ? "postgres://" :
                       databaseUrl.startsWith("jdbc:postgresql://") ? "jdbc:postgresql://" : "unknown");
            config.put("database_url_masked", maskPassword(databaseUrl));
        }

        config.put("jdbc_database_url_set", jdbcDatabaseUrl != null);
        if (jdbcDatabaseUrl != null) {
            config.put("jdbc_database_url_masked", maskPassword(jdbcDatabaseUrl));
        }

        // Configuration Spring
        config.put("active_profiles", springProfiles);
        config.put("datasource_url_configured", environment.getProperty("spring.datasource.url") != null);
        config.put("datasource_url_masked", maskPassword(
            environment.getProperty("spring.datasource.url", "NOT CONFIGURED")));
        config.put("driver_class", environment.getProperty("spring.datasource.driver-class-name"));
        config.put("port", environment.getProperty("PORT", "8080"));
        config.put("hikari_max_pool_size", environment.getProperty("spring.datasource.hikari.maximum-pool-size"));

        return ResponseEntity.ok(config);
    }

    /**
     * Masque le mot de passe pour sécurité
     */
    private String maskPassword(String url) {
        if (url == null || !url.contains("@")) {
            return url;
        }
        return url.replaceAll("(://)([^:]+):([^@]+)@", "$1$2:****@");
    }
}
