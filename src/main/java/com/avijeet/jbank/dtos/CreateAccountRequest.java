package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull AccountType accountType
) {
}

