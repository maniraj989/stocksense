package com.inventory.service;

public interface ReportGenerator {
    double getTodaySales();
    double getWeeklySales();
    double getMonthlySales();
    double getTotalRevenue();
}
