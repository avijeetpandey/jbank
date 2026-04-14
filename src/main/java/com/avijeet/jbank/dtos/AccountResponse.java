package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.AccountStatus;
import com.avijeet.jbank.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        BigDecimal minimumBalance
) {
}

