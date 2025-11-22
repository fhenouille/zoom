package com.zoom.service;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentification échouée pour: {}", request.getUsername());
            throw new RuntimeException("Invalid credentials");
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

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> getAllUsers() {
        log.info("Récupération de la liste de tous les utilisateurs");
        return userRepository.findAll();
    }

    /**
     * Récupère un utilisateur par son username
     */
    public User getUserByUsername(String username) {
        log.info("Récupération de l'utilisateur: {}", username);
        return userRepository.findById(username).orElse(null);
    }

    /**
     * Met à jour un utilisateur
     * Si password est null, le mot de passe n'est pas changé
     */
    public User updateUser(String username, String role, String password) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        log.info("Mise à jour de l'utilisateur: {} avec le rôle: {}", username, role);

        user.setRole(role);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
            log.info("Mot de passe mis à jour pour l'utilisateur: {}", username);
        }

        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur
     */
    public boolean deleteUser(String username) {
        if (!userRepository.existsById(username)) {
            log.warn("Tentative de suppression d'un utilisateur inexistant: {}", username);
            return false;
        }

        log.info("Suppression de l'utilisateur: {}", username);
        userRepository.deleteById(username);
        return true;
    }
}
