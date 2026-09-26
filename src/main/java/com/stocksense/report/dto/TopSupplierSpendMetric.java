package com.stocksense.report.dto;

import java.math.BigDecimal;

public record TopSupplierSpendMetric(
        String supplierName,
        Long orderCount,
        BigDecimal totalSpend
) {
    public TopSupplierSpendMetric(String supplierName, Number orderCount, BigDecimal totalSpend) {
        this(supplierName, orderCount != null ? orderCount.longValue() : 0L, totalSpend != null ? totalSpend : BigDecimal.ZERO);
    }
}
