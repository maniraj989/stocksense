package com.inventory.gui;

import com.inventory.dao.ProductDAO;
import com.inventory.exception.InsufficientStockException;
import com.inventory.model.Product;
import com.inventory.model.SaleItem;
import com.inventory.service.SalesService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final SalesService salesService = new SalesService();
    private final JComboBox<Product> productCombo = new JComboBox<>();
    private final JTextField quantityField = new JTextField();
    private final JTable saleTable = new JTable();
    private final List<SaleItem> pendingSaleItems = new ArrayList<>();
    private final int currentUserId;

    public SalesPanel() {
        this(1);
    }

    public SalesPanel(int currentUserId) {
        this.currentUserId = currentUserId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loadProducts();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Product")); formPanel.add(productCombo);
        formPanel.add(new JLabel("Quantity")); formPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Item");
        JButton saveButton = new JButton("Save Sale");
        buttonPanel.add(addButton); buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(saleTable), BorderLayout.SOUTH);

        addButton.addActionListener(e -> addSaleItem());
        saveButton.addActionListener(e -> saveSale());
        refreshSalesTable();
    }

    private void loadProducts() {
        try {
            productCombo.removeAllItems();
            for (Product product : productDAO.getAllProducts()) {
                productCombo.addItem(product);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load products.");
        }
    }

    private void addSaleItem() {
        Product product = (Product) productCombo.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
            SaleItem item = new SaleItem();
            item.setProductId(product.getId());
            item.setQuantity(quantity);
            pendingSaleItems.add(item);
            refreshSalesTable();
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity.");
        }
    }

    private void saveSale() {
        if (pendingSaleItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one sale item.");
            return;
        }

        try {
            int saleId = salesService.createSale(pendingSaleItems, currentUserId);
            JOptionPane.showMessageDialog(this, "Sale saved successfully with ID: " + saleId);
            pendingSaleItems.clear();
            refreshSalesTable();
        } catch (InsufficientStockException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Stock Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sale Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSalesTable() {
        String[] columns = {"Product", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (SaleItem item : pendingSaleItems) {
            try {
                Product product = productDAO.getProductById(item.getProductId());
                if (product != null) {
                    model.addRow(new Object[] { product.getName(), item.getQuantity(), product.getSellingPrice(), product.getSellingPrice() * item.getQuantity() });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Unable to load product details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        saleTable.setModel(model);
    }
}
