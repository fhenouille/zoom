package com.zoom.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un utilisateur de l'application
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}
