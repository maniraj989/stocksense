package com.stocksense.report.dto;

import java.math.BigDecimal;

public record TopPurchasedProductMetric(
        String productCode,
        String productName,
        Long quantityPurchased,
        BigDecimal totalSpend
) {
    public TopPurchasedProductMetric(String productCode, String productName, Number quantityPurchased, BigDecimal totalSpend) {
        this(productCode, productName, quantityPurchased != null ? quantityPurchased.longValue() : 0L, totalSpend != null ? totalSpend : BigDecimal.ZERO);
    }
}
