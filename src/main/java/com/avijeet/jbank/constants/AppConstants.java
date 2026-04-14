package com.avijeet.jbank.constants;

import java.math.BigDecimal;

public class AppConstants {
    private AppConstants() {
    }

    public static final BigDecimal TRANSACTION_FEE = new BigDecimal("5.00");
    public static final BigDecimal MIN_FD_AMOUNT = new BigDecimal("1000.00");
    public static final BigDecimal SAVINGS_MIN_BALANCE = new BigDecimal("10000.00");
    public static final double FD_INTEREST_RATE = 0.07;
    public static final long BALANCE_CACHE_TTL_HOURS = 8L;
}
