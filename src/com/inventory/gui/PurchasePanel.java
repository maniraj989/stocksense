package com.inventory.gui;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.model.Product;
import com.inventory.model.PurchaseItem;
import com.inventory.model.Supplier;
import com.inventory.service.PurchaseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PurchasePanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final PurchaseService purchaseService = new PurchaseService();
    private final JComboBox<Supplier> supplierCombo = new JComboBox<>();
    private final JComboBox<Product> productCombo = new JComboBox<>();
    private final JTextField quantityField = new JTextField();
    private final JTable purchaseTable = new JTable();
    private final List<PurchaseItem> pendingItems = new ArrayList<>();
    private final int currentUserId;

    public PurchasePanel() {
        this(1);
    }

    public PurchasePanel(int currentUserId) {
        this.currentUserId = currentUserId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loadSuppliers();
        loadProducts();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Supplier")); formPanel.add(supplierCombo);
        formPanel.add(new JLabel("Product")); formPanel.add(productCombo);
        formPanel.add(new JLabel("Quantity")); formPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Item");
        JButton saveButton = new JButton("Save Purchase");
        buttonPanel.add(addButton); buttonPanel.add(saveButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(purchaseTable), BorderLayout.SOUTH);

        addButton.addActionListener(e -> addPurchaseItem());
        saveButton.addActionListener(e -> savePurchase());
        refreshPurchaseTable();
    }

    private void loadSuppliers() {
        try {
            supplierCombo.removeAllItems();
            for (Supplier supplier : supplierDAO.getAllSuppliers()) {
                supplierCombo.addItem(supplier);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load suppliers.");
        }
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

    private void addPurchaseItem() {
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
            PurchaseItem item = new PurchaseItem();
            item.setProductId(product.getId());
            item.setQuantity(quantity);
            pendingItems.add(item);
            refreshPurchaseTable();
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity.");
        }
    }

    private void savePurchase() {
        Supplier supplier = (Supplier) supplierCombo.getSelectedItem();
        if (supplier == null || pendingItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Choose supplier and add items before saving.");
            return;
        }

        try {
            int purchaseId = purchaseService.createPurchase(supplier.getId(), pendingItems, currentUserId);
            JOptionPane.showMessageDialog(this, "Purchase saved with ID: " + purchaseId);
            pendingItems.clear();
            refreshPurchaseTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Purchase Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPurchaseTable() {
        String[] columns = {"Product ID", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (PurchaseItem item : pendingItems) {
            try {
                Product product = productDAO.getProductById(item.getProductId());
                if (product != null) {
                    model.addRow(new Object[] {
                            product.getName(),
                            item.getQuantity(),
                            product.getPurchasePrice(),
                            product.getPurchasePrice() * item.getQuantity()
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Unable to load product details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        purchaseTable.setModel(model);
    }
}
