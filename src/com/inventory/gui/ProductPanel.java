package com.inventory.gui;

import com.inventory.dao.SupplierDAO;
import com.inventory.exception.InvalidProductException;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductPanel extends JPanel {
    private final InventoryService inventoryService = new InventoryService();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final JTextField codeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(3, 20);
    private final JComboBox<Supplier> supplierCombo = new JComboBox<>();
    private final JTextField purchasePriceField = new JTextField();
    private final JTextField sellingPriceField = new JTextField();
    private final JTextField quantityField = new JTextField();
    private final JTextField minimumStockField = new JTextField();
    private final JTextField maximumStockField = new JTextField();
    private final JTextField expiryField = new JTextField();
    private final JTable productTable = new JTable();
    private final Map<Integer, String> supplierNamesById = new HashMap<>();
    private Integer selectedProductId = null;

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loadSuppliers();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        formPanel.add(new JLabel("Code")); formPanel.add(codeField);
        formPanel.add(new JLabel("Name")); formPanel.add(nameField);
        formPanel.add(new JLabel("Category")); formPanel.add(categoryField);
        formPanel.add(new JLabel("Description")); formPanel.add(new JScrollPane(descriptionArea));
        formPanel.add(new JLabel("Supplier")); formPanel.add(supplierCombo);
        formPanel.add(new JLabel("Purchase Price")); formPanel.add(purchasePriceField);
        formPanel.add(new JLabel("Selling Price")); formPanel.add(sellingPriceField);
        formPanel.add(new JLabel("Quantity")); formPanel.add(quantityField);
        formPanel.add(new JLabel("Min Stock")); formPanel.add(minimumStockField);
        formPanel.add(new JLabel("Max Stock")); formPanel.add(maximumStockField);
        formPanel.add(new JLabel("Expiry (yyyy-MM-dd)")); formPanel.add(expiryField);

        JButton saveButton = new JButton("Save Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton clearButton = new JButton("New / Clear");
        JButton refreshButton = new JButton("Refresh");
        Dimension buttonSize = new Dimension(140, 32);
        for (JButton button : new JButton[] {saveButton, deleteButton, clearButton, refreshButton}) {
            button.setPreferredSize(buttonSize);
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        }
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(saveButton); buttonPanel.add(deleteButton); buttonPanel.add(clearButton); buttonPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Existing Products"));
        tableScrollPane.setPreferredSize(new Dimension(100, 280));
        productTable.setRowHeight(22);
        productTable.setFillsViewportHeight(true);
        productTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        saveButton.addActionListener(e -> saveProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> loadProducts());
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedProductIntoForm();
            }
        });
        loadProducts();
    }

    private void loadSuppliers() {
        try {
            supplierCombo.removeAllItems();
            supplierNamesById.clear();
            List<Supplier> suppliers = supplierDAO.getAllSuppliers();
            for (Supplier supplier : suppliers) {
                supplierCombo.addItem(supplier);
                supplierNamesById.put(supplier.getId(), supplier.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load suppliers.");
        }
    }

    private void loadSelectedProductIntoForm() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = (int) productTable.getModel().getValueAt(row, 0);
        try {
            Product product = inventoryService.getProductById(id);
            selectedProductId = product.getId();
            codeField.setText(product.getProductCode());
            nameField.setText(product.getName());
            categoryField.setText(product.getCategory());
            descriptionArea.setText(product.getDescription());
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                if (supplierCombo.getItemAt(i).getId() == product.getSupplierId()) {
                    supplierCombo.setSelectedIndex(i);
                    break;
                }
            }
            purchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
            sellingPriceField.setText(String.valueOf(product.getSellingPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            minimumStockField.setText(String.valueOf(product.getMinimumStock()));
            maximumStockField.setText(String.valueOf(product.getMaximumStock()));
            expiryField.setText(product.getExpiryDate() == null ? "" : product.getExpiryDate().toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load selected product.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        selectedProductId = null;
        productTable.clearSelection();
        codeField.setText("");
        nameField.setText("");
        categoryField.setText("");
        descriptionArea.setText("");
        if (supplierCombo.getItemCount() > 0) {
            supplierCombo.setSelectedIndex(0);
        }
        purchasePriceField.setText("");
        sellingPriceField.setText("");
        quantityField.setText("");
        minimumStockField.setText("");
        maximumStockField.setText("");
        expiryField.setText("");
    }

    private void saveProduct() {
        try {
            Product product = new Product();
            product.setProductCode(codeField.getText());
            product.setName(nameField.getText());
            product.setCategory(categoryField.getText());
            product.setDescription(descriptionArea.getText());
            Supplier supplier = (Supplier) supplierCombo.getSelectedItem();
            if (supplier != null) {
                product.setSupplierId(supplier.getId());
            }
            product.setPurchasePrice(Double.parseDouble(purchasePriceField.getText()));
            product.setSellingPrice(Double.parseDouble(sellingPriceField.getText()));
            product.setQuantity(Integer.parseInt(quantityField.getText()));
            product.setMinimumStock(Integer.parseInt(minimumStockField.getText()));
            product.setMaximumStock(Integer.parseInt(maximumStockField.getText()));
            if (!expiryField.getText().trim().isEmpty()) {
                product.setExpiryDate(LocalDate.parse(expiryField.getText()));
            }
            if (selectedProductId != null) {
                product.setId(selectedProductId);
                inventoryService.updateProduct(product);
                JOptionPane.showMessageDialog(this, "Product updated successfully.");
            } else {
                inventoryService.addProduct(product);
                JOptionPane.showMessageDialog(this, "Product saved successfully.");
            }
            clearForm();
            loadProducts();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidProductException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Product Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        if (selectedProductId == null) {
            JOptionPane.showMessageDialog(this, "Select a product from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected product? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            inventoryService.deleteProduct(selectedProductId);
            JOptionPane.showMessageDialog(this, "Product deleted successfully.");
            clearForm();
            loadProducts();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to delete product. It may still be referenced by existing purchase or sale records.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = inventoryService.getAllProducts();
            String[] columns = {"ID", "Code", "Name", "Category", "Supplier", "Purchase Price", "Selling Price", "Quantity", "Min Stock", "Max Stock", "Expiry Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (Product product : products) {
                String supplierName = supplierNamesById.getOrDefault(product.getSupplierId(), "Unknown");
                model.addRow(new Object[] {
                        product.getId(),
                        product.getProductCode(),
                        product.getName(),
                        product.getCategory(),
                        supplierName,
                        product.getPurchasePrice(),
                        product.getSellingPrice(),
                        product.getQuantity(),
                        product.getMinimumStock(),
                        product.getMaximumStock(),
                        product.getExpiryDate() == null ? "-" : product.getExpiryDate().toString()
                });
            }
            productTable.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load products.");
        }
    }
}
