package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must contain letters and numbers")
        String password,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 300) String address,
        @NotBlank @Email String email,
        @NotNull AccountType accountType
) {
}

