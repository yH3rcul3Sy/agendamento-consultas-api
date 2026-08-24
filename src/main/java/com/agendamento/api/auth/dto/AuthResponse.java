package com.agendamento.api.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String name,
        String email,
        String role
) {
    public AuthResponse(String token, String name, String email, String role) {
        this(token, "Bearer", name, email, role);
    }
}
