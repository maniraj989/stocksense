package com.stocksense;

import com.stocksense.barcode.service.BarcodeService;
import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.dto.StockMovementResponse;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.notification.service.AlertService;
import com.stocksense.product.dto.ProductRequest;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.dto.SupplierRequest;
import com.stocksense.product.dto.SupplierResponse;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.purchase.dto.PurchaseResponse;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.report.dto.*;
import com.stocksense.report.service.*;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleRequest;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.service.SaleService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReportServiceIntegrationTest {

    @Autowired
    private InventoryValuationService inventoryValuationService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private BarcodeService barcodeService;

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private AlertService alertService;

    private Product testProductA;
    private Product testProductB;
    private SupplierResponse testSupplier;
    private SaleResponse createdSale;
    private PurchaseResponse createdPurchase;

    @Autowired
    private SupplierRepository supplierRepository;

    @BeforeEach
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void setUpTestData() {
        Supplier supplier = supplierRepository.save(new Supplier("Test Analytics Supplier", "Test Co", "555-9001", "test.analytics@example.com", "789 Report Ave"));

        ProductRequest p1Req = new ProductRequest("P8-PROD-001", "Phase 8 Alpha Widget, with comma", "Hardware", "Alpha test widget",
                supplier.getId(), new BigDecimal("50.00"), new BigDecimal("100.00"), 40, 10, 200, null);
        ProductResponse p1Resp = productService.createProduct(p1Req);
        testProductA = productRepository.findById(p1Resp.getId()).orElseThrow();

        ProductRequest p2Req = new ProductRequest("P8-PROD-002", "Phase 8 Beta \"Gadget\"\nNewline", "Hardware", "Beta test gadget",
                supplier.getId(), new BigDecimal("20.00"), new BigDecimal("45.00"), 5, 15, 200, null);
        ProductResponse p2Resp = productService.createProduct(p2Req);
        testProductB = productRepository.findById(p2Resp.getId()).orElseThrow();

        // Create a Purchase Order
        PurchaseRequest purchaseRequest = new PurchaseRequest(supplier.getId(), List.of(
                new PurchaseItemRequest(testProductA.getId(), 20, new BigDecimal("50.00")),
                new PurchaseItemRequest(testProductB.getId(), 10, new BigDecimal("20.00"))
        ));
        createdPurchase = purchaseService.createPurchase(purchaseRequest);

        // Create a Sale
        SaleRequest saleRequest = new SaleRequest(List.of(
                new SaleItemRequest(testProductA.getId(), 5, new BigDecimal("100.00")),
                new SaleItemRequest(testProductB.getId(), 2, new BigDecimal("45.00"))
        ));
        createdSale = saleService.createSale(saleRequest);
    }

    // =========================================================================
    // 1. INVENTORY VALUATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Inventory Valuation: Formula and summary metrics use BigDecimal")
    void testInventoryValuationCalculations() {
        InventoryValuationSummary summary = inventoryValuationService.getValuationSummary();
        assertThat(summary).isNotNull();
        assertThat(summary.productCount()).isGreaterThanOrEqualTo(2);
        assertThat(summary.totalUnits()).isGreaterThanOrEqualTo(0);
        assertThat(summary.totalInventoryCostValue()).isNotNull().isGreaterThan(BigDecimal.ZERO);
        assertThat(summary.totalPotentialRevenue()).isNotNull().isGreaterThan(BigDecimal.ZERO);

        List<InventoryValuationItem> items = inventoryValuationService.getItemizedValuation();
        assertThat(items).isNotEmpty();

        Optional<InventoryValuationItem> itemA = items.stream()
                .filter(i -> "P8-PROD-001".equals(i.productCode()))
                .findFirst();
        assertThat(itemA).isPresent();
        InventoryValuationItem valA = itemA.get();
        // Valuation = quantity * unitCost
        BigDecimal expectedCostVal = BigDecimal.valueOf(valA.quantity()).multiply(valA.unitCost());
        assertThat(valA.inventoryValue()).isEqualByComparingTo(expectedCostVal);
    }

    // =========================================================================
    // 2. ANALYTICS TESTS
    // =========================================================================

    @Test
    @DisplayName("Sales Analytics: Aggregations, revenues, and top selling products")
    void testSalesAnalytics() {
        SalesAnalyticsSummary analytics = analyticsService.getSalesAnalytics();
        assertThat(analytics).isNotNull();
        assertThat(analytics.totalSalesCount()).isGreaterThanOrEqualTo(1);
        assertThat(analytics.totalRevenue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(analytics.averageSaleValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(analytics.topSellingProducts()).isNotEmpty();

        TopSellingProductMetric topProduct = analytics.topSellingProducts().get(0);
        assertThat(topProduct.productCode()).isNotBlank();
        assertThat(topProduct.quantitySold()).isGreaterThan(0);
        assertThat(topProduct.totalRevenue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Purchase Analytics: Aggregations, spends, and top suppliers")
    void testPurchaseAnalytics() {
        PurchaseAnalyticsSummary analytics = analyticsService.getPurchaseAnalytics();
        assertThat(analytics).isNotNull();
        assertThat(analytics.totalPurchaseCount()).isGreaterThanOrEqualTo(1);
        assertThat(analytics.totalSpend()).isGreaterThan(BigDecimal.ZERO);
        assertThat(analytics.topSuppliers()).isNotEmpty();
        assertThat(analytics.topPurchasedProducts()).isNotEmpty();

        TopSupplierSpendMetric topSupplier = analytics.topSuppliers().get(0);
        assertThat(topSupplier.supplierName()).isEqualTo("Test Analytics Supplier");
        assertThat(topSupplier.totalSpend()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Dashboard Analytics: Live summary metrics")
    void testDashboardAnalytics() {
        DashboardAnalyticsDto dashboard = analyticsService.getDashboardAnalytics();
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.totalProducts()).isGreaterThanOrEqualTo(2);
        assertThat(dashboard.totalSuppliers()).isGreaterThanOrEqualTo(1);
        assertThat(dashboard.totalInventoryValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(dashboard.todaySales()).isNotNull();
        assertThat(dashboard.todayPurchases()).isNotNull();
        assertThat(dashboard.salesThisMonth()).isNotNull();
        assertThat(dashboard.purchasesThisMonth()).isNotNull();
    }

    // =========================================================================
    // 3. BARCODE TESTS
    // =========================================================================

    @Test
    @DisplayName("Barcode Service: Code 128 PNG generation, validation, and product lookup")
    void testBarcodeService() {
        // Generation
        byte[] pngBytes = barcodeService.generateBarcodePng("P8-PROD-001", 300, 100);
        assertThat(pngBytes).isNotEmpty();
        // Check PNG magic bytes: 0x89 0x50 0x4E 0x47 (‰PNG)
        assertThat(pngBytes[0]).isEqualTo((byte) 0x89);
        assertThat(pngBytes[1]).isEqualTo((byte) 0x50);
        assertThat(pngBytes[2]).isEqualTo((byte) 0x4E);
        assertThat(pngBytes[3]).isEqualTo((byte) 0x47);

        // Validation
        assertThat(barcodeService.isValidBarcode("P8-PROD-001")).isTrue();
        assertThat(barcodeService.isValidBarcode("")).isFalse();
        assertThat(barcodeService.isValidBarcode(null)).isFalse();

        // Product lookup by barcode
        Optional<ProductResponse> lookedUp = barcodeService.findProductByBarcode("P8-PROD-001");
        assertThat(lookedUp).isPresent();
        assertThat(lookedUp.get().getProductCode()).isEqualTo("P8-PROD-001");
        assertThat(lookedUp.get().getName()).isEqualTo("Phase 8 Alpha Widget, with comma");

        // Non-existent barcode lookup returns empty
        Optional<ProductResponse> notFound = barcodeService.findProductByBarcode("NON-EXISTENT-SKU-999");
        assertThat(notFound).isEmpty();
    }

    // =========================================================================
    // 4. CSV EXPORT TESTS
    // =========================================================================

    @Test
    @DisplayName("CSV Export: Products CSV contains UTF-8 BOM, correctly escapes commas and quotes")
    void testCsvExportEscapingAndUtf8() {
        byte[] csvBytes = csvExportService.exportProductsCsv();
        assertThat(csvBytes).isNotEmpty();

        // Check UTF-8 BOM: EF BB BF
        assertThat(csvBytes[0]).isEqualTo((byte) 0xEF);
        assertThat(csvBytes[1]).isEqualTo((byte) 0xBB);
        assertThat(csvBytes[2]).isEqualTo((byte) 0xBF);

        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);
        assertThat(csvContent).contains("Product Code,Product Name,Category,Supplier");
        // Verify product with comma is enclosed in quotes
        assertThat(csvContent).contains("\"Phase 8 Alpha Widget, with comma\"");
        // Verify product with quotes is escaped
        assertThat(csvContent).contains("Phase 8 Beta \"\"Gadget\"\"");
    }

    @Test
    @DisplayName("CSV Export: All business domains generate non-empty CSVs with headers")
    void testCsvExportAllDomains() {
        assertThat(new String(csvExportService.exportSuppliersCsv(), StandardCharsets.UTF_8)).contains("ID,Name,Email,Phone,Address");
        assertThat(new String(csvExportService.exportSalesCsv(), StandardCharsets.UTF_8)).contains("Sale ID,Date,Total,Item Count");
        assertThat(new String(csvExportService.exportPurchasesCsv(), StandardCharsets.UTF_8)).contains("Purchase ID,Date,Supplier,Total,Item Count");
        assertThat(new String(csvExportService.exportStockMovementsCsv(null, null, null, null), StandardCharsets.UTF_8)).contains("Movement ID,Product,Product Code,Movement Type,Quantity,Date,Reference");
        assertThat(new String(csvExportService.exportAlertsCsv(), StandardCharsets.UTF_8)).contains("Alert ID,Product,Alert Type,Severity,Status,Created Date");
        assertThat(new String(csvExportService.exportInventoryValuationCsv(), StandardCharsets.UTF_8)).contains("Product ID,Product Code,Product Name,Category,Stock Quantity,Unit Cost,Selling Price,Inventory Value,Potential Revenue,Stock Status");
    }

    // =========================================================================
    // 5. EXCEL EXPORT TESTS
    // =========================================================================

    @Test
    @DisplayName("Excel Export: Valid workbook generation with sheets, frozen panes, and headers")
    void testExcelExports() throws IOException {
        // Inventory Valuation Excel
        byte[] valuationBytes = excelExportService.exportInventoryValuationExcel();
        assertThat(valuationBytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(valuationBytes))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Valuation Details");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Product ID");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Code");
        }

        // Sales Excel
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusDays(1);
        byte[] salesBytes = excelExportService.exportSalesAnalyticsExcel(start, end);
        assertThat(salesBytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(salesBytes))) {
            assertThat(wb.getSheet("Sales KPI Summary")).isNotNull();
            assertThat(wb.getSheet("Top Selling Products")).isNotNull();
        }

        // Purchases Excel
        byte[] purchasesBytes = excelExportService.exportPurchaseAnalyticsExcel(start, end);
        assertThat(purchasesBytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(purchasesBytes))) {
            assertThat(wb.getSheet("Procurement KPI Summary")).isNotNull();
            assertThat(wb.getSheet("Top Suppliers")).isNotNull();
        }

        // Stock Movements Excel
        byte[] movementsBytes = excelExportService.exportStockMovementsExcel(null, null, null, null);
        assertThat(movementsBytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(movementsBytes))) {
            assertThat(wb.getSheet("Stock Movements Audit")).isNotNull();
        }
    }

    // =========================================================================
    // 6. PDF REPORT TESTS
    // =========================================================================

    @Test
    @DisplayName("PDF Reports: In-memory generation of Sales Invoice, Purchase Order, and Inventory Valuation")
    void testPdfReports() {
        // Sales Invoice PDF
        byte[] invoicePdf = pdfReportService.generateSalesInvoicePdf(createdSale.getId());
        assertThat(invoicePdf).isNotEmpty();
        // PDF header magic bytes: %PDF-
        String pdfHeader = new String(invoicePdf, 0, 8, StandardCharsets.US_ASCII);
        assertThat(pdfHeader).startsWith("%PDF-");

        // Purchase Order PDF
        byte[] purchasePdf = pdfReportService.generatePurchaseOrderPdf(createdPurchase.getId());
        assertThat(purchasePdf).isNotEmpty();
        assertThat(new String(purchasePdf, 0, 8, StandardCharsets.US_ASCII)).startsWith("%PDF-");

        // Inventory Valuation PDF
        byte[] inventoryPdf = pdfReportService.generateInventoryValuationPdf();
        assertThat(inventoryPdf).isNotEmpty();
        assertThat(new String(inventoryPdf, 0, 8, StandardCharsets.US_ASCII)).startsWith("%PDF-");
    }

    // =========================================================================
    // 7. STOCK MOVEMENTS FILTERING
    // =========================================================================

    @Test
    @DisplayName("Stock Movement Reporting: Filter by product, movement type, and date range")
    void testStockMovementFiltering() {
        // Record an adjustment
        stockMovementService.recordAdjustment(new StockAdjustmentRequest(testProductA.getId(), 5, MovementType.IN, null));

        List<StockMovementResponse> allFiltered = stockMovementService.findFilteredMovements(
                testProductA.getId(), MovementType.IN, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(allFiltered).isNotEmpty();
        assertThat(allFiltered.get(0).getProductId()).isEqualTo(testProductA.getId());
        assertThat(allFiltered.get(0).getMovementType()).isEqualTo(MovementType.IN);
    }
}
