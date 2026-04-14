package com.avijeet.jbank.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 300) String remark
) {
}

