package com.zoom.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.dto.AuthResponse;
import com.zoom.dto.LoginRequest;
import com.zoom.entity.User;
import com.zoom.repository.UserRepository;
import com.zoom.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour l'authentification des utilisateurs
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authentifie un utilisateur et génère un token JWT
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'utilisateur: {}", request.getUsername());

        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Mot de passe incorrect pour l'utilisateur: {}", request.getUsername());
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Génère le token JWT
        String token = jwtTokenProvider.generateToken(user.getUsername());

        log.info("✅ Connexion réussie pour l'utilisateur: {}", request.getUsername());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole());
    }

    /**
     * Crée un nouvel utilisateur (pour l'administration)
     */
    public User createUser(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("L'utilisateur existe déjà");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Valide un token JWT
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Extrait le username d'un token JWT
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.extractUsername(token);
    }
}
