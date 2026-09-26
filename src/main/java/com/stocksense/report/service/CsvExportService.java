package com.stocksense.report.service;

import com.stocksense.inventory.entity.StockMovement;
import com.stocksense.notification.entity.Alert;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.report.dto.InventoryValuationItem;
import com.stocksense.sales.entity.Sale;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.notification.repository.AlertRepository;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.sales.repository.SaleRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AlertRepository alertRepository;
    private final InventoryValuationService inventoryValuationService;

    public CsvExportService(ProductRepository productRepository,
                            SupplierRepository supplierRepository,
                            SaleRepository saleRepository,
                            PurchaseRepository purchaseRepository,
                            StockMovementRepository stockMovementRepository,
                            AlertRepository alertRepository,
                            InventoryValuationService inventoryValuationService) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.alertRepository = alertRepository;
        this.inventoryValuationService = inventoryValuationService;
    }

    public byte[] exportProductsCsv() {
        return exportProductsCsv(productRepository.findAll());
    }

    public byte[] exportSuppliersCsv() {
        return exportSuppliersCsv(supplierRepository.findAll());
    }

    public byte[] exportSalesCsv() {
        return exportSalesCsv(saleRepository.findAll());
    }

    public byte[] exportPurchasesCsv() {
        return exportPurchasesCsv(purchaseRepository.findAll());
    }

    public byte[] exportStockMovementsCsv(Long productId, MovementType movementType, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime start = startDate != null ? startDate.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime end = endDate != null ? endDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;
        return exportStockMovementsCsv(stockMovementRepository.findFilteredMovements(productId, movementType, start, end));
    }

    public byte[] exportAlertsCsv() {
        return exportAlertsCsv(alertRepository.findAll());
    }

    public byte[] exportInventoryValuationCsv() {
        return exportValuationCsv(inventoryValuationService.getValuationItems());
    }

    public byte[] exportProductsCsv(List<Product> products) {
        return writeCsv(printer -> {
            printer.printRecord("ID", "Product Code", "Product Name", "Category", "Supplier", "Price", "Stock", "Reorder Level");
            for (Product p : products) {
                printer.printRecord(
                        p.getId(),
                        p.getProductCode(),
                        p.getName(),
                        p.getCategory() != null ? p.getCategory() : "",
                        p.getSupplier() != null ? p.getSupplier().getName() : "",
                        p.getSellingPrice(),
                        p.getQuantity(),
                        p.getMinimumStock()
                );
            }
        });
    }

    public byte[] exportSuppliersCsv(List<Supplier> suppliers) {
        return writeCsv(printer -> {
            printer.printRecord("ID", "Name", "Email", "Phone", "Address");
            for (Supplier s : suppliers) {
                printer.printRecord(
                        s.getId(),
                        s.getName(),
                        s.getEmail() != null ? s.getEmail() : "",
                        s.getPhone() != null ? s.getPhone() : "",
                        s.getAddress() != null ? s.getAddress() : ""
                );
            }
        });
    }

    public byte[] exportSalesCsv(List<Sale> sales) {
        return writeCsv(printer -> {
            printer.printRecord("Sale ID", "Date", "Total", "Item Count");
            for (Sale s : sales) {
                printer.printRecord(
                        s.getId(),
                        s.getSaleDate() != null ? s.getSaleDate().format(DATE_FORMATTER) : "",
                        s.getTotalAmount(),
                        s.getItems() != null ? s.getItems().size() : 0
                );
            }
        });
    }

    public byte[] exportPurchasesCsv(List<Purchase> purchases) {
        return writeCsv(printer -> {
            printer.printRecord("Purchase ID", "Date", "Supplier", "Total", "Item Count");
            for (Purchase p : purchases) {
                printer.printRecord(
                        p.getId(),
                        p.getPurchaseDate() != null ? p.getPurchaseDate().format(DATE_FORMATTER) : "",
                        p.getSupplier() != null ? p.getSupplier().getName() : "",
                        p.getTotalAmount(),
                        p.getItems() != null ? p.getItems().size() : 0
                );
            }
        });
    }

    public byte[] exportStockMovementsCsv(List<StockMovement> movements) {
        return writeCsv(printer -> {
            printer.printRecord("Movement ID", "Product", "Product Code", "Movement Type", "Quantity", "Date", "Reference");
            for (StockMovement m : movements) {
                printer.printRecord(
                        m.getId(),
                        m.getProduct() != null ? m.getProduct().getName() : "",
                        m.getProduct() != null ? m.getProduct().getProductCode() : "",
                        m.getMovementType() != null ? m.getMovementType().name() : "",
                        m.getQuantity(),
                        m.getMovementDate() != null ? m.getMovementDate().format(DATE_FORMATTER) : "",
                        m.getReferenceId() != null ? m.getReferenceId().toString() : ""
                );
            }
        });
    }

    public byte[] exportAlertsCsv(List<Alert> alerts) {
        return writeCsv(printer -> {
            printer.printRecord("Alert ID", "Product", "Alert Type", "Severity", "Status", "Created Date");
            for (Alert a : alerts) {
                printer.printRecord(
                        a.getId(),
                        a.getProduct() != null ? a.getProduct().getName() : "",
                        a.getAlertType() != null ? a.getAlertType().name() : "",
                        a.getSeverity() != null ? a.getSeverity().name() : "",
                        a.getStatus() != null ? a.getStatus().name() : "",
                        a.getCreatedAt() != null ? a.getCreatedAt().format(DATE_FORMATTER) : ""
                );
            }
        });
    }

    public byte[] exportValuationCsv(List<InventoryValuationItem> items) {
        return writeCsv(printer -> {
            printer.printRecord("Product ID", "Product Code", "Product Name", "Category", "Stock Quantity", "Unit Cost", "Selling Price", "Inventory Value", "Potential Revenue", "Stock Status");
            for (InventoryValuationItem i : items) {
                printer.printRecord(
                        i.productId(),
                        i.productCode(),
                        i.productName(),
                        i.category(),
                        i.quantity(),
                        i.unitCost(),
                        i.sellingPrice(),
                        i.inventoryValue(),
                        i.potentialRevenue(),
                        i.stockStatus()
                );
            }
        });
    }

    @FunctionalInterface
    public interface CsvRecordWriter {
        void write(CSVPrinter printer) throws Exception;
    }

    private byte[] writeCsv(CsvRecordWriter writer) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Write UTF-8 BOM so Excel opens UTF-8 CSV with special characters seamlessly
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            CSVPrinter printer = new CSVPrinter(osw, CSVFormat.DEFAULT);
            writer.write(printer);
            printer.flush();
            osw.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export: " + e.getMessage(), e);
        }
    }
}
