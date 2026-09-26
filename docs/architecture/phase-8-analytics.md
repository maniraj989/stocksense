# Phase 8 — Analytics & Valuation Architecture

## Overview
Phase 8 implements real-time business intelligence and inventory valuation across sales, procurement, and stock health. All metrics are computed strictly using real PostgreSQL records and authoritative database aggregations (`SUM`, `COUNT`, `GROUP BY`, `ORDER BY`, projections) without loading unbounded tables into memory.

## Monetary Calculations & Accuracy
- All financial metrics strictly use `java.math.BigDecimal` with `RoundingMode.HALF_UP` and a scale of 2 decimal places.
- Primitive `double` and `float` are strictly prohibited to prevent floating-point inaccuracies.

## Inventory Valuation
- **Cost Formula**: `Inventory Cost Value = Product Stock Quantity × Purchase Price`
- **Selling Formula**: `Potential Revenue = Product Stock Quantity × Selling Price`
- **Field Utilization**:
  - `Product.purchasePrice` (`cost`) is used for the true capital inventory valuation.
  - `Product.sellingPrice` is used to project potential gross revenue.
  - No database schema alterations were necessary as both fields existed in the core schema.
- **Summary Metrics**:
  - `Total Inventory Cost Value`: `SELECT COALESCE(SUM(p.quantity * p.purchasePrice), 0) FROM Product p`
  - `Total Potential Revenue`: `SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p`
  - `Total Units in Stock`: `SELECT COALESCE(SUM(p.quantity), 0) FROM Product p`
  - `Low Stock Count`: Count of products where `quantity <= minimumStock` and `quantity > 0`
  - `Out of Stock Count`: Count of products where `quantity = 0`

## Sales Analytics
- **Total Revenue**: All-time sales revenue from `SaleRepository`.
- **Order Count & Average Order Value**: Evaluated via DB aggregation.
- **Time Window Queries**: Today's revenue, current month-to-date, previous calendar month.
- **Top 5 Selling Products**:
  `SELECT si.product.productCode, si.product.name, SUM(si.quantity), SUM(si.subtotal) FROM SaleItem si GROUP BY si.product.id, si.product.productCode, si.product.name ORDER BY SUM(si.quantity) DESC`

## Purchase Analytics
- **Total Spend**: All-time procurement expenditure.
- **Top 5 Suppliers by Spend**:
  `SELECT p.supplier.id, p.supplier.name, COUNT(p.id), SUM(p.totalAmount) FROM Purchase p GROUP BY p.supplier.id, p.supplier.name ORDER BY SUM(p.totalAmount) DESC`
- **Top 5 Purchased Products**:
  `SELECT pi.product.id, pi.product.productCode, pi.product.name, SUM(pi.quantity), SUM(pi.subtotal) FROM PurchaseItem pi GROUP BY pi.product.id, pi.product.productCode, pi.product.name ORDER BY SUM(pi.quantity) DESC`

## Dashboard Integration
- The main `DashboardView` incorporates live KPI StatCards:
  - Inventory Valuation (Cost Value)
  - Today's Sales Revenue
  - Today's Procurement Spend
  - Sales This Month
  - Purchases This Month
  - Low Stock Count
  - Total Products & Suppliers
- Avoids repeated UI re-querying by caching data during the initial view load and user-triggered refresh.
