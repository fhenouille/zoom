package com.zoom.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Configuration de Flyway pour Railway (PostgreSQL).
 * 
 * Cette configuration est activée UNIQUEMENT lorsque le profil "railway" est actif.
 * Elle crée une instance Flyway qui s'exécute au démarrage AVANT la création
 * d'EntityManagerFactory, évitant les dépendances circulaires avec JPA/Hibernate.
 */
@Configuration
@Profile("railway")
@EnableConfigurationProperties
public class FlywayConfiguration {

    /**
     * Configure et crée Flyway Bean pour la migration de base de données.
     * S'exécute automatiquement au démarrage de l'application.
     * 
     * @param dataSource La source de données (PostgreSQL sur Railway)
     * @param environment L'environnement Spring pour accéder aux propriétés
     * @return Instance Flyway configurée et prête pour les migrations
     */
    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource, Environment environment) {
        // Récupère la configuration depuis les propriétés
        String locationsValue = environment.getProperty("spring.flyway.locations", 
            "classpath:db/migration");
        
        // Crée et configure Flyway indépendamment de JPA
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locationsValue)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }
}
