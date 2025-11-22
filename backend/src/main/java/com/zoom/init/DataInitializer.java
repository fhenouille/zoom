package com.zoom.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zoom.entity.User;
import com.zoom.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialise un utilisateur par défaut au démarrage de l'application
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("🔄 DataInitializer - Vérification de la base de données...");

        long userCount = userRepository.count();
        log.info("📊 DataInitializer - Nombre d'utilisateurs existants: {}", userCount);

        // Crée un utilisateur par défaut si aucun utilisateur n'existe
        if (userCount == 0) {
            log.info("📝 DataInitializer - Création de l'utilisateur admin par défaut...");

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole("ADMIN");

            userRepository.save(adminUser);

            log.info("✅ DataInitializer - Utilisateur admin créé avec le rôle: ADMIN");
            log.info("🔑 DataInitializer - Username: admin | Password: {} | Role: ADMIN", adminPassword);
        } else {
            log.info("ℹ️ DataInitializer - Utilisateurs existants, pas d'initialisation nécessaire");
        }
    }
}
