package com.avijeet.jbank.dtos;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String accountNumber,
        String role
) {
}

