package com.stocksense.report.dto;

import java.math.BigDecimal;

public record TopSellingProductMetric(
        String productCode,
        String productName,
        Long quantitySold,
        BigDecimal totalRevenue
) {
    public TopSellingProductMetric(String productCode, String productName, Number quantitySold, BigDecimal totalRevenue) {
        this(productCode, productName, quantitySold != null ? quantitySold.longValue() : 0L, totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
    }
}
