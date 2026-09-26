package com.stocksense.ui.sales;

import com.stocksense.barcode.service.BarcodeService;
import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.product.entity.Product;
import com.stocksense.product.service.ProductService;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleRequest;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.service.SaleService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Route(value = "sales", layout = MainLayout.class)
@PageTitle("Point of Sale (POS) — StockSense")
@PermitAll
public class SalesPosView extends VerticalLayout {

    private final ProductService productService;
    private final SaleService saleService;
    private final BarcodeService barcodeService;

    private final List<SaleItemRequest> cartItems = new ArrayList<>();
    private final Grid<CartItemModel> cartGrid = new Grid<>();
    private final Span totalAmountSpan = new Span("$0.00");
    private final Button completeSaleBtn = new Button("Complete Sale", new Icon(VaadinIcon.CHECK));

    private final TextField barcodeScanField = new TextField("Barcode / SKU Scan");
    private final ComboBox<Product> productSelector = new ComboBox<>("Or Select Product");
    private final Span stockStatusBadge = new Span();
    private final IntegerField quantityField = new IntegerField("Quantity");
    private final BigDecimalField priceField = new BigDecimalField("Selling Price ($)");

    public SalesPosView(ProductService productService, SaleService saleService, BarcodeService barcodeService) {
        this.productService = productService;
        this.saleService = saleService;
        this.barcodeService = barcodeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Point of Sale Terminal");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        HorizontalLayout mainContent = new HorizontalLayout(createProductSelectionPanel(), createCartPanel());
        mainContent.setSizeFull();
        mainContent.setFlexGrow(1, mainContent.getComponentAt(0));
        mainContent.setFlexGrow(2, mainContent.getComponentAt(1));

        add(title, mainContent);
        refreshProductList();
    }

    private VerticalLayout createProductSelectionPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "20px");
        panel.setWidth("380px");

        H3 panelHeader = new H3("Add Item to Cart");
        panelHeader.getStyle().set("margin-top", "0").set("color", "#1e293b");

        barcodeScanField.setWidthFull();
        barcodeScanField.setPlaceholder("Enter SKU or scan barcode...");
        barcodeScanField.setPrefixComponent(VaadinIcon.BARCODE.create());
        barcodeScanField.setClearButtonVisible(true);

        Button scanBtn = new Button("Lookup", new Icon(VaadinIcon.SEARCH), e -> handleBarcodeScan());
        scanBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout barcodeRow = new HorizontalLayout(barcodeScanField, scanBtn);
        barcodeRow.setWidthFull();
        barcodeRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        barcodeRow.setFlexGrow(1, barcodeScanField);

        barcodeScanField.addKeyDownListener(Key.ENTER, e -> handleBarcodeScan());

        productSelector.setWidthFull();
        productSelector.setItemLabelGenerator(p -> p.getProductCode() + " - " + p.getName());

        stockStatusBadge.setVisible(false);
        stockStatusBadge.getStyle()
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("padding", "2px 8px")
                .set("border-radius", "10px");

        quantityField.setValue(1);
        quantityField.setMin(1);
        quantityField.setWidthFull();

        priceField.setWidthFull();
        priceField.setReadOnly(true);

        productSelector.addValueChangeListener(e -> {
            Product selected = e.getValue();
            if (selected != null) {
                int stock = selected.getQuantity() != null ? selected.getQuantity() : 0;
                stockStatusBadge.setVisible(true);
                stockStatusBadge.setText("Available Stock: " + stock + " units");
                if (stock > selected.getMinimumStock()) {
                    stockStatusBadge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
                } else if (stock > 0) {
                    stockStatusBadge.getStyle().set("background-color", "#fef3c7").set("color", "#b45309");
                } else {
                    stockStatusBadge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
                }
                priceField.setValue(selected.getSellingPrice() != null ? selected.getSellingPrice() : BigDecimal.ZERO);
            } else {
                stockStatusBadge.setVisible(false);
                priceField.clear();
            }
        });

        Button addToCartBtn = new Button("Add to Cart", new Icon(VaadinIcon.PLUS), e -> addToCart());
        addToCartBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartBtn.setWidthFull();

        panel.add(panelHeader, barcodeRow, productSelector, stockStatusBadge, quantityField, priceField, addToCartBtn);
        return panel;
    }

    private void handleBarcodeScan() {
        String code = barcodeScanField.getValue();
        if (code == null || code.trim().isEmpty()) {
            Notification.show("Please enter or scan a barcode/SKU", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }

        String targetCode = code.trim();
        List<Product> products = productService.findAll();
        Product matchedProduct = products.stream()
                .filter(p -> Objects.equals(p.getProductCode(), targetCode))
                .findFirst()
                .orElse(null);

        if (matchedProduct != null) {
            productSelector.setValue(matchedProduct);
            quantityField.focus();
            Notification.show("Found: " + matchedProduct.getName() + " (SKU: " + matchedProduct.getProductCode() + ")",
                    2500, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            barcodeScanField.clear();
        } else {
            Notification.show("No product found matching barcode/SKU: " + targetCode,
                    4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            barcodeScanField.focus();
        }
    }

    private VerticalLayout createCartPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setSizeFull();
        panel.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "20px");

        H3 panelHeader = new H3("Current Order Cart");
        panelHeader.getStyle().set("margin-top", "0").set("color", "#1e293b");

        configureCartGrid();

        // Footer Summary
        HorizontalLayout summaryBar = new HorizontalLayout();
        summaryBar.setWidthFull();
        summaryBar.setAlignItems(FlexComponent.Alignment.CENTER);
        summaryBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        summaryBar.getStyle()
                .set("border-top", "2px solid #e2e8f0")
                .set("padding-top", "16px");

        Button clearBtn = new Button("Clear Cart", new Icon(VaadinIcon.ERASER), e -> clearCart());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Span totalLabel = new Span("Grand Total: ");
        totalLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "600").set("color", "#334155");

        totalAmountSpan.getStyle()
                .set("font-size", "1.6rem")
                .set("font-weight", "800")
                .set("color", "#059669");

        HorizontalLayout totalContainer = new HorizontalLayout(totalLabel, totalAmountSpan);
        totalContainer.setAlignItems(FlexComponent.Alignment.BASELINE);

        completeSaleBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        completeSaleBtn.addClickListener(e -> completeSale());
        completeSaleBtn.setEnabled(false);

        summaryBar.add(clearBtn, totalContainer, completeSaleBtn);

        panel.add(panelHeader, cartGrid, summaryBar);
        return panel;
    }

    private void configureCartGrid() {
        cartGrid.setSizeFull();
        cartGrid.addColumn(CartItemModel::getCode).setHeader("Product Code").setAutoWidth(true);
        cartGrid.addColumn(CartItemModel::getName).setHeader("Product").setFlexGrow(2);
        cartGrid.addColumn(CartItemModel::getQuantity).setHeader("Qty").setAutoWidth(true);
        cartGrid.addColumn(i -> "$" + i.getUnitPrice()).setHeader("Price").setAutoWidth(true);
        cartGrid.addColumn(i -> "$" + i.getSubtotal()).setHeader("Subtotal").setAutoWidth(true);

        cartGrid.addComponentColumn(item -> {
            Button removeBtn = new Button(new Icon(VaadinIcon.TRASH), e -> {
                cartItems.removeIf(req -> req.getProductId().equals(item.getProductId()));
                updateCartDisplay();
            });
            removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            return removeBtn;
        }).setHeader("Action").setAutoWidth(true);
    }

    private void addToCart() {
        Product product = productSelector.getValue();
        Integer qty = quantityField.getValue();
        BigDecimal price = priceField.getValue();

        if (product == null || qty == null || qty <= 0 || price == null) {
            Notification.show("Please select a product and valid quantity", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        int available = product.getQuantity() != null ? product.getQuantity() : 0;
        int inCart = cartItems.stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .mapToInt(SaleItemRequest::getQuantity)
                .sum();

        if (inCart + qty > available) {
            Notification.show("Requested quantity (" + (inCart + qty) + ") exceeds available stock (" + available + ")",
                            4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Add or increment
        boolean exists = false;
        for (SaleItemRequest req : cartItems) {
            if (req.getProductId().equals(product.getId())) {
                req.setQuantity(req.getQuantity() + qty);
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartItems.add(new SaleItemRequest(product.getId(), qty, price));
        }

        updateCartDisplay();
        productSelector.clear();
        quantityField.setValue(1);
    }

    private void updateCartDisplay() {
        List<CartItemModel> models = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest req : cartItems) {
            Product p = productService.findById(req.getProductId()).orElse(null);
            if (p != null) {
                BigDecimal sub = req.getUnitPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
                total = total.add(sub);
                models.add(new CartItemModel(p.getId(), p.getProductCode(), p.getName(), req.getQuantity(), req.getUnitPrice(), sub));
            }
        }

        cartGrid.setItems(models);
        totalAmountSpan.setText("$" + total);
        completeSaleBtn.setEnabled(!cartItems.isEmpty());
    }

    private void clearCart() {
        cartItems.clear();
        updateCartDisplay();
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            return;
        }

        try {
            completeSaleBtn.setEnabled(false);
            SaleRequest saleRequest = new SaleRequest(new ArrayList<>(cartItems));
            SaleResponse response = saleService.createSale(saleRequest);

            Notification.show("Sale #" + response.getId() + " completed successfully! Total: $" + response.getTotalAmount(),
                            4000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            clearCart();
            refreshProductList();
        } catch (InsufficientStockException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            completeSaleBtn.setEnabled(true);
        } catch (Exception ex) {
            Notification.show("Transaction failed: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            completeSaleBtn.setEnabled(true);
        }
    }

    private void refreshProductList() {
        productSelector.setItems(productService.findAll());
    }

    public static class CartItemModel {
        private final Long productId;
        private final String code;
        private final String name;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;

        public CartItemModel(Long productId, String code, String name, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.productId = productId;
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public Long getProductId() { return productId; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
