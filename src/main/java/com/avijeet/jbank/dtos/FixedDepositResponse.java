package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.FdStatus;
import com.avijeet.jbank.enums.FdTenure;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FixedDepositResponse(
        Long id,
        BigDecimal principalAmount,
        FdTenure tenure,
        BigDecimal interestRate,
        BigDecimal maturityAmount,
        FdStatus status,
        LocalDate startDate,
        LocalDate maturityDate
) {
}

