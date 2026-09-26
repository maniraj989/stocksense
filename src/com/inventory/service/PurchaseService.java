package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.PurchaseDAO;
import com.inventory.model.Product;
import com.inventory.model.Purchase;
import com.inventory.model.PurchaseItem;
import com.inventory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseService {
    private final ProductDAO productDAO = new ProductDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();

    public int createPurchase(int supplierId, List<PurchaseItem> items, int createdBy) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No purchase items selected.");
        }

        double totalAmount = 0.0;
        for (PurchaseItem item : items) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Product not found for purchase item.");
            }
            item.setUnitPrice(product.getPurchasePrice());
            item.setSubtotal(product.getPurchasePrice() * item.getQuantity());
            totalAmount += item.getSubtotal();
        }

        Purchase purchase = new Purchase();
        purchase.setSupplierId(supplierId);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setTotalAmount(totalAmount);
        purchase.setCreatedBy(createdBy);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int purchaseId = purchaseDAO.createPurchase(purchase, connection);
                for (PurchaseItem item : items) {
                    item.setPurchaseId(purchaseId);
                    purchaseDAO.addPurchaseItem(item, connection);

                    Product product = productDAO.getProductById(item.getProductId(), connection);
                    if (product != null) {
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        productDAO.updateProduct(product, connection);

                        String movementSql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reference_id, movement_date, created_by) VALUES (?, 'IN', ?, ?, ?, ?)";
                        try (PreparedStatement movementStmt = connection.prepareStatement(movementSql)) {
                            movementStmt.setInt(1, product.getId());
                            movementStmt.setInt(2, item.getQuantity());
                            movementStmt.setInt(3, purchaseId);
                            movementStmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                            movementStmt.setInt(5, createdBy);
                            movementStmt.executeUpdate();
                        }
                    }
                }
                connection.commit();
                return purchaseId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
