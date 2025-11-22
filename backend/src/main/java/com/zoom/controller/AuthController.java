package com.zoom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zoom.dto.AuthResponse;
import com.zoom.dto.LoginRequest;
import com.zoom.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur pour l'authentification
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de connexion
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            log.info("📨 POST /api/auth/login - Tentative de connexion pour: {}", request.getUsername());
            AuthResponse response = authService.login(request);
            log.info("✅ POST /api/auth/login - Connexion réussie");
            return ResponseEntity.ok((Object) response);
        } catch (Exception e) {
            log.error("❌ POST /api/auth/login - Erreur: {}", e.getMessage());
            return ResponseEntity.status(401).body((Object) new com.zoom.dto.ErrorResponse("Identifiants incorrects"));
        }
    }

    /**
     * Endpoint pour valider un token
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
