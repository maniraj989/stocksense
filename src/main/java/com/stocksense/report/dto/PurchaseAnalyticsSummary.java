package com.stocksense.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseAnalyticsSummary(
        BigDecimal totalSpend,
        Long totalPurchaseCount,
        BigDecimal averagePurchaseValue,
        BigDecimal todayPurchases,
        BigDecimal thisMonthPurchases,
        BigDecimal previousMonthPurchases,
        List<TopSupplierSpendMetric> topSuppliers,
        List<TopPurchasedProductMetric> topPurchasedProducts
) {}
