package com.stocksense.ui.inventory;

import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.entity.Product;
import com.stocksense.product.service.ProductService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Route(value = "stock", layout = MainLayout.class)
@PageTitle("Stock Overview — StockSense")
@PermitAll
public class StockView extends VerticalLayout {

    private final ProductService productService;
    private final StockMovementService stockMovementService;

    private final Grid<ProductResponse> grid = new Grid<>();

    public StockView(ProductService productService, StockMovementService stockMovementService) {
        this.productService = productService;
        this.stockMovementService = stockMovementService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Stock & Inventory Levels");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        Button adjustBtn = new Button("Manual Stock Adjustment", new Icon(VaadinIcon.SLIDERS), e -> openAdjustmentDialog());
        adjustBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(adjustBtn);
        toolbar.setWidthFull();

        configureGrid();
        add(title, toolbar, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(ProductResponse::getProductCode).setHeader("Product Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(ProductResponse::getName).setHeader("Product Name").setFlexGrow(2).setSortable(true);
        grid.addColumn(ProductResponse::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(ProductResponse::getQuantity).setHeader("Current Stock").setAutoWidth(true).setSortable(true);
        grid.addColumn(ProductResponse::getMinimumStock).setHeader("Min Threshold").setAutoWidth(true);
        grid.addColumn(ProductResponse::getMaximumStock).setHeader("Max Capacity").setAutoWidth(true);

        grid.addComponentColumn(p -> {
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            int min = p.getMinimumStock() != null ? p.getMinimumStock() : 0;
            Span badge = new Span();
            if (qty == 0) {
                badge.setText("Out of Stock");
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#dc2626");
            } else if (qty <= min) {
                badge.setText("Low Stock");
                badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
            } else {
                badge.setText("Optimal");
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
            }
            badge.getStyle()
                    .set("padding", "3px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Stock Status").setAutoWidth(true);
    }

    private void openAdjustmentDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Manual Stock Adjustment");

        ComboBox<Product> productCombo = new ComboBox<>("Product");
        productCombo.setItems(productService.findAll());
        productCombo.setItemLabelGenerator(p -> p.getProductCode() + " - " + p.getName() + " (Current: " + p.getQuantity() + ")");
        productCombo.setRequired(true);
        productCombo.setWidthFull();

        RadioButtonGroup<MovementType> typeRadio = new RadioButtonGroup<>("Movement Type");
        typeRadio.setItems(MovementType.IN, MovementType.OUT, MovementType.ADJUSTMENT);
        typeRadio.setValue(MovementType.ADJUSTMENT);

        IntegerField quantityInput = new IntegerField("Quantity");
        quantityInput.setValue(1);
        quantityInput.setHelperText("For IN/OUT: positive amount. For ADJUSTMENT: +/- offset.");

        FormLayout form = new FormLayout(productCombo, typeRadio, quantityInput);
        form.setColspan(productCombo, 2);

        Button saveBtn = new Button("Apply Adjustment", e -> {
            Product selected = productCombo.getValue();
            Integer qty = quantityInput.getValue();
            MovementType type = typeRadio.getValue();

            if (selected == null || qty == null || qty == 0) {
                Notification.show("Please select product and a non-zero quantity", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                StockAdjustmentRequest req = new StockAdjustmentRequest(selected.getId(), qty, type, null);
                stockMovementService.recordAdjustment(req);

                Notification.show("Stock adjustment applied successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (InsufficientStockException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Adjustment failed: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.setWidth("500px");
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(productService.findAll(PageRequest.of(0, 100)).getContent());
    }
}
