package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.FdTenure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FixedDepositRequest(
        @NotNull @DecimalMin(value = "1000.00") BigDecimal amount,
        @NotNull FdTenure tenure
) {
}

