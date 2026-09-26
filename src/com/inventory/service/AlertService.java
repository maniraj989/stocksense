package com.inventory.service;

import com.inventory.dao.AlertDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Alert;
import com.inventory.model.Product;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertService {
    private final AlertDAO alertDAO = new AlertDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public List<Alert> getCurrentAlerts() {
        List<Alert> alerts = new ArrayList<>();
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                if (product.isLowStock()) {
                    alerts.add(new Alert(0, product.getId(), "LOW_STOCK",
                            "Low stock: " + product.getName() + " has only " + product.getQuantity() + " units left.",
                            "HIGH", LocalDateTime.now(), "OPEN"));
                }
                if (product.getExpiryDate() != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), product.getExpiryDate());
                    if (days <= 30) {
                        alerts.add(new Alert(0, product.getId(), "EXPIRY",
                                "Expiry warning: " + product.getName() + " expires in " + days + " days.",
                                "MEDIUM", LocalDateTime.now(), "OPEN"));
                    }
                }
            }
            return alerts;
        } catch (SQLException e) {
            return alerts;
        }
    }
}
