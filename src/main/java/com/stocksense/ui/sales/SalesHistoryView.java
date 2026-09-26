package com.stocksense.ui.sales;

import com.stocksense.report.service.PdfReportService;
import com.stocksense.sales.dto.SaleItemResponse;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.service.SaleService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "sales/history", layout = MainLayout.class)
@PageTitle("Sales History — StockSense")
@PermitAll
public class SalesHistoryView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SaleService saleService;
    private final PdfReportService pdfReportService;
    private final Grid<SaleResponse> grid = new Grid<>();

    public SalesHistoryView(SaleService saleService, PdfReportService pdfReportService) {
        this.saleService = saleService;
        this.pdfReportService = pdfReportService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Sales History & Receipts");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        configureGrid();
        add(title, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(s -> "#" + s.getId()).setHeader("Sale ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(s -> s.getSaleDate() != null ? s.getSaleDate().format(DATE_FORMATTER) : "-")
                .setHeader("Date & Time").setAutoWidth(true).setSortable(true);
        grid.addColumn(s -> "$" + (s.getTotalAmount() != null ? s.getTotalAmount() : "0.00"))
                .setHeader("Total Revenue").setAutoWidth(true);
        grid.addColumn(s -> s.getItems() != null ? s.getItems().size() + " items" : "0 items")
                .setHeader("Items Count").setAutoWidth(true);
        grid.addColumn(s -> s.getCreatedByUsername() != null ? s.getCreatedByUsername() : "System")
                .setHeader("Cashier / Staff").setAutoWidth(true);

        grid.addComponentColumn(s -> {
            Button detailsBtn = new Button("View Receipt", new Icon(VaadinIcon.FILE_TEXT_O), e -> openReceiptDialog(s));
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

            StreamResource pdfRes = new StreamResource("invoice-" + s.getId() + ".pdf",
                    () -> new ByteArrayInputStream(pdfReportService.generateSalesInvoicePdf(s.getId())));
            Anchor pdfLink = new Anchor(pdfRes, "");
            pdfLink.getElement().setAttribute("download", true);
            Button pdfBtn = new Button("Invoice PDF", new Icon(VaadinIcon.DOWNLOAD_ALT));
            pdfBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            pdfLink.add(pdfBtn);

            HorizontalLayout actions = new HorizontalLayout(detailsBtn, pdfLink);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openReceiptDialog(SaleResponse sale) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Sale Transaction #" + sale.getId());

        Grid<SaleItemResponse> itemsGrid = new Grid<>();
        itemsGrid.addColumn(SaleItemResponse::getProductCode).setHeader("Product Code").setAutoWidth(true);
        itemsGrid.addColumn(SaleItemResponse::getProductName).setHeader("Product Name").setFlexGrow(2);
        itemsGrid.addColumn(SaleItemResponse::getQuantity).setHeader("Quantity").setAutoWidth(true);
        itemsGrid.addColumn(i -> "$" + i.getUnitPrice()).setHeader("Unit Price").setAutoWidth(true);
        itemsGrid.addColumn(i -> "$" + i.getSubtotal()).setHeader("Subtotal").setAutoWidth(true);

        itemsGrid.setItems(sale.getItems() != null ? sale.getItems() : List.of());
        itemsGrid.setHeight("250px");

        Span totalSpan = new Span("Grand Total: $" + sale.getTotalAmount());
        totalSpan.getStyle().set("font-weight", "700").set("font-size", "1.1rem").set("color", "#059669");

        VerticalLayout layout = new VerticalLayout(itemsGrid, totalSpan);
        layout.setPadding(false);

        StreamResource pdfRes = new StreamResource("invoice-" + sale.getId() + ".pdf",
                () -> new ByteArrayInputStream(pdfReportService.generateSalesInvoicePdf(sale.getId())));
        Anchor downloadPdfLink = new Anchor(pdfRes, "");
        downloadPdfLink.getElement().setAttribute("download", true);
        Button downloadPdfBtn = new Button("Download Invoice PDF", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadPdfBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadPdfLink.add(downloadPdfBtn);

        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.add(layout);
        dialog.getFooter().add(downloadPdfLink, closeBtn);
        dialog.setWidth("650px");
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(saleService.findAll(PageRequest.of(0, 100)).getContent());
    }
}
