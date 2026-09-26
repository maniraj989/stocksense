package com.inventory.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for formatting monetary amounts as Indian Rupees, e.g. "Rupee-sign 21,675.00".
 *
 * The Rupee sign is written in code using a Unicode escape (backslash, letter u, then
 * hex digits 20B9) rather than the literal character. Java processes such escapes as a
 * pre-processing step on the raw (ASCII) source text before any charset-specific
 * decoding is applied to the rest of the file, so this representation compiles
 * correctly regardless of the platform/default charset used to read the .java source
 * (e.g. Cp1252 on Windows), avoiding "mojibake" garbage characters being baked into the
 * compiled class file.
 *
 * The grouping is applied manually (rather than via {@link java.text.DecimalFormat})
 * because {@code DecimalFormat} does not honour a secondary grouping size when
 * formatting, so a pattern such as "#,##,##0.00" still produces Western 3-digit
 * grouping throughout instead of the Indian lakh/crore style (2-2-3) grouping.
 */
public final class CurrencyUtil {

    private static final char RUPEE_SIGN = '\u20B9';

    private CurrencyUtil() {
    }

    /**
     * Formats an amount as an Indian Rupee string using Indian numbering system grouping
     * (last 3 digits, then groups of 2) and exactly 2 decimal places, e.g.
     * {@code formatInr(21675.0)} returns {@code "\u20B9 21,675.00"} and
     * {@code formatInr(1234567.5)} returns {@code "\u20B9 12,34,567.50"}.
     */
    public static String formatInr(double amount) {
        BigDecimal value = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        boolean negative = value.signum() < 0;
        String plain = value.abs().toPlainString();

        int dotIndex = plain.indexOf('.');
        String integerPart = plain.substring(0, dotIndex);
        String fractionPart = plain.substring(dotIndex + 1);

        StringBuilder grouped = new StringBuilder();
        int len = integerPart.length();
        if (len <= 3) {
            grouped.append(integerPart);
        } else {
            // Last 3 digits form the first (rightmost) group.
            grouped.append(integerPart, len - 3, len);
            int remaining = len - 3;
            while (remaining > 0) {
                int start = Math.max(0, remaining - 2);
                grouped.insert(0, ",");
                grouped.insert(0, integerPart, start, remaining);
                remaining = start;
            }
        }

        return RUPEE_SIGN + " " + (negative ? "-" : "") + grouped + "." + fractionPart;
    }
}
