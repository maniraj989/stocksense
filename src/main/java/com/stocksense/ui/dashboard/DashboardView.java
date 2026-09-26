package com.stocksense.ui.dashboard;

import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.service.AlertService;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.service.SaleService;
import com.stocksense.ui.dashboard.component.StatCard;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard — StockSense")
@PermitAll
public class DashboardView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final AlertService alertService;
    private final com.stocksense.report.service.AnalyticsService analyticsService;

    private final FlexLayout kpiCardsLayout = new FlexLayout();
    private final Grid<ProductResponse> lowStockGrid = new Grid<>();
    private final Grid<SaleResponse> recentSalesGrid = new Grid<>();

    public DashboardView(ProductService productService,
                         SupplierService supplierService,
                         PurchaseService purchaseService,
                         SaleService saleService,
                         AlertService alertService,
                         com.stocksense.report.service.AnalyticsService analyticsService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.purchaseService = purchaseService;
        this.saleService = saleService;
        this.alertService = alertService;
        this.analyticsService = analyticsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 pageHeader = new H2("Inventory Overview");
        pageHeader.getStyle().set("margin-top", "0").set("color", "#0f172a");

        configureKpiCards();
        configureGrids();

        HorizontalLayout tablesRow = new HorizontalLayout(createLowStockSection(), createRecentSalesSection());
        tablesRow.setWidthFull();
        tablesRow.setSpacing(true);

        add(pageHeader, kpiCardsLayout, tablesRow);
        refreshData();
    }

    private void configureKpiCards() {
        kpiCardsLayout.setWidthFull();
        kpiCardsLayout.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))")
                .set("gap", "16px");
    }

    private void configureGrids() {
        lowStockGrid.addColumn(ProductResponse::getProductCode).setHeader("Code").setAutoWidth(true);
        lowStockGrid.addColumn(ProductResponse::getName).setHeader("Product").setFlexGrow(2);
        lowStockGrid.addColumn(ProductResponse::getQuantity).setHeader("In Stock").setAutoWidth(true);
        lowStockGrid.addColumn(ProductResponse::getMinimumStock).setHeader("Min Threshold").setAutoWidth(true);
        lowStockGrid.addComponentColumn(p -> {
            Span badge = new Span(p.getQuantity() == 0 ? "Out of Stock" : "Low Stock");
            badge.getStyle()
                    .set("color", p.getQuantity() == 0 ? "#dc2626" : "#d97706")
                    .set("background-color", p.getQuantity() == 0 ? "#fee2e2" : "#fef3c7")
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Status").setAutoWidth(true);
        lowStockGrid.setHeight("260px");

        recentSalesGrid.addColumn(s -> "#" + s.getId()).setHeader("Sale ID").setAutoWidth(true);
        recentSalesGrid.addColumn(s -> s.getSaleDate() != null ? s.getSaleDate().format(DATE_FORMATTER) : "-")
                .setHeader("Date").setFlexGrow(1);
        recentSalesGrid.addColumn(s -> "$" + s.getTotalAmount()).setHeader("Amount").setAutoWidth(true);
        recentSalesGrid.addColumn(s -> s.getCreatedByUsername() != null ? s.getCreatedByUsername() : "System")
                .setHeader("Staff").setAutoWidth(true);
        recentSalesGrid.setHeight("260px");
    }

    private VerticalLayout createLowStockSection() {
        H3 sectionTitle = new H3("Critical Stock Alerts");
        sectionTitle.getStyle().set("margin", "0 0 8px 0").set("font-size", "1.1rem").set("color", "#334155");
        VerticalLayout layout = new VerticalLayout(sectionTitle, lowStockGrid);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidth("50%");
        return layout;
    }

    private VerticalLayout createRecentSalesSection() {
        H3 sectionTitle = new H3("Recent Completed Sales");
        sectionTitle.getStyle().set("margin", "0 0 8px 0").set("font-size", "1.1rem").set("color", "#334155");
        VerticalLayout layout = new VerticalLayout(sectionTitle, recentSalesGrid);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidth("50%");
        return layout;
    }

    private void refreshData() {
        kpiCardsLayout.removeAll();

        com.stocksense.report.dto.DashboardAnalyticsDto analytics = analyticsService.getDashboardAnalytics();

        kpiCardsLayout.add(
                new StatCard("Inventory Valuation", "$" + analytics.totalInventoryValue(), VaadinIcon.STORAGE.create(), "#1e3a8a", "Total stock cost value"),
                new StatCard("Today's Sales", "$" + analytics.todaySales(), VaadinIcon.CASH.create(), "#16a34a", "Revenue today"),
                new StatCard("Today's Purchases", "$" + analytics.todayPurchases(), VaadinIcon.CART.create(), "#0891b2", "Procurement spend today"),
                new StatCard("Sales This Month", "$" + analytics.salesThisMonth(), VaadinIcon.CHART_LINE.create(), "#7e22ce", "Month-to-date revenue"),
                new StatCard("Purchases This Month", "$" + analytics.purchasesThisMonth(), VaadinIcon.MONEY_WITHDRAW.create(), "#d97706", "Month-to-date procurement"),
                new StatCard("Low Stock Items", String.valueOf(analytics.lowStockCount()), VaadinIcon.WARNING.create(), "#dc2626", "Action required"),
                new StatCard("Total Products", String.valueOf(analytics.totalProducts()), VaadinIcon.PACKAGE.create(), "#2563eb", "Catalog count"),
                new StatCard("Total Suppliers", String.valueOf(analytics.totalSuppliers()), VaadinIcon.TRUCK.create(), "#475569", "Registered vendors")
        );

        lowStockGrid.setItems(productService.findLowStockProducts());
        recentSalesGrid.setItems(saleService.findAll(PageRequest.of(0, 10)).getContent());
    }
}
