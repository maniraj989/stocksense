package com.stocksense.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesAnalyticsSummary(
        BigDecimal totalRevenue,
        Long totalSalesCount,
        BigDecimal averageSaleValue,
        BigDecimal todaySales,
        BigDecimal thisMonthSales,
        BigDecimal previousMonthSales,
        List<TopSellingProductMetric> topSellingProducts
) {}
