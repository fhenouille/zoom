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

    @Value("${zoom.user-id}")
    private String zoomUserId;

    @Value("${zoom.account-id}")
    private String zoomAccountId;

    @Override
    public void run(String... args) {
        // Crée un utilisateur par défaut si aucun utilisateur n'existe
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("z00Mons"));
            adminUser.setZoomUserId(zoomUserId);
            adminUser.setZoomAccountId(zoomAccountId);

            userRepository.save(adminUser);

            log.info("👤 Utilisateur par défaut créé:");
            log.info("   Username: admin");
            log.info("   Password: z00Mons");
            log.info("   Zoom User ID: {}", zoomUserId);
            log.info("   Zoom Account ID: {}", zoomAccountId);
        }
    }
}
