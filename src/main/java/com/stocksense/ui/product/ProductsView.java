package com.stocksense.ui.product;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.DuplicateResourceException;
import com.stocksense.product.dto.ProductRequest;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.ui.layout.MainLayout;
import com.stocksense.ui.util.SecurityUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products — StockSense")
@PermitAll
public class ProductsView extends VerticalLayout {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final SecurityUtils securityUtils;
    private final com.stocksense.barcode.service.BarcodeService barcodeService;

    private final Grid<ProductResponse> grid = new Grid<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();

    public ProductsView(ProductService productService,
                        SupplierService supplierService,
                        SecurityUtils securityUtils,
                        com.stocksense.barcode.service.BarcodeService barcodeService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.securityUtils = securityUtils;
        this.barcodeService = barcodeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Product Catalog");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        HorizontalLayout toolbar = createToolbar();
        configureGrid();

        add(title, toolbar, grid);
        refreshGrid();
    }

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Search by code or name...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("300px");

        categoryFilter.setPlaceholder("Filter by category");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setItems("Electronics", "Grocery", "Stationery", "Clothing", "Hardware");
        categoryFilter.addValueChangeListener(e -> refreshGrid());

        Button addProductBtn = new Button("Add Product", new Icon(VaadinIcon.PLUS), e -> openProductDialog(null));
        addProductBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, categoryFilter, addProductBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(ProductResponse::getProductCode).setHeader("Product Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(ProductResponse::getName).setHeader("Name").setFlexGrow(2).setSortable(true);
        grid.addColumn(ProductResponse::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(ProductResponse::getSupplierName).setHeader("Supplier").setAutoWidth(true);
        grid.addColumn(p -> "$" + (p.getPurchasePrice() != null ? p.getPurchasePrice() : "0.00"))
                .setHeader("Cost").setAutoWidth(true);
        grid.addColumn(p -> "$" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0.00"))
                .setHeader("Selling Price").setAutoWidth(true);
        grid.addComponentColumn(p -> {
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            int min = p.getMinimumStock() != null ? p.getMinimumStock() : 0;
            Span badge = new Span(String.valueOf(qty));
            if (qty <= min) {
                badge.getStyle()
                        .set("background-color", qty == 0 ? "#fee2e2" : "#fef3c7")
                        .set("color", qty == 0 ? "#dc2626" : "#d97706")
                        .set("padding", "2px 8px")
                        .set("border-radius", "10px")
                        .set("font-weight", "600");
            }
            return badge;
        }).setHeader("Stock").setAutoWidth(true);

        grid.addComponentColumn(p -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button barcodeBtn = new Button(new Icon(VaadinIcon.BARCODE), e -> openBarcodeDialog(p));
            barcodeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            barcodeBtn.setTooltipText("View & Download Barcode");
            actions.add(barcodeBtn);

            Button editBtn = new Button(new Icon(VaadinIcon.EDIT), e -> openProductDialog(p));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            editBtn.setTooltipText("Edit Product");
            actions.add(editBtn);

            if (securityUtils.isAdmin()) {
                Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(p));
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
                deleteBtn.setTooltipText("Delete Product (Admin)");
                actions.add(deleteBtn);
            }
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openBarcodeDialog(ProductResponse product) {
        if (product == null || product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            Notification.show("Product does not have a valid product code.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Product Barcode: " + product.getProductCode());

        try {
            byte[] barcodeBytes = barcodeService.generateBarcodePng(product.getProductCode(), 320, 110);
            com.vaadin.flow.server.StreamResource resource = new com.vaadin.flow.server.StreamResource(
                    "barcode_" + product.getProductCode() + ".png",
                    () -> new java.io.ByteArrayInputStream(barcodeBytes)
            );

            com.vaadin.flow.component.html.Image barcodeImage = new com.vaadin.flow.component.html.Image(resource, "Barcode for " + product.getProductCode());
            barcodeImage.setWidth("320px");
            barcodeImage.setHeight("110px");
            barcodeImage.getStyle().set("border", "1px solid #cbd5e1").set("padding", "8px").set("background-color", "#ffffff");

            Span nameSpan = new Span(product.getName());
            nameSpan.getStyle().set("font-weight", "600").set("color", "#1e3a8a");

            Span codeSpan = new Span("SKU / Code 128: " + product.getProductCode());
            codeSpan.getStyle().set("font-size", "0.875rem").set("color", "#64748b");

            VerticalLayout content = new VerticalLayout(nameSpan, codeSpan, barcodeImage);
            content.setAlignItems(FlexComponent.Alignment.CENTER);
            content.setSpacing(true);
            dialog.add(content);

            com.vaadin.flow.component.html.Anchor downloadAnchor = new com.vaadin.flow.component.html.Anchor(resource, "");
            downloadAnchor.getElement().setAttribute("download", true);
            Button downloadBtn = new Button("Download PNG", new Icon(VaadinIcon.DOWNLOAD));
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            downloadAnchor.add(downloadBtn);

            Button closeBtn = new Button("Close", e -> dialog.close());
            dialog.getFooter().add(downloadAnchor, closeBtn);
            dialog.open();
        } catch (Exception ex) {
            Notification.show("Failed to generate barcode: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openProductDialog(ProductResponse product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product == null ? "Add New Product" : "Edit Product");

        TextField codeField = new TextField("Product Code");
        codeField.setRequired(true);
        codeField.setRequiredIndicatorVisible(true);

        TextField nameField = new TextField("Product Name");
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        TextField catField = new TextField("Category");
        TextArea descField = new TextArea("Description");

        ComboBox<Supplier> supplierCombo = new ComboBox<>("Supplier");
        List<Supplier> suppliers = supplierService.findAll();
        supplierCombo.setItems(suppliers);
        supplierCombo.setItemLabelGenerator(Supplier::getName);

        BigDecimalField purchasePriceField = new BigDecimalField("Purchase Price ($)");
        purchasePriceField.setRequiredIndicatorVisible(true);

        BigDecimalField sellingPriceField = new BigDecimalField("Selling Price ($)");
        sellingPriceField.setRequiredIndicatorVisible(true);

        IntegerField qtyField = new IntegerField("Stock Quantity");
        qtyField.setMin(0);
        IntegerField minStockField = new IntegerField("Minimum Stock Threshold");
        minStockField.setMin(0);
        IntegerField maxStockField = new IntegerField("Maximum Stock Threshold");
        maxStockField.setMin(0);
        DatePicker expiryField = new DatePicker("Expiry Date");

        FormLayout form = new FormLayout();
        form.add(codeField, nameField, catField, supplierCombo,
                purchasePriceField, sellingPriceField, qtyField, minStockField,
                maxStockField, expiryField, descField);
        form.setColspan(descField, 2);

        if (product != null) {
            codeField.setValue(product.getProductCode() != null ? product.getProductCode() : "");
            nameField.setValue(product.getName() != null ? product.getName() : "");
            catField.setValue(product.getCategory() != null ? product.getCategory() : "");
            descField.setValue(product.getDescription() != null ? product.getDescription() : "");
            purchasePriceField.setValue(product.getPurchasePrice());
            sellingPriceField.setValue(product.getSellingPrice());
            qtyField.setValue(product.getQuantity());
            minStockField.setValue(product.getMinimumStock());
            maxStockField.setValue(product.getMaximumStock());
            expiryField.setValue(product.getExpiryDate());

            if (product.getSupplierId() != null) {
                suppliers.stream()
                        .filter(s -> s.getId().equals(product.getSupplierId()))
                        .findFirst()
                        .ifPresent(supplierCombo::setValue);
            }
        } else {
            qtyField.setValue(0);
            minStockField.setValue(5);
            maxStockField.setValue(100);
            purchasePriceField.setValue(BigDecimal.ZERO);
            sellingPriceField.setValue(BigDecimal.ZERO);
        }

        Button saveBtn = new Button("Save", e -> {
            if (codeField.isEmpty() || nameField.isEmpty() || purchasePriceField.getValue() == null || sellingPriceField.getValue() == null) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                ProductRequest req = new ProductRequest(
                        codeField.getValue().trim(),
                        nameField.getValue().trim(),
                        catField.getValue() != null ? catField.getValue().trim() : null,
                        descField.getValue() != null ? descField.getValue().trim() : null,
                        supplierCombo.getValue() != null ? supplierCombo.getValue().getId() : null,
                        purchasePriceField.getValue(),
                        sellingPriceField.getValue(),
                        qtyField.getValue() != null ? qtyField.getValue() : 0,
                        minStockField.getValue() != null ? minStockField.getValue() : 0,
                        maxStockField.getValue() != null ? maxStockField.getValue() : 0,
                        expiryField.getValue()
                );

                if (product == null) {
                    productService.createProduct(req);
                    Notification.show("Product created successfully", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    productService.updateProduct(product.getId(), req);
                    Notification.show("Product updated successfully", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshGrid();
            } catch (DuplicateResourceException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error saving product: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.setWidth("650px");
        dialog.open();
    }

    private void confirmDelete(ProductResponse product) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Deletion");
        confirmDialog.add(new Span("Are you sure you want to delete product '" + product.getName() + "' (" + product.getProductCode() + ")?"));

        Button deleteBtn = new Button("Delete", e -> {
            try {
                productService.deleteById(product.getId());
                Notification.show("Product deleted successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                refreshGrid();
            } catch (BusinessException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error deleting product: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> confirmDialog.close());
        confirmDialog.getFooter().add(cancelBtn, deleteBtn);
        confirmDialog.open();
    }

    private void refreshGrid() {
        String search = searchField.getValue();
        String cat = categoryFilter.getValue();

        List<ProductResponse> items;
        if (search != null && !search.isBlank()) {
            items = productService.search(search.trim(), PageRequest.of(0, 100)).getContent();
        } else if (cat != null && !cat.isBlank()) {
            items = productService.findByCategory(cat.trim(), PageRequest.of(0, 100)).getContent();
        } else {
            items = productService.findAll(PageRequest.of(0, 100)).getContent();
        }
        grid.setItems(items);
    }
}
