package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull UserRole role
) {
}

