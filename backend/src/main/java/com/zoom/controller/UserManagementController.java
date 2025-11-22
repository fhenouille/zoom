package com.zoom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.zoom.dto.*;
import com.zoom.entity.User;
import com.zoom.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur pour la gestion administrative des utilisateurs
 * ⚠️ Seuls les utilisateurs avec le rôle ADMIN peuvent accéder à ces endpoints
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class UserManagementController {

    private static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    private static final String ADMIN_ONLY = "Seuls les administrateurs peuvent accéder à cette ressource";
    private static final String FORBIDDEN_SELF_DELETE = "Vous ne pouvez pas supprimer votre propre compte";
    private static final String USERNAME_REQUIRED = "Le nom d'utilisateur est requis";
    private static final String PASSWORD_REQUIRED = "Le mot de passe est requis";
    private static final String ROLE_REQUIRED = "Le rôle est requis";
    private static final String USER_DELETED = "Utilisateur supprimé avec succès";

    private final AuthService authService;

    /**
     * Récupère le nom d'utilisateur actuel depuis le contexte de sécurité
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Récupère le rôle de l'utilisateur actuel depuis la base de données
     */
    private String getCurrentUserRole() {
        String username = getCurrentUsername();
        User currentUser = authService.getUserByUsername(username);
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Vérifie que l'utilisateur a le rôle ADMIN
     */
    private boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * Crée un nouvel utilisateur
     * POST /api/admin/users
     *
     * @param request Contient: username, password (en clair sur HTTPS), role
     * @return UserResponse sans le mot de passe
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Vérification du rôle ADMIN
            if (!isAdmin()) {
                log.warn("❌ Tentative de création d'utilisateur sans droit ADMIN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ADMIN_ONLY);
            }

            log.info("📝 POST /api/admin/users - Création d'utilisateur: {}", request.getUsername());

            // Validation des entrées
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(USERNAME_REQUIRED);
            }
            if (request.getUsername().length() > 100 || request.getUsername().length() < 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le nom d'utilisateur doit contenir entre 3 et 100 caractères");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(PASSWORD_REQUIRED);
            }
            if (request.getPassword().length() > 255 || request.getPassword().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le mot de passe doit contenir au minimum 8 caractères");
            }
            if (request.getRole() == null || request.getRole().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ROLE_REQUIRED);
            }
            if (!request.getRole().matches("^(USER|ADMIN)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le rôle doit être USER ou ADMIN");
            }

            // Création de l'utilisateur
            User user = authService.createUser(request.getUsername(), request.getPassword(),
                    request.getRole());

            UserResponse response = new UserResponse(user.getUsername(), user.getRole());

            log.info("✅ POST /api/admin/users - Utilisateur créé: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("❌ POST /api/admin/users - Erreur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ POST /api/admin/users - Erreur serveur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur");
        }
    }

    /**
     * Récupère tous les utilisateurs
     * GET /api/admin/users
     *
     * @return Liste des utilisateurs (sans les mots de passe)
     */
    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        try {
            if (!isAdmin()) {
                log.warn("❌ Tentative d'accès à la liste des utilisateurs sans droit ADMIN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ADMIN_ONLY);
            }

            log.info("📋 GET /api/admin/users - Récupération de la liste des utilisateurs");

            List<UserResponse> users = authService.getAllUsers().stream()
                    .map(user -> new UserResponse(user.getUsername(), user.getRole()))
                    .toList();

            log.info("✅ GET /api/admin/users - {} utilisateurs trouvés", users.size());
            return ResponseEntity.ok((Object) users);

        } catch (Exception e) {
            log.error("❌ GET /api/admin/users - Erreur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des utilisateurs");
        }
    }

    /**
     * Récupère un utilisateur spécifique
     * GET /api/admin/users/{username}
     */
    @GetMapping("/{username}")
    public ResponseEntity<Object> getUser(@PathVariable String username) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ADMIN_ONLY);
            }

            log.info("📋 GET /api/admin/users/{} - Récupération de l'utilisateur", username);

            User user = authService.getUserByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(USER_NOT_FOUND);
            }

            UserResponse response = new UserResponse(user.getUsername(), user.getRole());
            return ResponseEntity.ok((Object) response);

        } catch (Exception e) {
            log.error("❌ GET /api/admin/users/{} - Erreur: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de l'utilisateur");
        }
    }

    /**
     * Met à jour un utilisateur
     * PUT /api/admin/users/{username}
     *
     * @param username Identifiant de l'utilisateur à modifier
     * @param request  Contient: role (requis), password (optionnel)
     */
    @PutMapping("/{username}")
    public ResponseEntity<Object> updateUser(@PathVariable String username,
            @RequestBody UpdateUserRequest request) {
        try {
            if (!isAdmin()) {
                log.warn("❌ Tentative de modification d'utilisateur sans droit ADMIN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ADMIN_ONLY);
            }

            log.info("✏️ PUT /api/admin/users/{} - Modification de l'utilisateur", username);

            // Validation
            if (request.getRole() == null || request.getRole().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ROLE_REQUIRED);
            }

            User user = authService.updateUser(username, request.getRole(), request.getPassword());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(USER_NOT_FOUND);
            }

            UserResponse response = new UserResponse(user.getUsername(), user.getRole());

            log.info("✅ PUT /api/admin/users/{} - Utilisateur modifié", username);
            return ResponseEntity.ok((Object) response);

        } catch (Exception e) {
            log.error("❌ PUT /api/admin/users/{} - Erreur: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la modification de l'utilisateur");
        }
    }

    /**
     * Supprime un utilisateur
     * DELETE /api/admin/users/{username}
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Object> deleteUser(@PathVariable String username) {
        try {
            if (!isAdmin()) {
                log.warn("❌ Tentative de suppression d'utilisateur sans droit ADMIN");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ADMIN_ONLY);
            }

            log.info("🗑️ DELETE /api/admin/users/{} - Suppression de l'utilisateur", username);

            // Vérification que l'utilisateur n'essaie pas de se supprimer lui-même
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username.equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(FORBIDDEN_SELF_DELETE);
            }

            boolean deleted = authService.deleteUser(username);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(USER_NOT_FOUND);
            }

            log.info("✅ DELETE /api/admin/users/{} - Utilisateur supprimé", username);
            return ResponseEntity.ok(USER_DELETED);

        } catch (Exception e) {
            log.error("❌ DELETE /api/admin/users/{} - Erreur: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression de l'utilisateur");
        }
    }
}
