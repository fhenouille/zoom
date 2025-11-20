package com.zoom.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
