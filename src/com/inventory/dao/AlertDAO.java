package com.inventory.dao;

import com.inventory.model.Alert;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    public List<Alert> getAllAlerts() throws SQLException {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alerts ORDER BY created_at DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
        }
        return alerts;
    }

    public int addAlert(Alert alert) throws SQLException {
        String sql = "INSERT INTO alerts (product_id, alert_type, message, severity, created_at, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, alert.getProductId());
            statement.setString(2, alert.getAlertType());
            statement.setString(3, alert.getMessage());
            statement.setString(4, alert.getSeverity());
            statement.setTimestamp(5, Timestamp.valueOf(alert.getCreatedAt()));
            statement.setString(6, alert.getStatus());
            return statement.executeUpdate();
        }
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setId(rs.getInt("id"));
        alert.setProductId(rs.getInt("product_id"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setMessage(rs.getString("message"));
        alert.setSeverity(rs.getString("severity"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            alert.setCreatedAt(createdAt.toLocalDateTime());
        }
        alert.setStatus(rs.getString("status"));
        return alert;
    }
}
