# Phase 8 — Reporting Architecture

## Overview
Phase 8 introduces enterprise-grade reporting for the **STOCKSENSE Smart Inventory Management System**. The reporting architecture generates PDF documents directly in memory using OpenPDF (`com.github.librepdf:openpdf`), avoiding server-side file creation and file system exposure.

## Key PDF Reports

### 1. Sales Invoice / Receipt (`INV-<id>`)
- **Brand Identity**: Stocksense blue header (`#1e3a8a`), metadata summary table, line-item grid, and grand totals.
- **Fields Displayed**:
  - Invoice Number (`INV-<id>`)
  - Sale Date & Time
  - Cashier / Staff who processed the transaction
  - Status (`COMPLETED`)
  - Itemized table: Product SKU/Code, Name, Quantity, Unit Price, Subtotal
  - Grand Total
- **Data Safety**: Only uses existing attributes from `Sale` and `SaleItem`. No invented tax or payment fields.
- **Access Points**:
  - Sales History view: Row-level "Invoice PDF" button and Receipt Dialog "Download Invoice PDF" button.
  - Reports view: Sales Report tab.

### 2. Purchase Order PDF (`PO-<id>`)
- **Header**: STOCKSENSE Procurement branding, PO Number, Order Date, Supplier Name, Created By Staff.
- **Items Grid**: Product Code, Product Name, Quantity Ordered, Unit Purchase Price, Line Total.
- **Summary**: Total Order Expenditure.
- **Access Points**:
  - Purchases view: Row action "Purchase PDF" and Dialog "Download Purchase PDF" button.
  - Reports view: Procurement Analytics tab.

### 3. Inventory Status & Valuation PDF
- **Orientation**: Landscape (`PageSize.A4.rotate()`) for readability.
- **Header & KPI Summary**: Total Inventory Cost Value, Potential Revenue, Total Units in Stock, Catalog Product Count.
- **Catalog Grid**: SKU / Code, Product Name, Category, Stock Qty, Unit Cost, Total Cost Value, Stock Status (`OPTIMAL`, `LOW_STOCK`, `OUT_OF_STOCK`).
- **Access Point**: Reports view: Inventory Valuation tab.

## Technical Implementation
- **Library**: OpenPDF 1.3.40 (LGPL/MPL compliant, pure Java).
- **Service**: `com.stocksense.report.service.PdfReportService`.
- **In-Memory Streaming**: Streamed to clients via Vaadin `StreamResource` and `Anchor` components with `download` attributes.
- **Exception Handling**: Generates graceful errors; no raw stack traces exposed to end-users.
