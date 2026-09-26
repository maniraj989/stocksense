package com.stocksense.report.dto;

import java.math.BigDecimal;

public record DashboardAnalyticsDto(
        BigDecimal totalInventoryValue,
        BigDecimal todaySales,
        BigDecimal todayPurchases,
        Long lowStockCount,
        Long totalProducts,
        Long totalSuppliers,
        BigDecimal salesThisMonth,
        BigDecimal purchasesThisMonth
) {}
