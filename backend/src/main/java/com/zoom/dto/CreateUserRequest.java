package com.zoom.dto;

import lombok.*;

/**
 * DTO pour créer un nouvel utilisateur
 * Le mot de passe doit être envoyé en HTTPS seulement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private String username;

    private String password;

    private String role; // ADMIN, USER, etc.
}
