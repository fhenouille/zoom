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

    private static final String POSTGRES_URL_PREFIX = "postgres://";
    private static final String POSTGRESQL_URL_PREFIX = "postgresql://";

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

            // Convert to JDBC format
            String jdbcUrl = convertToJdbc(databaseUrl);
            if (!databaseUrl.equals(jdbcUrl)) {
                System.out.println("📝 Converting to JDBC format");
                System.out.println("   After conversion: " + maskSensitiveData(jdbcUrl));
            }

            // Extract credentials from URL for HikariCP
            String[] credentials = extractCredentials(databaseUrl);
            String username = credentials[0];
            String password = credentials[1];

            // Set properties with highest priority
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", jdbcUrl);

            if (username != null && !username.isEmpty()) {
                properties.put("spring.datasource.username", username);
            }
            if (password != null && !password.isEmpty()) {
                properties.put("spring.datasource.password", password);
            }

            environment.getPropertySources().addFirst(
                new MapPropertySource("railway-database-url", properties)
            );

            System.out.println("✅ spring.datasource.url configured with converted URL");
            if (username != null) {
                System.out.println("✅ spring.datasource.username set from DATABASE_URL");
            }
            if (password != null) {
                System.out.println("✅ spring.datasource.password set from DATABASE_URL");
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
     * Convertit les formats postgres:// et postgresql:// en jdbc:postgresql://
     * Railway provides: postgresql://user:pass@postgres.railway.internal:5432/railway
     */
    private String convertToJdbc(String databaseUrl) {
        if (databaseUrl == null) {
            return null;
        }

        // Simply replace the protocol prefix - the rest of URL stays the same
        if (databaseUrl.startsWith(POSTGRES_URL_PREFIX)) {
            return "jdbc:postgresql://" + databaseUrl.substring(POSTGRES_URL_PREFIX.length());
        } else if (databaseUrl.startsWith(POSTGRESQL_URL_PREFIX)) {
            return "jdbc:postgresql://" + databaseUrl.substring(POSTGRESQL_URL_PREFIX.length());
        }

        // Already in JDBC format or unknown format
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

    /**
     * Extrait le username et password de l'URL DATABASE_URL
     * Format: postgresql://user:password@host:port/database
     * Returns: [username, password]
     */
    private String[] extractCredentials(String databaseUrl) {
        String[] result = {"", ""};

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return result;
        }

        try {
            // Remove protocol prefix
            String urlWithoutProtocol = databaseUrl;
            if (databaseUrl.startsWith(POSTGRES_URL_PREFIX)) {
                urlWithoutProtocol = databaseUrl.substring(POSTGRES_URL_PREFIX.length());
            } else if (databaseUrl.startsWith(POSTGRESQL_URL_PREFIX)) {
                urlWithoutProtocol = databaseUrl.substring(POSTGRESQL_URL_PREFIX.length());
            }

            // Extract user:password portion (everything before @)
            int atIndex = urlWithoutProtocol.indexOf('@');
            if (atIndex > 0) {
                String userPassword = urlWithoutProtocol.substring(0, atIndex);
                int colonIndex = userPassword.indexOf(':');

                if (colonIndex > 0) {
                    result[0] = userPassword.substring(0, colonIndex); // username
                    result[1] = userPassword.substring(colonIndex + 1); // password
                } else {
                    result[0] = userPassword; // username only, no password
                }
            }
        } catch (Exception e) {
            System.out.println("   ⚠️  Failed to extract credentials from DATABASE_URL: " + e.getMessage());
        }

        return result;
    }
}

