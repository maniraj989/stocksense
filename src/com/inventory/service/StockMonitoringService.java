package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockMonitoringService implements StockAnalyzer {
    private final ProductDAO productDAO = new ProductDAO();

    public void runMonitoring() {
        List<Product> lowStock = detectLowStockProducts();
        List<Product> expiring = detectExpiringSoonProducts(30);
        List<Product> reorder = recommendReorderProducts();

        System.out.println("Low stock count: " + lowStock.size());
        System.out.println("Expiring soon count: " + expiring.size());
        System.out.println("Reorder recommendation count: " + reorder.size());
    }

    @Override
    public List<Product> detectLowStockProducts() {
        try {
            return productDAO.getLowStockProducts();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> detectExpiringSoonProducts(int daysThreshold) {
        try {
            return productDAO.getExpiringSoonProducts(daysThreshold);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> recommendReorderProducts() {
        List<Product> recommendations = new ArrayList<>();
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                if (product.getQuantity() <= product.getMinimumStock() || product.getMaximumStock() > 0 && product.getQuantity() < product.getMaximumStock() * 0.5) {
                    recommendations.add(product);
                }
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return recommendations;
    }

    public double estimateDaysRemaining(Product product) {
        if (product == null || product.getQuantity() <= 0) {
            return 0;
        }
        double avgDailySales = 1.0;
        return product.getQuantity() / avgDailySales;
    }

    public boolean isExpiringSoon(Product product, int thresholdDays) {
        if (product == null || product.getExpiryDate() == null) {
            return false;
        }
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), product.getExpiryDate());
        return daysRemaining >= 0 && daysRemaining <= thresholdDays;
    }
}
