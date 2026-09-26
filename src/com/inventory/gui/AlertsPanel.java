package com.inventory.gui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AlertsPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final JTable alertTable = new JTable();

    public AlertsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAlerts());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(alertTable), BorderLayout.CENTER);
        loadAlerts();
    }

    public void refresh() {
        loadAlerts();
    }

    private void loadAlerts() {
        String[] columns = {"Product", "Type", "Message", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                if (product.isLowStock()) {
                    model.addRow(new Object[] { product.getName(), "LOW_STOCK", "Current stock " + product.getQuantity() + " is below minimum " + product.getMinimumStock(), "OPEN" });
                }
                if (product.getExpiryDate() != null) {
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), product.getExpiryDate());
                    if (daysLeft <= 30) {
                        model.addRow(new Object[] { product.getName(), "EXPIRY", "Expires in " + daysLeft + " days", "OPEN" });
                    }
                }
            }
            alertTable.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load alerts.");
        }
    }
}
