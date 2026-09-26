package com.stocksense.report.dto;

import com.stocksense.inventory.entity.MovementType;

import java.time.LocalDate;

public record StockMovementFilter(
        Long productId,
        MovementType movementType,
        LocalDate dateFrom,
        LocalDate dateTo
) {}
