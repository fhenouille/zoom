package com.zoom.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Post-processeur d'environnement Spring pour diagnostiquer et configurer DATABASE_URL
 * Exécuté très tôt dans le cycle de démarrage de Spring
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 DATABASE_URL Configuration Diagnostic");
        System.out.println("=".repeat(60));

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            System.out.println("❌ DATABASE_URL environment variable NOT SET");
            System.out.println("   Available properties in Spring context will use fallback");
        } else {
            System.out.println("✅ DATABASE_URL is SET");
            System.out.println("   Original format: " + maskSensitiveData(databaseUrl));

            // Check if it's already in JDBC format
            if (databaseUrl.startsWith("jdbc:postgresql://")) {
                System.out.println("✅ Already in JDBC format");
            } else if (databaseUrl.startsWith("postgres://")) {
                System.out.println("📝 Raw postgres:// format detected");
                System.out.println("   Dockerfile will convert to jdbc:postgresql://");

                // Convert and set as new property
                String jdbcUrl = convertPostgresToJdbc(databaseUrl);
                System.out.println("   After conversion: " + maskSensitiveData(jdbcUrl));

                // Add to Spring properties
                Map<String, Object> properties = new HashMap<>();
                properties.put("JDBC_DATABASE_URL", jdbcUrl);
                environment.getPropertySources().addFirst(
                    new MapPropertySource("converted-database-url", properties)
                );
            }
        }

        // Log active profiles
        String[] profiles = environment.getActiveProfiles();
        System.out.println("\n📋 Active Spring Profiles: " +
            (profiles.length > 0 ? String.join(", ", profiles) : "none"));

        // Log datasource configuration
        System.out.println("\n📊 DataSource Configuration:");
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        System.out.println("   spring.datasource.url: " +
            (datasourceUrl != null ? maskSensitiveData(datasourceUrl) : "NOT SET"));
        System.out.println("   driver-class-name: " +
            environment.getProperty("spring.datasource.driver-class-name"));
        System.out.println("   hikari.max-pool-size: " +
            environment.getProperty("spring.datasource.hikari.maximum-pool-size"));
        System.out.println("   PORT: " + environment.getProperty("PORT", "8080"));

        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Convertit le format postgres:// en jdbc:postgresql://
     */
    private String convertPostgresToJdbc(String postgresUrl) {
        if (postgresUrl == null) {
            return null;
        }
        return postgresUrl.replaceFirst("^postgres://", "jdbc:postgresql://");
    }

    /**
     * Masque les informations sensibles (password) pour l'affichage
     */
    private String maskSensitiveData(String url) {
        if (url == null) {
            return null;
        }
        // Masquer le mot de passe: user:password@host → user:****@host
        return url.replaceAll("(://)([^:]+):([^@]+)@", "$1$2:****@");
    }
}
