package com.stocksense.report.dto;

import java.math.BigDecimal;

public record InventoryValuationItem(
        Long productId,
        String productCode,
        String productName,
        String category,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal sellingPrice,
        BigDecimal inventoryValue,
        BigDecimal potentialRevenue,
        String stockStatus
) {}
