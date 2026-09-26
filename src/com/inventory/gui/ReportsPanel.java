package com.inventory.gui;

import com.inventory.service.ReportService;
import com.inventory.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;

public class ReportsPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private final JLabel totalProductsLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel totalStockLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel lowStockLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel outOfStockLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel todaySalesLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel monthlySalesLabel = new JLabel("", SwingConstants.CENTER);

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);

        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        summaryPanel.add(createCard("Total Products", totalProductsLabel));
        summaryPanel.add(createCard("Total Stock", totalStockLabel));
        summaryPanel.add(createCard("Low Stock", lowStockLabel));
        summaryPanel.add(createCard("Out of Stock", outOfStockLabel));
        summaryPanel.add(createCard("Today's Sales", todaySalesLabel));
        summaryPanel.add(createCard("Monthly Sales", monthlySalesLabel));

        add(buttonPanel, BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        totalProductsLabel.setText(String.valueOf(reportService.getTotalProducts()));
        totalStockLabel.setText(String.valueOf(reportService.getTotalStockUnits()));
        lowStockLabel.setText(String.valueOf(reportService.getLowStockCount()));
        outOfStockLabel.setText(String.valueOf(reportService.getOutOfStockCount()));
        todaySalesLabel.setText(CurrencyUtil.formatInr(reportService.getTodaySales()));
        monthlySalesLabel.setText(CurrencyUtil.formatInr(reportService.getMonthlySales()));
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
