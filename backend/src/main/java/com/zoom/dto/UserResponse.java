package com.zoom.dto;

import lombok.*;

/**
 * DTO pour retourner les informations d'un utilisateur
 * N'inclut PAS le mot de passe (jamais envoyé au client)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String username;

    private String role;
}
