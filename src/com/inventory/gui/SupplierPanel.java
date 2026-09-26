package com.inventory.gui;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SupplierPanel extends JPanel {
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final JTable supplierTable = new JTable();
    private final JTextField nameField = new JTextField();
    private final JTextField companyField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextArea addressArea = new JTextArea(3, 20);

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Name")); formPanel.add(nameField);
        formPanel.add(new JLabel("Company")); formPanel.add(companyField);
        formPanel.add(new JLabel("Phone")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Email")); formPanel.add(emailField);
        formPanel.add(new JLabel("Address")); formPanel.add(new JScrollPane(addressArea));

        JButton saveButton = new JButton("Save Supplier");
        JButton refreshButton = new JButton("Refresh");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(saveButton); buttonPanel.add(refreshButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(supplierTable), BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveSupplier());
        refreshButton.addActionListener(e -> loadSuppliers());
        loadSuppliers();
    }

    private void saveSupplier() {
        try {
            Supplier supplier = new Supplier();
            supplier.setName(nameField.getText());
            supplier.setCompany(companyField.getText());
            supplier.setPhone(phoneField.getText());
            supplier.setEmail(emailField.getText());
            supplier.setAddress(addressArea.getText());
            supplierDAO.addSupplier(supplier);
            JOptionPane.showMessageDialog(this, "Supplier added successfully.");
            loadSuppliers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to add supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierDAO.getAllSuppliers();
            String[] columns = {"ID", "Name", "Company", "Phone", "Email", "Address"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (Supplier supplier : suppliers) {
                model.addRow(new Object[] {supplier.getId(), supplier.getName(), supplier.getCompany(), supplier.getPhone(), supplier.getEmail(), supplier.getAddress()});
            }
            supplierTable.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Unable to load suppliers.");
        }
    }
}
