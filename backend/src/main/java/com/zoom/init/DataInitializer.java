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
        // Crée un utilisateur par défaut si aucun utilisateur n'existe
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole("ADMIN");

            userRepository.save(adminUser);
        }
    }
}
