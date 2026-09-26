package com.inventory.service;

import com.inventory.model.Product;

import java.util.List;

public interface StockAnalyzer {
    List<Product> detectLowStockProducts();
    List<Product> detectExpiringSoonProducts(int daysThreshold);
    List<Product> recommendReorderProducts();
}
