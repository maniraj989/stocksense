package com.inventory.gui;

import com.inventory.model.User;
import com.inventory.service.ReportService;
import com.inventory.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final User currentUser;
    private final ReportService reportService = new ReportService();
    private final JLabel overviewTotalProductsLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel overviewLowStockLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel overviewExpiringSoonLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel overviewTodaySalesLabel = new JLabel("", SwingConstants.CENTER);
    private AlertsPanel alertsPanel;
    private ReportsPanel reportsPanel;

    public DashboardFrame(User currentUser) {
        super("Inventory Dashboard");
        this.currentUser = currentUser;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        alertsPanel = new AlertsPanel();
        reportsPanel = new ReportsPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", createOverviewPanel());
        tabs.addTab("Products", new ProductPanel());
        tabs.addTab("Suppliers", new SupplierPanel());
        tabs.addTab("Purchases", new PurchasePanel(currentUser.getId()));
        tabs.addTab("Sales", new SalesPanel(currentUser.getId()));
        tabs.addTab("Alerts", alertsPanel);
        tabs.addTab("Reports", reportsPanel);
        tabs.addChangeListener(e -> {
            String title = tabs.getTitleAt(tabs.getSelectedIndex());
            switch (title) {
                case "Overview":
                    refreshOverview();
                    break;
                case "Alerts":
                    alertsPanel.refresh();
                    break;
                case "Reports":
                    reportsPanel.refresh();
                    break;
                default:
                    break;
            }
        });
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.add(createStatCard("Total Products", overviewTotalProductsLabel));
        cards.add(createStatCard("Low Stock", overviewLowStockLabel));
        cards.add(createStatCard("Expiring Soon", overviewExpiringSoonLabel));
        cards.add(createStatCard("Today's Sales", overviewTodaySalesLabel));
        panel.add(cards, BorderLayout.NORTH);

        JTextArea summaryArea = new JTextArea(
                "- Admin can manage products and suppliers\n" +
                "- Staff can record sales and purchases\n" +
                "- Low stock, expiry and reorder recommendations are monitored in the background\n" +
                "- Database is connected through JDBC and MySQL"
        );
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(245, 245, 245));
        summaryArea.setBorder(BorderFactory.createTitledBorder("System Summary"));
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        refreshOverview();
        return panel;
    }

    private void refreshOverview() {
        overviewTotalProductsLabel.setText(String.valueOf(reportService.getTotalProducts()));
        overviewLowStockLabel.setText(String.valueOf(reportService.getLowStockCount()));
        overviewExpiringSoonLabel.setText(String.valueOf(reportService.getExpiringSoonCount(30)));
        overviewTodaySalesLabel.setText(CurrencyUtil.formatInr(reportService.getTodaySales()));
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        card.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
