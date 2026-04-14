package com.avijeet.jbank.dtos;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}

