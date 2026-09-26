package com.stocksense.report.service;

import com.stocksense.inventory.entity.StockMovement;
import com.stocksense.product.entity.Product;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.report.dto.InventoryValuationItem;
import com.stocksense.report.dto.InventoryValuationSummary;
import com.stocksense.report.dto.PurchaseAnalyticsSummary;
import com.stocksense.report.dto.SalesAnalyticsSummary;
import com.stocksense.report.dto.TopPurchasedProductMetric;
import com.stocksense.report.dto.TopSellingProductMetric;
import com.stocksense.report.dto.TopSupplierSpendMetric;
import com.stocksense.sales.entity.Sale;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.sales.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryValuationService inventoryValuationService;
    private final AnalyticsService analyticsService;

    public ExcelExportService(ProductRepository productRepository,
                              SaleRepository saleRepository,
                              PurchaseRepository purchaseRepository,
                              StockMovementRepository stockMovementRepository,
                              InventoryValuationService inventoryValuationService,
                              AnalyticsService analyticsService) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryValuationService = inventoryValuationService;
        this.analyticsService = analyticsService;
    }

    public byte[] exportInventoryValuationExcel() {
        return exportInventoryValuationExcel(inventoryValuationService.getValuationItems(), inventoryValuationService.getValuationSummary());
    }

    public byte[] exportSalesAnalyticsExcel(LocalDate start, LocalDate end) {
        return exportSalesSummaryExcel(analyticsService.getSalesAnalytics());
    }

    public byte[] exportPurchaseAnalyticsExcel(LocalDate start, LocalDate end) {
        return exportPurchaseSummaryExcel(analyticsService.getPurchaseAnalytics());
    }

    public byte[] exportStockMovementsExcel(Long productId, MovementType movementType, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime start = startDate != null ? startDate.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime end = endDate != null ? endDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;
        return exportStockMovementsExcel(stockMovementRepository.findFilteredMovements(productId, movementType, start, end));
    }

    public byte[] exportInventoryExcel(List<Product> products) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventory Catalog");
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            String[] headers = {"ID", "Product Code", "Product Name", "Category", "Supplier", "Purchase Price", "Selling Price", "Quantity", "Min Stock", "Max Stock"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getProductCode());
                row.createCell(2).setCellValue(p.getName());
                row.createCell(3).setCellValue(p.getCategory() != null ? p.getCategory() : "");
                row.createCell(4).setCellValue(p.getSupplier() != null ? p.getSupplier().getName() : "");

                createNumericCell(row, 5, p.getPurchasePrice(), currencyStyle);
                createNumericCell(row, 6, p.getSellingPrice(), currencyStyle);
                createIntegerCell(row, 7, p.getQuantity(), intStyle);
                createIntegerCell(row, 8, p.getMinimumStock(), intStyle);
                createIntegerCell(row, 9, p.getMaximumStock(), intStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export inventory excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportInventoryValuationExcel(List<InventoryValuationItem> items, InventoryValuationSummary summary) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Valuation Details");
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            String[] headers = {"Product ID", "Code", "Name", "Category", "Quantity", "Unit Cost", "Selling Price", "Inventory Value", "Potential Revenue", "Status"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (InventoryValuationItem item : items) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.productId());
                row.createCell(1).setCellValue(item.productCode());
                row.createCell(2).setCellValue(item.productName());
                row.createCell(3).setCellValue(item.category());
                createIntegerCell(row, 4, item.quantity(), intStyle);
                createNumericCell(row, 5, item.unitCost(), currencyStyle);
                createNumericCell(row, 6, item.sellingPrice(), currencyStyle);
                createNumericCell(row, 7, item.inventoryValue(), currencyStyle);
                createNumericCell(row, 8, item.potentialRevenue(), currencyStyle);
                row.createCell(9).setCellValue(item.stockStatus());
            }

            autoSizeColumns(sheet, headers.length);

            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Valuation Summary");
            createHeaderRow(summarySheet, new String[]{"Valuation Metric", "Value"}, headerStyle);
            int sRow = 1;
            addSummaryRow(summarySheet, sRow++, "Total Inventory Value (Cost)", summary.totalInventoryCostValue(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Total Potential Revenue", summary.totalPotentialRevenue(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Total Units in Stock", summary.totalUnits(), intStyle);
            addSummaryRow(summarySheet, sRow++, "Total Catalog Products", summary.productCount(), intStyle);
            addSummaryRow(summarySheet, sRow++, "Low Stock Products", summary.lowStockCount(), intStyle);
            addSummaryRow(summarySheet, sRow++, "Out of Stock Products", summary.outOfStockCount(), intStyle);
            autoSizeColumns(summarySheet, 2);

            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export valuation excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportSalesExcel(List<Sale> sales) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Orders");
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            String[] headers = {"Sale ID", "Date & Time", "Cashier", "Item Count", "Total Amount"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (Sale s : sales) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getSaleDate() != null ? s.getSaleDate().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(s.getCreatedBy() != null ? s.getCreatedBy().getName() : "");
                createIntegerCell(row, 3, s.getItems() != null ? s.getItems().size() : 0, intStyle);
                createNumericCell(row, 4, s.getTotalAmount(), currencyStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export sales excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportSalesSummaryExcel(SalesAnalyticsSummary analytics) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            Sheet summarySheet = workbook.createSheet("Sales KPI Summary");
            createHeaderRow(summarySheet, new String[]{"KPI Metric", "Value"}, headerStyle);
            int sRow = 1;
            addSummaryRow(summarySheet, sRow++, "All-Time Revenue", analytics.totalRevenue(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Total Completed Sales", analytics.totalSalesCount(), intStyle);
            addSummaryRow(summarySheet, sRow++, "Average Order Value", analytics.averageSaleValue(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Today's Revenue", analytics.todaySales(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Current Month Revenue", analytics.thisMonthSales(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Previous Month Revenue", analytics.previousMonthSales(), currencyStyle);
            autoSizeColumns(summarySheet, 2);

            Sheet topProductsSheet = workbook.createSheet("Top Selling Products");
            createHeaderRow(topProductsSheet, new String[]{"Product Code", "Product Name", "Quantity Sold", "Total Revenue"}, headerStyle);
            int pRow = 1;
            for (TopSellingProductMetric m : analytics.topSellingProducts()) {
                Row r = topProductsSheet.createRow(pRow++);
                r.createCell(0).setCellValue(m.productCode());
                r.createCell(1).setCellValue(m.productName());
                createIntegerCell(r, 2, m.quantitySold().intValue(), intStyle);
                createNumericCell(r, 3, m.totalRevenue(), currencyStyle);
            }
            autoSizeColumns(topProductsSheet, 4);

            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export sales summary excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportPurchasesExcel(List<Purchase> purchases) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Purchase Orders");
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            String[] headers = {"Purchase ID", "Date & Time", "Supplier", "Created By", "Item Count", "Total Amount"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (Purchase p : purchases) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getPurchaseDate() != null ? p.getPurchaseDate().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(p.getSupplier() != null ? p.getSupplier().getName() : "");
                row.createCell(3).setCellValue(p.getCreatedBy() != null ? p.getCreatedBy().getName() : "");
                createIntegerCell(row, 4, p.getItems() != null ? p.getItems().size() : 0, intStyle);
                createNumericCell(row, 5, p.getTotalAmount(), currencyStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export purchases excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportPurchaseSummaryExcel(PurchaseAnalyticsSummary analytics) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            Sheet summarySheet = workbook.createSheet("Procurement KPI Summary");
            createHeaderRow(summarySheet, new String[]{"KPI Metric", "Value"}, headerStyle);
            int sRow = 1;
            addSummaryRow(summarySheet, sRow++, "All-Time Procurement Spend", analytics.totalSpend(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Total Completed POs", analytics.totalPurchaseCount(), intStyle);
            addSummaryRow(summarySheet, sRow++, "Average PO Value", analytics.averagePurchaseValue(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Today's Procurement Spend", analytics.todayPurchases(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Current Month Spend", analytics.thisMonthPurchases(), currencyStyle);
            addSummaryRow(summarySheet, sRow++, "Previous Month Spend", analytics.previousMonthPurchases(), currencyStyle);
            autoSizeColumns(summarySheet, 2);

            Sheet topSuppliersSheet = workbook.createSheet("Top Suppliers");
            createHeaderRow(topSuppliersSheet, new String[]{"Supplier Name", "Purchase Orders", "Total Spend"}, headerStyle);
            int supRow = 1;
            for (TopSupplierSpendMetric m : analytics.topSuppliers()) {
                Row r = topSuppliersSheet.createRow(supRow++);
                r.createCell(0).setCellValue(m.supplierName());
                createIntegerCell(r, 1, m.orderCount().intValue(), intStyle);
                createNumericCell(r, 2, m.totalSpend(), currencyStyle);
            }
            autoSizeColumns(topSuppliersSheet, 3);

            Sheet topProductsSheet = workbook.createSheet("Top Purchased Products");
            createHeaderRow(topProductsSheet, new String[]{"Product Code", "Product Name", "Quantity Purchased", "Total Spend"}, headerStyle);
            int pRow = 1;
            for (TopPurchasedProductMetric m : analytics.topPurchasedProducts()) {
                Row r = topProductsSheet.createRow(pRow++);
                r.createCell(0).setCellValue(m.productCode());
                r.createCell(1).setCellValue(m.productName());
                createIntegerCell(r, 2, m.quantityPurchased().intValue(), intStyle);
                createNumericCell(r, 3, m.totalSpend(), currencyStyle);
            }
            autoSizeColumns(topProductsSheet, 4);

            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export purchase summary excel: " + e.getMessage(), e);
        }
    }

    public byte[] exportStockMovementsExcel(List<StockMovement> movements) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock Movements Audit");
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle intStyle = createIntegerStyle(workbook);

            String[] headers = {"ID", "Date & Time", "Product Code", "Product Name", "Movement Type", "Quantity", "Reference ID", "Recorded By"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (StockMovement m : movements) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getMovementDate() != null ? m.getMovementDate().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(m.getProduct() != null ? m.getProduct().getProductCode() : "");
                row.createCell(3).setCellValue(m.getProduct() != null ? m.getProduct().getName() : "");
                row.createCell(4).setCellValue(m.getMovementType() != null ? m.getMovementType().name() : "");
                createIntegerCell(row, 5, m.getQuantity(), intStyle);
                row.createCell(6).setCellValue(m.getReferenceId() != null ? m.getReferenceId().toString() : "-");
                row.createCell(7).setCellValue(m.getCreatedBy() != null ? m.getCreatedBy().getName() : "");
            }

            autoSizeColumns(sheet, headers.length);
            return writeWorkbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export stock movements excel: " + e.getMessage(), e);
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createNumericCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(style);
    }

    private void createIntegerCell(Row row, int col, Integer value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(style);
    }

    private void addSummaryRow(Sheet sheet, int rowIdx, String label, Object value, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        Cell valCell = row.createCell(1);
        if (value instanceof BigDecimal bd) {
            valCell.setCellValue(bd.doubleValue());
        } else if (value instanceof Number n) {
            valCell.setCellValue(n.doubleValue());
        } else if (value != null) {
            valCell.setCellValue(value.toString());
        }
        valCell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createIntegerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth + 1200, 15000));
        }
    }

    private byte[] writeWorkbookToBytes(Workbook workbook) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        return baos.toByteArray();
    }
}
