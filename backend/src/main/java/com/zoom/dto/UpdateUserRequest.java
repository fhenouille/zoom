package com.zoom.dto;

import lombok.*;

/**
 * DTO pour mettre à jour un utilisateur
 * Le mot de passe est optionnel (si null, pas de changement)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String role;

    private String password; // Optionnel
}
