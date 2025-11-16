package com.zoom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Parse the DATABASE_URL environment variable from Railway
 * Format: postgres://user:password@host:port/database
 */
@Configuration
@Profile("railway")
public class RailwayDatabaseConfig {

    public RailwayDatabaseConfig() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            System.out.println("✅ DATABASE_URL detected: " + maskUrl(databaseUrl));
            // Parse and set system properties if needed
            parseDatabaseUrl(databaseUrl);
        } else {
            System.out.println("⚠️  DATABASE_URL not found. Using default localhost connection.");
        }
    }

    private void parseDatabaseUrl(String databaseUrl) {
        try {
            // Convert postgres:// to jdbc:postgresql://
            if (databaseUrl.startsWith("postgres://")) {
                String jdbcUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
                System.setProperty("spring.datasource.url", jdbcUrl);
                System.out.println("✅ Updated JDBC URL");
            } else if (databaseUrl.startsWith("postgresql://")) {
                String jdbcUrl = databaseUrl.replace("postgresql://", "jdbc:postgresql://");
                System.setProperty("spring.datasource.url", jdbcUrl);
                System.out.println("✅ Updated JDBC URL");
            }
        } catch (Exception e) {
            System.err.println("❌ Error parsing DATABASE_URL: " + e.getMessage());
        }
    }

    private String maskUrl(String url) {
        return url.replaceAll(":[^:/@]+@", ":****@");
    }
}
