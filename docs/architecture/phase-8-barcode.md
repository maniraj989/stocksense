# Phase 8 — Barcode Architecture & POS Integration

## Overview
Phase 8 integrates high-density **Code 128** linear barcode generation, display, downloading, and scanning lookup across StockSense using the ZXing ("Zebra Crossing") library.

## Barcode Specifications
- **Standard**: GS1 / AIM Code 128 (high-density alphanumeric symbology).
- **Identifier**: Uses the existing database `Product.productCode` (SKU) as the barcode payload.
- **Rendering**: In-memory PNG byte stream rendered via `MatrixToImageWriter`.
- **Validation**:
  - Null/empty check.
  - Alphanumeric + standard ASCII (0x20 to 0x7E).
  - Maximum 50 characters length constraint.

## Service Layer (`BarcodeService`)
- `generateBarcodePng(String productCode, int width, int height)`: Produces valid PNG magic byte array (`0x89 0x50 0x4E 0x47`).
- `isValidBarcode(String productCode)`: Verifies symbology validity.
- `findProductByBarcode(String barcode)`: Direct database lookup via `productRepository.findByProductCode(...)`.

## User Interface Integrations

### 1. Product Catalog View (`ProductsView`)
- Added row action button **"Barcode"** (`VaadinIcon.BARCODE`).
- **Modal Dialog**:
  - Displays product code, product name, and current stock.
  - Dynamically renders generated barcode image inline.
  - Provides a **"Download Barcode PNG"** button for printable label export.

### 2. POS Terminal (`SalesPosView`)
- Added **"Barcode / SKU Scan"** input field with search button and Enter key listener.
- **Workflow**:
  1. Cashier scans barcode via standard USB/Bluetooth barcode scanner or types SKU manually.
  2. Submits on Enter key.
  3. Product is looked up directly in PostgreSQL database.
  4. Automatically selects product in dropdown, displays available stock badge, and sets unit selling price.
  5. Automatically focuses quantity field for instant order entry.
  6. Transaction integrity, deduction, and stock movements remain strictly managed by `SaleService`.
