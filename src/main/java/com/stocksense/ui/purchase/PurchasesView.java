package com.stocksense.ui.purchase;

import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseItemResponse;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.purchase.dto.PurchaseResponse;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "purchases", layout = MainLayout.class)
@PageTitle("Purchases — StockSense")
@PermitAll
public class PurchasesView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final com.stocksense.report.service.PdfReportService pdfReportService;

    private final Grid<PurchaseResponse> grid = new Grid<>();

    public PurchasesView(PurchaseService purchaseService,
                         SupplierService supplierService,
                         ProductService productService,
                         com.stocksense.report.service.PdfReportService pdfReportService) {
        this.purchaseService = purchaseService;
        this.supplierService = supplierService;
        this.productService = productService;
        this.pdfReportService = pdfReportService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Purchase Orders");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        Button createBtn = new Button("New Purchase Order", new Icon(VaadinIcon.PLUS), e -> openCreatePurchaseDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(createBtn);
        toolbar.setWidthFull();

        configureGrid();

        add(title, toolbar, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(p -> "#" + p.getId()).setHeader("Order ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(p -> p.getPurchaseDate() != null ? p.getPurchaseDate().format(DATE_FORMATTER) : "-")
                .setHeader("Purchase Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(PurchaseResponse::getSupplierName).setHeader("Supplier").setFlexGrow(2);
        grid.addColumn(p -> "$" + (p.getTotalAmount() != null ? p.getTotalAmount() : "0.00"))
                .setHeader("Total Amount").setAutoWidth(true);
        grid.addColumn(p -> p.getItems() != null ? p.getItems().size() + " items" : "0 items")
                .setHeader("Line Items").setAutoWidth(true);
        grid.addColumn(p -> p.getCreatedByUsername() != null ? p.getCreatedByUsername() : "System")
                .setHeader("Created By").setAutoWidth(true);

        grid.addComponentColumn(p -> {
            Button detailsBtn = new Button("View Items", new Icon(VaadinIcon.EYE), e -> openDetailsDialog(p));
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

            com.vaadin.flow.server.StreamResource pdfRes = new com.vaadin.flow.server.StreamResource("purchase-order-" + p.getId() + ".pdf",
                    () -> new java.io.ByteArrayInputStream(pdfReportService.generatePurchaseOrderPdf(p.getId())));
            com.vaadin.flow.component.html.Anchor pdfLink = new com.vaadin.flow.component.html.Anchor(pdfRes, "");
            pdfLink.getElement().setAttribute("download", true);
            Button pdfBtn = new Button("Purchase PDF", new Icon(VaadinIcon.DOWNLOAD_ALT));
            pdfBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            pdfLink.add(pdfBtn);

            HorizontalLayout actions = new HorizontalLayout(detailsBtn, pdfLink);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openDetailsDialog(PurchaseResponse purchase) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Purchase Order #" + purchase.getId() + " Details");

        Grid<PurchaseItemResponse> itemsGrid = new Grid<>();
        itemsGrid.addColumn(PurchaseItemResponse::getProductCode).setHeader("Product Code").setAutoWidth(true);
        itemsGrid.addColumn(PurchaseItemResponse::getProductName).setHeader("Product Name").setFlexGrow(2);
        itemsGrid.addColumn(PurchaseItemResponse::getQuantity).setHeader("Quantity").setAutoWidth(true);
        itemsGrid.addColumn(i -> "$" + i.getUnitPrice()).setHeader("Unit Price").setAutoWidth(true);
        itemsGrid.addColumn(i -> "$" + i.getSubtotal()).setHeader("Subtotal").setAutoWidth(true);

        itemsGrid.setItems(purchase.getItems() != null ? purchase.getItems() : List.of());
        itemsGrid.setHeight("250px");

        Span totalSpan = new Span("Total Order Amount: $" + purchase.getTotalAmount());
        totalSpan.getStyle().set("font-weight", "700").set("font-size", "1.1rem").set("color", "#0f172a");

        VerticalLayout layout = new VerticalLayout(itemsGrid, totalSpan);
        layout.setPadding(false);

        com.vaadin.flow.server.StreamResource pdfRes = new com.vaadin.flow.server.StreamResource("purchase-order-" + purchase.getId() + ".pdf",
                () -> new java.io.ByteArrayInputStream(pdfReportService.generatePurchaseOrderPdf(purchase.getId())));
        com.vaadin.flow.component.html.Anchor downloadPdfLink = new com.vaadin.flow.component.html.Anchor(pdfRes, "");
        downloadPdfLink.getElement().setAttribute("download", true);
        Button downloadPdfBtn = new Button("Download Purchase PDF", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadPdfBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadPdfLink.add(downloadPdfBtn);

        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.add(layout);
        dialog.getFooter().add(downloadPdfLink, closeBtn);
        dialog.setWidth("650px");
        dialog.open();
    }

    private void openCreatePurchaseDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Purchase Order");

        ComboBox<Supplier> supplierCombo = new ComboBox<>("Supplier");
        supplierCombo.setItems(supplierService.findAll());
        supplierCombo.setItemLabelGenerator(Supplier::getName);
        supplierCombo.setRequired(true);
        supplierCombo.setWidthFull();

        List<PurchaseItemRequest> itemsList = new ArrayList<>();
        Grid<PurchaseItemRequest> builderGrid = new Grid<>();
        Span totalDisplay = new Span("Total: $0.00");
        totalDisplay.getStyle().set("font-weight", "700").set("font-size", "1.1rem").set("color", "#1e3a8a");

        // Line item inputs
        ComboBox<Product> productCombo = new ComboBox<>("Add Product");
        List<Product> products = productService.findAll();
        productCombo.setItems(products);
        productCombo.setItemLabelGenerator(p -> p.getProductCode() + " - " + p.getName());
        productCombo.setWidth("240px");

        IntegerField qtyInput = new IntegerField("Quantity");
        qtyInput.setValue(1);
        qtyInput.setMin(1);
        qtyInput.setWidth("100px");

        BigDecimalField priceInput = new BigDecimalField("Unit Cost ($)");
        priceInput.setValue(BigDecimal.ZERO);
        priceInput.setWidth("120px");

        productCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().getPurchasePrice() != null) {
                priceInput.setValue(e.getValue().getPurchasePrice());
            }
        });

        Button addItemBtn = new Button("Add Item", new Icon(VaadinIcon.PLUS));
        addItemBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        addItemBtn.addClickListener(e -> {
            Product selectedProduct = productCombo.getValue();
            Integer qty = qtyInput.getValue();
            BigDecimal price = priceInput.getValue();

            if (selectedProduct == null || qty == null || qty <= 0 || price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Please select product, quantity > 0, and non-negative price", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            itemsList.add(new PurchaseItemRequest(selectedProduct.getId(), qty, price));
            builderGrid.setItems(new ArrayList<>(itemsList));

            BigDecimal sum = itemsList.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalDisplay.setText("Total: $" + sum);

            productCombo.clear();
            qtyInput.setValue(1);
            priceInput.setValue(BigDecimal.ZERO);
        });

        HorizontalLayout itemEntryRow = new HorizontalLayout(productCombo, qtyInput, priceInput, addItemBtn);
        itemEntryRow.setAlignItems(FlexComponent.Alignment.BASELINE);

        builderGrid.addColumn(item -> {
            return products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .map(p -> p.getProductCode() + " - " + p.getName())
                    .orElse("ID: " + item.getProductId());
        }).setHeader("Product").setFlexGrow(2);
        builderGrid.addColumn(PurchaseItemRequest::getQuantity).setHeader("Qty").setAutoWidth(true);
        builderGrid.addColumn(i -> "$" + i.getUnitPrice()).setHeader("Unit Price").setAutoWidth(true);
        builderGrid.addColumn(i -> "$" + i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).setHeader("Subtotal").setAutoWidth(true);

        builderGrid.addComponentColumn(item -> {
            Button removeBtn = new Button(new Icon(VaadinIcon.TRASH), ev -> {
                itemsList.remove(item);
                builderGrid.setItems(new ArrayList<>(itemsList));
                BigDecimal sum = itemsList.stream()
                        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalDisplay.setText("Total: $" + sum);
            });
            removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            return removeBtn;
        }).setHeader("Remove").setAutoWidth(true);

        builderGrid.setHeight("200px");

        VerticalLayout layout = new VerticalLayout(supplierCombo, itemEntryRow, builderGrid, totalDisplay);
        layout.setPadding(false);

        Button submitBtn = new Button("Submit Purchase Order", e -> {
            if (supplierCombo.getValue() == null) {
                Notification.show("Please select a supplier", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (itemsList.isEmpty()) {
                Notification.show("Please add at least one line item", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                PurchaseRequest request = new PurchaseRequest(supplierCombo.getValue().getId(), itemsList);
                purchaseService.createPurchase(request);

                Notification.show("Purchase order created and stock incremented successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Failed to create purchase order: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(cancelBtn, submitBtn);
        dialog.setWidth("750px");
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(purchaseService.findAll(PageRequest.of(0, 100)).getContent());
    }
}
