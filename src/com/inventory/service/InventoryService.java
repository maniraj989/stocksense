package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.exception.InvalidProductException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryService implements StockAnalyzer {
    private final ProductDAO productDAO = new ProductDAO();

    public void addProduct(Product product) throws InvalidProductException, SQLException {
        validateProduct(product);
        productDAO.addProduct(product);
    }

    public void updateProduct(Product product) throws InvalidProductException, SQLException {
        validateProduct(product);
        productDAO.updateProduct(product);
    }

    public void deleteProduct(int id) throws SQLException {
        productDAO.deleteProduct(id);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int id) throws SQLException, ProductNotFoundException {
        Product product = productDAO.getProductById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        return product;
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productDAO.getAllProducts();
        }
        return productDAO.searchProducts(keyword.trim());
    }

    public void validateProduct(Product product) throws InvalidProductException {
        if (product == null) {
            throw new InvalidProductException("Product details are missing.");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InvalidProductException("Product name is required.");
        }
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            throw new InvalidProductException("Product code is required.");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new InvalidProductException("Category is required.");
        }
        if (product.getPurchasePrice() <= 0) {
            throw new InvalidProductException("Purchase price must be greater than zero.");
        }
        if (product.getSellingPrice() <= 0) {
            throw new InvalidProductException("Selling price must be greater than zero.");
        }
        if (product.getQuantity() < 0) {
            throw new InvalidProductException("Quantity cannot be negative.");
        }
        if (product.getMinimumStock() < 0 || product.getMaximumStock() < 0) {
            throw new InvalidProductException("Stock limits cannot be negative.");
        }
    }

    public int getTotalProducts() throws SQLException {
        return productDAO.getTotalProducts();
    }

    public int getLowStockCount() throws SQLException {
        return productDAO.getLowStockCount();
    }

    public int getTotalStockUnits() throws SQLException {
        return productDAO.getTotalStockUnits();
    }

    public int getOutOfStockCount() throws SQLException {
        return productDAO.getOutOfStockCount();
    }

    public int getExpiringSoonCount(int daysThreshold) throws SQLException {
        return productDAO.getExpiringSoonCount(daysThreshold);
    }

    @Override
    public List<Product> detectLowStockProducts() {
        try {
            return productDAO.getLowStockProducts();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> detectExpiringSoonProducts(int daysThreshold) {
        try {
            return productDAO.getExpiringSoonProducts(daysThreshold);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Product> recommendReorderProducts() {
        List<Product> allProducts = new ArrayList<>();
        try {
            allProducts = productDAO.getAllProducts();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<Product> reorderList = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getQuantity() <= product.getMinimumStock()) {
                reorderList.add(product);
            }
        }
        return reorderList;
    }
}
