package com.avijeet.jbank.utils;

import java.security.SecureRandom;

public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountNumberGenerator() {
    }

    public static String generate() {
        long value = 100000000000L + (Math.abs(RANDOM.nextLong()) % 900000000000L);
        return String.valueOf(value);
    }
}

