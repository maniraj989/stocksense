package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.PurchaseDAO;
import com.inventory.dao.SaleDAO;

public class ReportService implements ReportGenerator {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();

    public int getTotalProducts() {
        try {
            return productDAO.getTotalProducts();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getLowStockCount() {
        try {
            return productDAO.getLowStockCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getTotalStockUnits() {
        try {
            return productDAO.getTotalStockUnits();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getOutOfStockCount() {
        try {
            return productDAO.getOutOfStockCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getExpiringSoonCount(int thresholdDays) {
        try {
            return productDAO.getExpiringSoonCount(thresholdDays);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getTodayPurchaseValue() {
        try {
            return purchaseDAO.getTodayPurchases();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getMonthlyPurchaseValue() {
        try {
            return purchaseDAO.getMonthlyPurchases();
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getTodaySales() {
        try {
            return saleDAO.getTodaySales();
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getWeeklySales() {
        try {
            return saleDAO.getWeeklySales();
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getMonthlySales() {
        try {
            return saleDAO.getMonthlySales();
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getTotalRevenue() {
        try {
            return saleDAO.getTotalRevenue();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
