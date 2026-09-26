package com.stocksense.report.dto;

import java.math.BigDecimal;

public record InventoryValuationSummary(
        BigDecimal totalInventoryCostValue,
        BigDecimal totalPotentialRevenue,
        Long totalUnits,
        Long productCount,
        Long lowStockCount,
        Long outOfStockCount
) {}
