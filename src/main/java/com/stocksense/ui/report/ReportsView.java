package com.stocksense.ui.report;

import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.entity.StockMovement;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.repository.AlertRepository;
import com.stocksense.product.entity.Product;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.report.dto.InventoryValuationItem;
import com.stocksense.report.dto.InventoryValuationSummary;
import com.stocksense.report.dto.PurchaseAnalyticsSummary;
import com.stocksense.report.dto.SalesAnalyticsSummary;
import com.stocksense.report.dto.TopPurchasedProductMetric;
import com.stocksense.report.dto.TopSellingProductMetric;
import com.stocksense.report.dto.TopSupplierSpendMetric;
import com.stocksense.report.service.AnalyticsService;
import com.stocksense.report.service.CsvExportService;
import com.stocksense.report.service.ExcelExportService;
import com.stocksense.report.service.InventoryValuationService;
import com.stocksense.report.service.PdfReportService;
import com.stocksense.sales.entity.Sale;
import com.stocksense.sales.repository.SaleRepository;
import com.stocksense.ui.dashboard.component.StatCard;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports & Analytics — StockSense")
@PermitAll
public class ReportsView extends VerticalLayout {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InventoryValuationService valuationService;
    private final AnalyticsService analyticsService;
    private final PdfReportService pdfReportService;
    private final CsvExportService csvExportService;
    private final ExcelExportService excelExportService;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AlertRepository alertRepository;

    public ReportsView(InventoryValuationService valuationService,
                       AnalyticsService analyticsService,
                       PdfReportService pdfReportService,
                       CsvExportService csvExportService,
                       ExcelExportService excelExportService,
                       ProductRepository productRepository,
                       SupplierRepository supplierRepository,
                       SaleRepository saleRepository,
                       PurchaseRepository purchaseRepository,
                       StockMovementRepository stockMovementRepository,
                       AlertRepository alertRepository) {
        this.valuationService = valuationService;
        this.analyticsService = analyticsService;
        this.pdfReportService = pdfReportService;
        this.csvExportService = csvExportService;
        this.excelExportService = excelExportService;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.alertRepository = alertRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Reports, Analytics & Exports");
        title.getStyle().set("margin-top", "0").set("color", "#1e3a8a");
        Paragraph subtitle = new Paragraph("Audit inventory valuation, export transaction histories, and analyze procurement and sales intelligence.");
        subtitle.getStyle().set("color", "#64748b").set("margin-top", "-8px");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.add("Inventory & Valuation", createInventoryTab());
        tabSheet.add("Sales Analytics", createSalesTab());
        tabSheet.add("Procurement Analytics", createProcurementTab());
        tabSheet.add("Stock Movement Audit", createStockMovementTab());
        tabSheet.add("Alerts Report", createAlertsTab());

        add(title, subtitle, tabSheet);
    }

    private Component createInventoryTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        InventoryValuationSummary summary = valuationService.getValuationSummary();
        List<InventoryValuationItem> items = valuationService.getValuationItems();

        HorizontalLayout kpis = new HorizontalLayout();
        kpis.setWidthFull();
        kpis.add(
                new StatCard("Total Inventory Cost", CURRENCY_FORMAT.format(summary.totalInventoryCostValue()), VaadinIcon.STORAGE.create(), "#1e3a8a", "Valuation at purchase cost"),
                new StatCard("Potential Revenue", CURRENCY_FORMAT.format(summary.totalPotentialRevenue()), VaadinIcon.MONEY.create(), "#16a34a", "Valuation at retail price"),
                new StatCard("Total Units in Stock", String.valueOf(summary.totalUnits()), VaadinIcon.PACKAGE.create(), "#475569", "Across all active products"),
                new StatCard("Low Stock Items", String.valueOf(summary.lowStockCount()), VaadinIcon.WARNING.create(), "#d97706", "Items below minimum"),
                new StatCard("Out of Stock", String.valueOf(summary.outOfStockCount()), VaadinIcon.CLOSE_CIRCLE.create(), "#dc2626", "Depleted inventory")
        );

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.add(
                createDownloadButton("Valuation PDF", VaadinIcon.FILE_TEXT, ButtonVariant.LUMO_PRIMARY, "inventory_valuation.pdf",
                        () -> pdfReportService.generateInventoryPdf(items, summary)),
                createDownloadButton("Valuation Excel", VaadinIcon.TABLE, ButtonVariant.LUMO_SUCCESS, "inventory_valuation.xlsx",
                        () -> excelExportService.exportInventoryValuationExcel(items, summary)),
                createDownloadButton("Valuation CSV", VaadinIcon.DOWNLOAD, null, "inventory_valuation.csv",
                        () -> csvExportService.exportValuationCsv(items)),
                createDownloadButton("Catalog CSV", VaadinIcon.DOWNLOAD_ALT, null, "products_catalog.csv",
                        () -> csvExportService.exportProductsCsv(productRepository.findAll())),
                createDownloadButton("Catalog Excel", VaadinIcon.FILE_TABLE, null, "products_catalog.xlsx",
                        () -> excelExportService.exportInventoryExcel(productRepository.findAll()))
        );

        Grid<InventoryValuationItem> grid = new Grid<>(InventoryValuationItem.class, false);
        grid.addColumn(InventoryValuationItem::productCode).setHeader("Product Code").setAutoWidth(true);
        grid.addColumn(InventoryValuationItem::productName).setHeader("Product Name").setAutoWidth(true);
        grid.addColumn(InventoryValuationItem::category).setHeader("Category").setAutoWidth(true);
        grid.addColumn(InventoryValuationItem::quantity).setHeader("Stock Qty").setAutoWidth(true);
        grid.addColumn(i -> CURRENCY_FORMAT.format(i.unitCost())).setHeader("Unit Cost").setAutoWidth(true);
        grid.addColumn(i -> CURRENCY_FORMAT.format(i.sellingPrice())).setHeader("Selling Price").setAutoWidth(true);
        grid.addColumn(i -> CURRENCY_FORMAT.format(i.inventoryValue())).setHeader("Total Valuation").setAutoWidth(true);
        grid.addColumn(i -> CURRENCY_FORMAT.format(i.potentialRevenue())).setHeader("Potential Revenue").setAutoWidth(true);
        grid.addComponentColumn(this::createValuationStatusBadge).setHeader("Status").setAutoWidth(true);

        grid.setItems(items);
        grid.setSizeFull();

        layout.add(kpis, actions, grid);
        return layout;
    }

    private Component createSalesTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        SalesAnalyticsSummary analytics = analyticsService.getSalesAnalytics();
        List<Sale> sales = saleRepository.findAll();

        HorizontalLayout kpis = new HorizontalLayout();
        kpis.setWidthFull();
        kpis.add(
                new StatCard("All-Time Revenue", CURRENCY_FORMAT.format(analytics.totalRevenue()), VaadinIcon.CASH.create(), "#16a34a", "Total recorded sales"),
                new StatCard("Total Orders", String.valueOf(analytics.totalSalesCount()), VaadinIcon.CART.create(), "#1e3a8a", "Completed transactions"),
                new StatCard("Average Order Value", CURRENCY_FORMAT.format(analytics.averageSaleValue()), VaadinIcon.CHART_LINE.create(), "#7e22ce", "Mean transaction amount"),
                new StatCard("Today's Revenue", CURRENCY_FORMAT.format(analytics.todaySales()), VaadinIcon.CALENDAR_CLOCK.create(), "#0284c7", "Sales recorded today"),
                new StatCard("This Month", CURRENCY_FORMAT.format(analytics.thisMonthSales()), VaadinIcon.CALENDAR.create(), "#475569", "Month-to-date sales")
        );

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.add(
                createDownloadButton("Sales Orders Excel", VaadinIcon.TABLE, ButtonVariant.LUMO_PRIMARY, "sales_orders.xlsx",
                        () -> excelExportService.exportSalesExcel(sales)),
                createDownloadButton("Sales Orders CSV", VaadinIcon.DOWNLOAD, null, "sales_orders.csv",
                        () -> csvExportService.exportSalesCsv(sales)),
                createDownloadButton("Sales KPI Summary Excel", VaadinIcon.CHART, ButtonVariant.LUMO_SUCCESS, "sales_summary.xlsx",
                        () -> excelExportService.exportSalesSummaryExcel(analytics))
        );

        H3 topTitle = new H3("Top Selling Products by Volume");
        topTitle.getStyle().set("margin-top", "12px").set("color", "#0f172a");

        Grid<TopSellingProductMetric> topGrid = new Grid<>(TopSellingProductMetric.class, false);
        topGrid.addColumn(TopSellingProductMetric::productCode).setHeader("Product Code").setAutoWidth(true);
        topGrid.addColumn(TopSellingProductMetric::productName).setHeader("Product Name").setAutoWidth(true);
        topGrid.addColumn(TopSellingProductMetric::quantitySold).setHeader("Total Units Sold").setAutoWidth(true);
        topGrid.addColumn(m -> CURRENCY_FORMAT.format(m.totalRevenue())).setHeader("Total Revenue Generated").setAutoWidth(true);
        topGrid.setItems(analytics.topSellingProducts());
        topGrid.setHeight("240px");

        layout.add(kpis, actions, topTitle, topGrid);
        return layout;
    }

    private Component createProcurementTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        PurchaseAnalyticsSummary analytics = analyticsService.getPurchaseAnalytics();
        List<Purchase> purchases = purchaseRepository.findAll();

        HorizontalLayout kpis = new HorizontalLayout();
        kpis.setWidthFull();
        kpis.add(
                new StatCard("All-Time Procurement", CURRENCY_FORMAT.format(analytics.totalSpend()), VaadinIcon.MONEY_WITHDRAW.create(), "#dc2626", "Total purchase expenditure"),
                new StatCard("Completed POs", String.valueOf(analytics.totalPurchaseCount()), VaadinIcon.TRUCK.create(), "#1e3a8a", "Received purchase orders"),
                new StatCard("Average PO Value", CURRENCY_FORMAT.format(analytics.averagePurchaseValue()), VaadinIcon.CHART_LINE.create(), "#7e22ce", "Mean purchase order size"),
                new StatCard("Today's Spend", CURRENCY_FORMAT.format(analytics.todayPurchases()), VaadinIcon.CALENDAR_CLOCK.create(), "#d97706", "Purchases recorded today"),
                new StatCard("This Month", CURRENCY_FORMAT.format(analytics.thisMonthPurchases()), VaadinIcon.CALENDAR.create(), "#475569", "Month-to-date purchases")
        );

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.add(
                createDownloadButton("Purchase Orders Excel", VaadinIcon.TABLE, ButtonVariant.LUMO_PRIMARY, "purchase_orders.xlsx",
                        () -> excelExportService.exportPurchasesExcel(purchases)),
                createDownloadButton("Purchase Orders CSV", VaadinIcon.DOWNLOAD, null, "purchase_orders.csv",
                        () -> csvExportService.exportPurchasesCsv(purchases)),
                createDownloadButton("Procurement Summary Excel", VaadinIcon.CHART, ButtonVariant.LUMO_SUCCESS, "procurement_summary.xlsx",
                        () -> excelExportService.exportPurchaseSummaryExcel(analytics)),
                createDownloadButton("Suppliers CSV", VaadinIcon.DOWNLOAD_ALT, null, "suppliers.csv",
                        () -> csvExportService.exportSuppliersCsv(supplierRepository.findAll()))
        );

        HorizontalLayout tables = new HorizontalLayout();
        tables.setSizeFull();

        VerticalLayout suppliersLayout = new VerticalLayout();
        suppliersLayout.setPadding(false);
        H3 supTitle = new H3("Top Suppliers by Spend");
        Grid<TopSupplierSpendMetric> supGrid = new Grid<>(TopSupplierSpendMetric.class, false);
        supGrid.addColumn(TopSupplierSpendMetric::supplierName).setHeader("Supplier").setAutoWidth(true);
        supGrid.addColumn(TopSupplierSpendMetric::orderCount).setHeader("Orders").setAutoWidth(true);
        supGrid.addColumn(m -> CURRENCY_FORMAT.format(m.totalSpend())).setHeader("Spend").setAutoWidth(true);
        supGrid.setItems(analytics.topSuppliers());
        supGrid.setHeight("240px");
        suppliersLayout.add(supTitle, supGrid);

        VerticalLayout productsLayout = new VerticalLayout();
        productsLayout.setPadding(false);
        H3 prodTitle = new H3("Top Purchased Products");
        Grid<TopPurchasedProductMetric> prodGrid = new Grid<>(TopPurchasedProductMetric.class, false);
        prodGrid.addColumn(TopPurchasedProductMetric::productCode).setHeader("Code").setAutoWidth(true);
        prodGrid.addColumn(TopPurchasedProductMetric::productName).setHeader("Name").setAutoWidth(true);
        prodGrid.addColumn(TopPurchasedProductMetric::quantityPurchased).setHeader("Qty").setAutoWidth(true);
        prodGrid.addColumn(m -> CURRENCY_FORMAT.format(m.totalSpend())).setHeader("Spend").setAutoWidth(true);
        prodGrid.setItems(analytics.topPurchasedProducts());
        prodGrid.setHeight("240px");
        productsLayout.add(prodTitle, prodGrid);

        tables.add(suppliersLayout, productsLayout);

        layout.add(kpis, actions, tables);
        return layout;
    }

    private Component createStockMovementTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        DatePicker dateFrom = new DatePicker("Date From");
        DatePicker dateTo = new DatePicker("Date To");

        ComboBox<Product> productFilter = new ComboBox<>("Product");
        productFilter.setItemLabelGenerator(Product::getName);
        productFilter.setItems(productRepository.findAll());
        productFilter.setClearButtonVisible(true);

        ComboBox<MovementType> typeFilter = new ComboBox<>("Movement Type");
        typeFilter.setItems(MovementType.values());
        typeFilter.setClearButtonVisible(true);

        Grid<StockMovement> grid = new Grid<>(StockMovement.class, false);
        grid.addColumn(StockMovement::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(m -> m.getMovementDate() != null ? m.getMovementDate().format(DATE_FORMATTER) : "").setHeader("Date & Time").setAutoWidth(true);
        grid.addColumn(m -> m.getProduct() != null ? m.getProduct().getProductCode() : "").setHeader("Product Code").setAutoWidth(true);
        grid.addColumn(m -> m.getProduct() != null ? m.getProduct().getName() : "").setHeader("Product Name").setAutoWidth(true);
        grid.addComponentColumn(this::createMovementBadge).setHeader("Type").setAutoWidth(true);
        grid.addColumn(StockMovement::getQuantity).setHeader("Quantity").setAutoWidth(true);
        grid.addColumn(m -> m.getReferenceId() != null ? String.valueOf(m.getReferenceId()) : "-").setHeader("Reference").setAutoWidth(true);
        grid.addColumn(m -> m.getCreatedBy() != null ? m.getCreatedBy().getName() : "System").setHeader("Recorded By").setAutoWidth(true);
        grid.setSizeFull();

        Runnable refreshMovements = () -> {
            Long prodId = productFilter.getValue() != null ? productFilter.getValue().getId() : null;
            MovementType mType = typeFilter.getValue();
            OffsetDateTime start = dateFrom.getValue() != null ? dateFrom.getValue().atStartOfDay().atOffset(ZoneOffset.UTC) : null;
            OffsetDateTime end = dateTo.getValue() != null ? dateTo.getValue().atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

            List<StockMovement> filtered = stockMovementRepository.findFilteredMovements(prodId, mType, start, end);
            grid.setItems(filtered);
        };

        Button filterBtn = new Button("Apply Filters", new Icon(VaadinIcon.FILTER), e -> refreshMovements.run());
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button resetBtn = new Button("Reset", new Icon(VaadinIcon.REFRESH), e -> {
            dateFrom.clear();
            dateTo.clear();
            productFilter.clear();
            typeFilter.clear();
            refreshMovements.run();
        });

        HorizontalLayout filters = new HorizontalLayout(dateFrom, dateTo, productFilter, typeFilter, filterBtn, resetBtn);
        filters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(
                createDownloadButton("Movements Excel", VaadinIcon.TABLE, ButtonVariant.LUMO_PRIMARY, "stock_movements.xlsx",
                        () -> {
                            Long prodId = productFilter.getValue() != null ? productFilter.getValue().getId() : null;
                            MovementType mType = typeFilter.getValue();
                            OffsetDateTime start = dateFrom.getValue() != null ? dateFrom.getValue().atStartOfDay().atOffset(ZoneOffset.UTC) : null;
                            OffsetDateTime end = dateTo.getValue() != null ? dateTo.getValue().atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;
                            return excelExportService.exportStockMovementsExcel(stockMovementRepository.findFilteredMovements(prodId, mType, start, end));
                        }),
                createDownloadButton("Movements CSV", VaadinIcon.DOWNLOAD, null, "stock_movements.csv",
                        () -> {
                            Long prodId = productFilter.getValue() != null ? productFilter.getValue().getId() : null;
                            MovementType mType = typeFilter.getValue();
                            OffsetDateTime start = dateFrom.getValue() != null ? dateFrom.getValue().atStartOfDay().atOffset(ZoneOffset.UTC) : null;
                            OffsetDateTime end = dateTo.getValue() != null ? dateTo.getValue().atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;
                            return csvExportService.exportStockMovementsCsv(stockMovementRepository.findFilteredMovements(prodId, mType, start, end));
                        })
        );

        refreshMovements.run();

        layout.add(filters, actions, grid);
        return layout;
    }

    private Component createAlertsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        List<Alert> alerts = alertRepository.findAll();

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(
                createDownloadButton("Alerts CSV", VaadinIcon.DOWNLOAD, ButtonVariant.LUMO_PRIMARY, "alerts_report.csv",
                        () -> csvExportService.exportAlertsCsv(alerts))
        );

        Grid<Alert> grid = new Grid<>(Alert.class, false);
        grid.addColumn(Alert::getId).setHeader("Alert ID").setAutoWidth(true);
        grid.addColumn(a -> a.getProduct() != null ? a.getProduct().getName() : "General").setHeader("Product").setAutoWidth(true);
        grid.addColumn(Alert::getAlertType).setHeader("Type").setAutoWidth(true);
        grid.addComponentColumn(this::createSeverityBadge).setHeader("Severity").setAutoWidth(true);
        grid.addColumn(Alert::getMessage).setHeader("Message").setAutoWidth(true);
        grid.addColumn(Alert::getStatus).setHeader("Status").setAutoWidth(true);
        grid.addColumn(a -> a.getCreatedAt() != null ? a.getCreatedAt().format(DATE_FORMATTER) : "").setHeader("Created Date").setAutoWidth(true);
        grid.setItems(alerts);
        grid.setSizeFull();

        layout.add(actions, grid);
        return layout;
    }

    private Component createDownloadButton(String label, VaadinIcon icon, ButtonVariant variant, String filename, Supplier<byte[]> dataSupplier) {
        if (com.vaadin.flow.component.UI.getCurrent() == null) {
            Button button = new Button(label, icon.create());
            if (variant != null) {
                button.addThemeVariants(variant);
            }
            return button;
        }

        StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(dataSupplier.get()));
        Anchor anchor = new Anchor(resource, "");
        anchor.getElement().setAttribute("download", true);

        Button button = new Button(label, icon.create());
        if (variant != null) {
            button.addThemeVariants(variant);
        }
        anchor.add(button);
        return anchor;
    }

    private Span createValuationStatusBadge(InventoryValuationItem item) {
        Span badge = new Span(item.stockStatus());
        badge.getElement().getThemeList().add("badge");
        if ("OUT_OF_STOCK".equals(item.stockStatus())) {
            badge.getStyle().set("background-color", "#fee2e2").set("color", "#991b1b");
        } else if ("LOW_STOCK".equals(item.stockStatus())) {
            badge.getStyle().set("background-color", "#fef3c7").set("color", "#92400e");
        } else {
            badge.getStyle().set("background-color", "#dcfce7").set("color", "#166534");
        }
        badge.getStyle().set("font-size", "0.75rem").set("padding", "2px 8px").set("border-radius", "12px");
        return badge;
    }

    private Span createMovementBadge(StockMovement m) {
        String type = m.getMovementType() != null ? m.getMovementType().name() : "";
        Span badge = new Span(type);
        badge.getElement().getThemeList().add("badge");
        if ("IN".equals(type)) {
            badge.getStyle().set("background-color", "#dcfce7").set("color", "#166534");
        } else if ("OUT".equals(type)) {
            badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
        } else {
            badge.getStyle().set("background-color", "#ffedd5").set("color", "#c2410c");
        }
        badge.getStyle().set("font-size", "0.75rem").set("padding", "2px 8px").set("border-radius", "12px");
        return badge;
    }

    private Span createSeverityBadge(Alert alert) {
        String sev = alert.getSeverity() != null ? alert.getSeverity().name() : "";
        Span badge = new Span(sev);
        badge.getElement().getThemeList().add("badge");
        if ("HIGH".equals(sev)) {
            badge.getStyle().set("background-color", "#fee2e2").set("color", "#991b1b");
        } else if ("MEDIUM".equals(sev)) {
            badge.getStyle().set("background-color", "#fef3c7").set("color", "#92400e");
        } else {
            badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
        }
        badge.getStyle().set("font-size", "0.75rem").set("padding", "2px 8px").set("border-radius", "12px");
        return badge;
    }
}
