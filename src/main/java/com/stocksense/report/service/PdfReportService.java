package com.stocksense.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.purchase.entity.PurchaseItem;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.report.dto.InventoryValuationItem;
import com.stocksense.report.dto.InventoryValuationSummary;
import com.stocksense.sales.entity.Sale;
import com.stocksense.sales.entity.SaleItem;
import com.stocksense.sales.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PdfReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 58, 138));
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(30, 41, 59));
    private static final Font BOLD_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(30, 41, 59));

    private static final Color PRIMARY_COLOR = new Color(30, 58, 138);
    private static final Color ALT_ROW_COLOR = new Color(248, 250, 252);

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final InventoryValuationService inventoryValuationService;

    public PdfReportService(SaleRepository saleRepository,
                            PurchaseRepository purchaseRepository,
                            InventoryValuationService inventoryValuationService) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.inventoryValuationService = inventoryValuationService;
    }

    public byte[] generateSalesInvoicePdf(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));
        return generateSalesInvoicePdf(sale);
    }

    public byte[] generatePurchaseOrderPdf(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + purchaseId));
        return generatePurchasePdf(purchase);
    }

    public byte[] generatePurchasePdf(Long purchaseId) {
        return generatePurchaseOrderPdf(purchaseId);
    }

    public byte[] generatePurchaseOrderPdf(Purchase purchase) {
        return generatePurchasePdf(purchase);
    }

    public byte[] generateInventoryValuationPdf() {
        return generateInventoryPdf(inventoryValuationService.getItemizedValuation(), inventoryValuationService.getValuationSummary());
    }

    public byte[] generateSalesInvoicePdf(Sale sale) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addBrandingHeader(document, "SALES INVOICE / RECEIPT");

            // Meta Info Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            metaTable.addCell(createMetaCell("Invoice Number:", "INV-" + sale.getId()));
            metaTable.addCell(createMetaCell("Date & Time:", sale.getSaleDate() != null ? sale.getSaleDate().format(DATE_FORMATTER) : "N/A"));
            metaTable.addCell(createMetaCell("Cashier / Staff:", sale.getCreatedBy() != null ? sale.getCreatedBy().getName() : "System"));
            metaTable.addCell(createMetaCell("Status:", "COMPLETED"));

            document.add(metaTable);

            // Items Table
            PdfPTable itemsTable = new PdfPTable(new float[]{2f, 4f, 1.5f, 2f, 2f});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingAfter(15);

            addTableHeader(itemsTable, new String[]{"SKU / Code", "Product Name", "Qty", "Unit Price", "Line Total"});

            boolean alt = false;
            for (SaleItem item : sale.getItems()) {
                Color bg = alt ? ALT_ROW_COLOR : Color.WHITE;
                itemsTable.addCell(createCell(item.getProduct() != null ? item.getProduct().getProductCode() : "-", bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(item.getProduct() != null ? item.getProduct().getName() : "-", bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getUnitPrice()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getSubtotal()), bg, Element.ALIGN_RIGHT));
                alt = !alt;
            }

            document.add(itemsTable);

            // Total Summary Table
            PdfPTable totalTable = new PdfPTable(new float[]{7f, 3f});
            totalTable.setWidthPercentage(100);
            totalTable.addCell(createBorderlessCell("Grand Total:", Element.ALIGN_RIGHT, BOLD_BODY_FONT));
            totalTable.addCell(createBorderlessCell(CURRENCY_FORMAT.format(sale.getTotalAmount()), Element.ALIGN_RIGHT, TITLE_FONT));

            document.add(totalTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sales invoice PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generatePurchasePdf(Purchase purchase) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addBrandingHeader(document, "PURCHASE ORDER");

            // Meta Info Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            metaTable.addCell(createMetaCell("PO Number:", "PO-" + purchase.getId()));
            metaTable.addCell(createMetaCell("Order Date:", purchase.getPurchaseDate() != null ? purchase.getPurchaseDate().format(DATE_FORMATTER) : "N/A"));
            metaTable.addCell(createMetaCell("Supplier:", purchase.getSupplier() != null ? purchase.getSupplier().getName() : "N/A"));
            metaTable.addCell(createMetaCell("Created By:", purchase.getCreatedBy() != null ? purchase.getCreatedBy().getName() : "System"));

            document.add(metaTable);

            // Items Table
            PdfPTable itemsTable = new PdfPTable(new float[]{2f, 4f, 1.5f, 2f, 2f});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingAfter(15);

            addTableHeader(itemsTable, new String[]{"SKU / Code", "Product Name", "Qty", "Unit Price", "Line Total"});

            boolean alt = false;
            for (PurchaseItem item : purchase.getItems()) {
                Color bg = alt ? ALT_ROW_COLOR : Color.WHITE;
                itemsTable.addCell(createCell(item.getProduct() != null ? item.getProduct().getProductCode() : "-", bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(item.getProduct() != null ? item.getProduct().getName() : "-", bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getUnitPrice()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.getSubtotal()), bg, Element.ALIGN_RIGHT));
                alt = !alt;
            }

            document.add(itemsTable);

            // Total Summary Table
            PdfPTable totalTable = new PdfPTable(new float[]{7f, 3f});
            totalTable.setWidthPercentage(100);
            totalTable.addCell(createBorderlessCell("Total PO Amount:", Element.ALIGN_RIGHT, BOLD_BODY_FONT));
            totalTable.addCell(createBorderlessCell(CURRENCY_FORMAT.format(purchase.getTotalAmount()), Element.ALIGN_RIGHT, TITLE_FONT));

            document.add(totalTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate purchase order PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateInventoryPdf(List<InventoryValuationItem> items, InventoryValuationSummary summary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addBrandingHeader(document, "INVENTORY STATUS & VALUATION REPORT");

            // Summary Table
            PdfPTable sumTable = new PdfPTable(4);
            sumTable.setWidthPercentage(100);
            sumTable.setSpacingAfter(15);

            sumTable.addCell(createMetaCell("Total Inventory Value:", CURRENCY_FORMAT.format(summary.totalInventoryCostValue())));
            sumTable.addCell(createMetaCell("Total Potential Revenue:", CURRENCY_FORMAT.format(summary.totalPotentialRevenue())));
            sumTable.addCell(createMetaCell("Total Units in Stock:", String.valueOf(summary.totalUnits())));
            sumTable.addCell(createMetaCell("Total Catalog Products:", String.valueOf(summary.productCount())));

            document.add(sumTable);

            // Items Table
            PdfPTable itemsTable = new PdfPTable(new float[]{1.5f, 3.5f, 2f, 1.2f, 1.8f, 2f, 1.8f});
            itemsTable.setWidthPercentage(100);

            addTableHeader(itemsTable, new String[]{"SKU / Code", "Product Name", "Category", "Stock", "Unit Cost", "Total Value", "Status"});

            boolean alt = false;
            for (InventoryValuationItem item : items) {
                Color bg = alt ? ALT_ROW_COLOR : Color.WHITE;
                itemsTable.addCell(createCell(item.productCode(), bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(item.productName(), bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(item.category(), bg, Element.ALIGN_LEFT));
                itemsTable.addCell(createCell(String.valueOf(item.quantity()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.unitCost()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(CURRENCY_FORMAT.format(item.inventoryValue()), bg, Element.ALIGN_RIGHT));
                itemsTable.addCell(createCell(item.stockStatus(), bg, Element.ALIGN_CENTER));
                alt = !alt;
            }

            document.add(itemsTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate inventory valuation PDF: " + e.getMessage(), e);
        }
    }

    private void addBrandingHeader(Document document, String documentTitle) throws Exception {
        Paragraph title = new Paragraph("StockSense", TITLE_FONT);
        title.setSpacingAfter(2);
        document.add(title);

        Paragraph subtitle = new Paragraph("Smart Inventory Management System — " + documentTitle, SUBTITLE_FONT);
        subtitle.setSpacingAfter(15);
        document.add(subtitle);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text, Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private PdfPCell createMetaCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(3);
        Paragraph p = new Paragraph();
        p.add(new Phrase(label + " ", BOLD_BODY_FONT));
        p.add(new Phrase(value, BODY_FONT));
        cell.addElement(p);
        return cell;
    }

    private PdfPCell createBorderlessCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(0);
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}
