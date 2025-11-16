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

            // Convert to JDBC format if needed and set directly in Spring properties
            String jdbcUrl = convertToJdbc(databaseUrl);
            if (!databaseUrl.equals(jdbcUrl)) {
                System.out.println("📝 Converting to JDBC format");
                System.out.println("   After conversion: " + maskSensitiveData(jdbcUrl));
            } else {
                System.out.println("✅ Already in JDBC format");
            }

            // Set the JDBC URL in Spring's datasource configuration
            // This will be the first source, overriding any other configuration
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", jdbcUrl);
            environment.getPropertySources().addFirst(
                new MapPropertySource("railway-database-url", properties)
            );
            System.out.println("✅ spring.datasource.url configured with converted URL");
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
     * Convertit les formats postgres:// et postgresql:// en jdbc:postgresql://
     */
    private String convertToJdbc(String databaseUrl) {
        if (databaseUrl == null) {
            return null;
        }
        // Handle both postgres:// and postgresql:// prefixes
        if (databaseUrl.startsWith("postgres://")) {
            return databaseUrl.replaceFirst("^postgres://", "jdbc:postgresql://");
        } else if (databaseUrl.startsWith("postgresql://")) {
            return databaseUrl.replaceFirst("^postgresql://", "jdbc:postgresql://");
        }
        return databaseUrl;
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
