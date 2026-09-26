package com.inventory.dao;

import com.inventory.model.Purchase;
import com.inventory.model.PurchaseItem;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public int createPurchase(Purchase purchase) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return createPurchase(purchase, connection);
        }
    }

    public int createPurchase(Purchase purchase, Connection connection) throws SQLException {
        String sql = "INSERT INTO purchases (supplier_id, purchase_date, total_amount, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, purchase.getSupplierId());
            statement.setTimestamp(2, Timestamp.valueOf(purchase.getPurchaseDate()));
            statement.setDouble(3, purchase.getTotalAmount());
            statement.setInt(4, purchase.getCreatedBy());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public int addPurchaseItem(PurchaseItem item) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return addPurchaseItem(item, connection);
        }
    }

    public int addPurchaseItem(PurchaseItem item, Connection connection) throws SQLException {
        String sql = "INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getPurchaseId());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.setDouble(4, item.getUnitPrice());
            statement.setDouble(5, item.getSubtotal());
            return statement.executeUpdate();
        }
    }

    public List<Purchase> getAllPurchases() throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases ORDER BY purchase_date DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                purchases.add(mapRow(rs));
            }
        }
        return purchases;
    }

    public double getTodayPurchases() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM purchases WHERE DATE(purchase_date) = CURDATE()";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getMonthlyPurchases() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM purchases WHERE MONTH(purchase_date) = MONTH(CURDATE()) AND YEAR(purchase_date) = YEAR(CURDATE())";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private Purchase mapRow(ResultSet rs) throws SQLException {
        Purchase purchase = new Purchase();
        purchase.setId(rs.getInt("id"));
        purchase.setSupplierId(rs.getInt("supplier_id"));
        purchase.setPurchaseDate(rs.getTimestamp("purchase_date").toLocalDateTime());
        purchase.setTotalAmount(rs.getDouble("total_amount"));
        purchase.setCreatedBy(rs.getInt("created_by"));
        return purchase;
    }
}
