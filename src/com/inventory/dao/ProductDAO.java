package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DBConnection;
import com.inventory.util.DateUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public int addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_code, name, category, description, supplier_id, purchase_price, selling_price, quantity, minimum_stock, maximum_stock, expiry_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getProductCode());
            statement.setString(2, product.getName());
            statement.setString(3, product.getCategory());
            statement.setString(4, product.getDescription());
            statement.setInt(5, product.getSupplierId());
            statement.setDouble(6, product.getPurchasePrice());
            statement.setDouble(7, product.getSellingPrice());
            statement.setInt(8, product.getQuantity());
            statement.setInt(9, product.getMinimumStock());
            statement.setInt(10, product.getMaximumStock());
            if (product.getExpiryDate() != null) {
                statement.setDate(11, Date.valueOf(product.getExpiryDate()));
            } else {
                statement.setNull(11, java.sql.Types.DATE);
            }
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(12, now);
            statement.setTimestamp(13, now);
            return statement.executeUpdate();
        }
    }

    public int updateProduct(Product product) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return updateProduct(product, connection);
        }
    }

    public int updateProduct(Product product, Connection connection) throws SQLException {
        String sql = "UPDATE products SET product_code = ?, name = ?, category = ?, description = ?, supplier_id = ?, purchase_price = ?, selling_price = ?, quantity = ?, minimum_stock = ?, maximum_stock = ?, expiry_date = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getProductCode());
            statement.setString(2, product.getName());
            statement.setString(3, product.getCategory());
            statement.setString(4, product.getDescription());
            statement.setInt(5, product.getSupplierId());
            statement.setDouble(6, product.getPurchasePrice());
            statement.setDouble(7, product.getSellingPrice());
            statement.setInt(8, product.getQuantity());
            statement.setInt(9, product.getMinimumStock());
            statement.setInt(10, product.getMaximumStock());
            if (product.getExpiryDate() != null) {
                statement.setDate(11, Date.valueOf(product.getExpiryDate()));
            } else {
                statement.setNull(11, java.sql.Types.DATE);
            }
            statement.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(13, product.getId());
            return statement.executeUpdate();
        }
    }

    public int deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    public Product getProductById(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return getProductById(id, connection);
        }
    }

    public Product getProductById(int id, Connection connection) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY name";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        }
        return products;
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE LOWER(p.name) LIKE ? OR LOWER(p.product_code) LIKE ? OR LOWER(p.category) LIKE ? OR LOWER(s.name) LIKE ? OR LOWER(s.company) LIKE ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            statement.setString(1, likeKeyword);
            statement.setString(2, likeKeyword);
            statement.setString(3, likeKeyword);
            statement.setString(4, likeKeyword);
            statement.setString(5, likeKeyword);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        }
        return products;
    }

    public List<Product> getLowStockProducts() throws SQLException {
        String sql = "SELECT * FROM products WHERE quantity <= minimum_stock ORDER BY quantity ASC";
        return getProductsByQuery(sql);
    }

    public List<Product> getExpiringSoonProducts(int daysThreshold) throws SQLException {
        String sql = "SELECT * FROM products WHERE expiry_date IS NOT NULL AND DATEDIFF(expiry_date, CURDATE()) <= ? AND DATEDIFF(expiry_date, CURDATE()) >= 0 ORDER BY expiry_date ASC";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, daysThreshold);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        }
        return products;
    }

    public List<Product> getProductsByQuery(String sql) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        }
        return products;
    }

    public Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setProductCode(rs.getString("product_code"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setDescription(rs.getString("description"));
        product.setSupplierId(rs.getInt("supplier_id"));
        product.setPurchasePrice(rs.getDouble("purchase_price"));
        product.setSellingPrice(rs.getDouble("selling_price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setMinimumStock(rs.getInt("minimum_stock"));
        product.setMaximumStock(rs.getInt("maximum_stock"));

        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            product.setExpiryDate(expiryDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return product;
    }

    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getTotalStockUnits() throws SQLException {
        String sql = "SELECT SUM(quantity) FROM products";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= minimum_stock";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOutOfStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= 0";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getExpiringSoonCount(int daysThreshold) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE expiry_date IS NOT NULL AND DATEDIFF(expiry_date, CURDATE()) <= ? AND DATEDIFF(expiry_date, CURDATE()) >= 0";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, daysThreshold);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
