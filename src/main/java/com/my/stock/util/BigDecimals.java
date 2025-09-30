package com.my.stock.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BigDecimals {
    private BigDecimals() {}

    public static BigDecimal scale(BigDecimal v, int s) {
        if (v == null) return null;
        return v.setScale(s, RoundingMode.HALF_UP);
    }

    public static BigDecimal safeDivide(BigDecimal a, BigDecimal b, int s) {
        if (a == null || b == null || b.signum() == 0) return BigDecimal.ZERO;
        return a.divide(b, s, RoundingMode.HALF_UP);
    }
}


