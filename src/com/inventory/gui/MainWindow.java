package com.inventory.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow() {
        super("Smart Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", new DashboardFrame(new com.inventory.model.User(1, "Demo User", "demo", "demo", "ADMIN", "demo@inventory.com", java.time.LocalDateTime.now())));
        tabbedPane.addTab("Products", new ProductPanel());
        tabbedPane.addTab("Suppliers", new SupplierPanel());
        tabbedPane.addTab("Purchases", new PurchasePanel());
        tabbedPane.addTab("Sales", new SalesPanel());
        tabbedPane.addTab("Alerts", new AlertsPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
