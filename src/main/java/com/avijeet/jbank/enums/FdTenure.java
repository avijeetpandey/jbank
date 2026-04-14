package com.avijeet.jbank.enums;

public enum FdTenure {
    SIX_MONTHS(6),
    ONE_YEAR(12);

    private final int months;

    FdTenure(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}

