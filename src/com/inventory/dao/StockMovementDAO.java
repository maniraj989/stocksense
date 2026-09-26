package com.inventory.dao;

import com.inventory.model.StockMovement;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StockMovementDAO {
    public void addMovement(StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reference_id, movement_date, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, movement.getProductId());
            statement.setString(2, movement.getMovementType());
            statement.setInt(3, movement.getQuantity());
            statement.setInt(4, movement.getReferenceId());
            statement.setTimestamp(5, Timestamp.valueOf(movement.getMovementDate()));
            statement.setInt(6, movement.getCreatedBy());
            statement.executeUpdate();
        }
    }

    public List<StockMovement> getMovementHistory(int productId) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY movement_date DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = new StockMovement();
                    movement.setId(rs.getInt("id"));
                    movement.setProductId(rs.getInt("product_id"));
                    movement.setMovementType(rs.getString("movement_type"));
                    movement.setQuantity(rs.getInt("quantity"));
                    movement.setReferenceId(rs.getInt("reference_id"));
                    movement.setMovementDate(rs.getTimestamp("movement_date").toLocalDateTime());
                    movement.setCreatedBy(rs.getInt("created_by"));
                    list.add(movement);
                }
            }
        }
        return list;
    }
}
