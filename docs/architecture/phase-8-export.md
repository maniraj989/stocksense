# Phase 8 — Data Export Architecture (CSV & Excel)

## Overview
Phase 8 delivers multi-format data export capabilities (CSV and Excel `.xlsx`) for all operational domains in StockSense, enabling offline analysis, spreadsheet modeling, and audit compliance.

## CSV Export (`CsvExportService`)
- **Library**: Apache Commons CSV (`org.apache.commons:commons-csv`).
- **Encoding**: UTF-8 with Byte Order Mark (BOM: `0xEF 0xBB 0xBF`) to guarantee immediate double-click opening in Microsoft Excel without character encoding corruption.
- **Escaping**: Fully complies with RFC 4180:
  - Strings with commas, newlines, or double quotes are automatically quoted and escaped (`""`).
- **Domain Exports**:
  - `Products`: ID, Product Code, Product Name, Category, Supplier, Price, Stock, Reorder Level.
  - `Suppliers`: ID, Name, Email, Phone, Address.
  - `Sales`: Sale ID, Date, Total, Item Count.
  - `Purchases`: Purchase ID, Date, Supplier, Total, Item Count.
  - `Stock Movements`: Movement ID, Product, Product Code, Movement Type, Quantity, Date, Reference.
  - `Alerts`: Alert ID, Product, Alert Type, Severity, Status, Created Date.
  - `Inventory Valuation`: Product ID, Product Code, Product Name, Category, Stock Quantity, Unit Cost, Selling Price, Inventory Value, Potential Revenue, Stock Status.

## Excel Export (`ExcelExportService`)
- **Library**: Apache POI 5.3.0 (`org.apache.poi:poi`, `poi-ooxml`).
- **Format**: Native OpenXML Workbook (`.xlsx`).
- **Design & Layout**:
  - Frozen top header row on all worksheets for continuous scrolling.
  - Curated styling: `#1e3a8a` navy header fill with white bold text.
  - Number formats: Currency `$#,##0.00` applied to all monetary columns; integer formatting applied to stock units.
  - Automatic column width resizing (`autoSizeColumn`).
- **Multi-Sheet Workbooks**:
  - **Inventory Valuation Workbook**:
    - Sheet 1: `Valuation Details` (itemized product catalog valuation).
    - Sheet 2: `Valuation Summary` (KPI summary table).
  - **Sales Analytics Workbook**:
    - Sheet 1: `Sales KPI Summary` (all-time, monthly, daily metrics).
    - Sheet 2: `Top Selling Products` (ranking table).
  - **Procurement Analytics Workbook**:
    - Sheet 1: `Procurement KPI Summary` (all-time, monthly, daily metrics).
    - Sheet 2: `Top Suppliers` (supplier spend ranking).
    - Sheet 3: `Top Purchased Products` (product purchase volume ranking).
  - **Stock Movement Audit Workbook**:
    - Sheet 1: `Stock Movements Audit` (historical movements with filtering).

## Performance & Security
- Streamed directly to client memory as `byte[]`; zero server disk writes or filesystem exposure.
- Filtered queries are evaluated directly in PostgreSQL to prevent memory exhaustion on large datasets.
