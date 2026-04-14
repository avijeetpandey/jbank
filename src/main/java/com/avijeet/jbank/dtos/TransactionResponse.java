package com.avijeet.jbank.dtos;

import com.avijeet.jbank.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String referenceId,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal balanceAfter,
        String counterpartyAccountNumber,
        String remark,
        LocalDateTime createdAt
) {
}

