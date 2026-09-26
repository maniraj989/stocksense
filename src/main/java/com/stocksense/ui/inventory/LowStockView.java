package com.stocksense.ui.inventory;

import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.service.ProductService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "stock/low", layout = MainLayout.class)
@PageTitle("Low Stock Alerts — StockSense")
@PermitAll
public class LowStockView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<ProductResponse> grid = new Grid<>();

    public LowStockView(ProductService productService) {
        this.productService = productService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Low Stock Items");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        Paragraph description = new Paragraph("Inventory items currently at or below their configured minimum threshold.");
        description.getStyle().set("color", "#64748b").set("margin-bottom", "16px");

        configureGrid();
        add(title, description, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(ProductResponse::getProductCode).setHeader("Product Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(ProductResponse::getName).setHeader("Product Name").setFlexGrow(2).setSortable(true);
        grid.addColumn(ProductResponse::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(ProductResponse::getSupplierName).setHeader("Supplier").setAutoWidth(true);

        grid.addColumn(p -> (p.getQuantity() != null ? p.getQuantity() : 0) + " units")
                .setHeader("Current Stock").setAutoWidth(true).setSortable(true);

        grid.addColumn(p -> (p.getMinimumStock() != null ? p.getMinimumStock() : 0) + " units")
                .setHeader("Min Threshold").setAutoWidth(true);

        grid.addColumn(p -> {
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            int min = p.getMinimumStock() != null ? p.getMinimumStock() : 0;
            int deficit = Math.max(0, min - qty);
            return deficit + " units";
        }).setHeader("Deficit").setAutoWidth(true);

        grid.addComponentColumn(p -> {
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            Span badge = new Span(qty == 0 ? "CRITICAL (0)" : "LOW STOCK");
            badge.getStyle()
                    .set("background-color", qty == 0 ? "#fee2e2" : "#fef3c7")
                    .set("color", qty == 0 ? "#dc2626" : "#d97706")
                    .set("padding", "3px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Urgency").setAutoWidth(true);
    }

    private void refreshGrid() {
        List<ProductResponse> lowStock = productService.findLowStockProducts();
        grid.setItems(lowStock);
    }
}
