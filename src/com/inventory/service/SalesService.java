package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.exception.InsufficientStockException;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SalesService implements ReportGenerator {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    public int createSale(List<SaleItem> saleItems, int createdBy) throws SQLException, InsufficientStockException {
        if (saleItems == null || saleItems.isEmpty()) {
            throw new IllegalArgumentException("No sale items selected.");
        }

        double totalAmount = 0.0;
        for (SaleItem item : saleItems) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("One or more products were not found.");
            }
            if (item.getQuantity() > product.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            item.setUnitPrice(product.getSellingPrice());
            item.setSubtotal(product.getSellingPrice() * item.getQuantity());
            totalAmount += item.getSubtotal();
        }

        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setTotalAmount(totalAmount);
        sale.setCreatedBy(createdBy);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int saleId = saleDAO.createSale(sale, connection);
                for (SaleItem item : saleItems) {
                    item.setSaleId(saleId);
                    saleDAO.addSaleItem(item, connection);

                    Product product = productDAO.getProductById(item.getProductId(), connection);
                    if (product != null) {
                        product.setQuantity(product.getQuantity() - item.getQuantity());
                        productDAO.updateProduct(product, connection);

                        String movementSql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reference_id, movement_date, created_by) VALUES (?, 'OUT', ?, ?, ?, ?)";
                        try (PreparedStatement movementStmt = connection.prepareStatement(movementSql)) {
                            movementStmt.setInt(1, product.getId());
                            movementStmt.setInt(2, item.getQuantity());
                            movementStmt.setInt(3, saleId);
                            movementStmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                            movementStmt.setInt(5, createdBy);
                            movementStmt.executeUpdate();
                        }
                    }
                }
                connection.commit();
                return saleId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public double getTodaySales() {
        try {
            return saleDAO.getTodaySales();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public double getWeeklySales() {
        try {
            return saleDAO.getWeeklySales();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public double getMonthlySales() {
        try {
            return saleDAO.getMonthlySales();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public double getTotalRevenue() {
        try {
            return saleDAO.getTotalRevenue();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
