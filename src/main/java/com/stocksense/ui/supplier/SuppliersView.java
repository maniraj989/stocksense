package com.stocksense.ui.supplier;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.DuplicateResourceException;
import com.stocksense.product.dto.SupplierRequest;
import com.stocksense.product.dto.SupplierResponse;
import com.stocksense.product.service.SupplierService;
import com.stocksense.ui.layout.MainLayout;
import com.stocksense.ui.util.SecurityUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Route(value = "suppliers", layout = MainLayout.class)
@PageTitle("Suppliers — StockSense")
@PermitAll
public class SuppliersView extends VerticalLayout {

    private final SupplierService supplierService;
    private final SecurityUtils securityUtils;

    private final Grid<SupplierResponse> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public SuppliersView(SupplierService supplierService, SecurityUtils securityUtils) {
        this.supplierService = supplierService;
        this.securityUtils = securityUtils;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Supplier Directory");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        HorizontalLayout toolbar = createToolbar();
        configureGrid();

        add(title, toolbar, grid);
        refreshGrid();
    }

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Search by name or company...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("320px");

        Button addSupplierBtn = new Button("Add Supplier", new Icon(VaadinIcon.PLUS), e -> openSupplierDialog(null));
        addSupplierBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, addSupplierBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(SupplierResponse::getName).setHeader("Supplier Name").setFlexGrow(2).setSortable(true);
        grid.addColumn(SupplierResponse::getCompany).setHeader("Company").setFlexGrow(2).setSortable(true);
        grid.addColumn(SupplierResponse::getPhone).setHeader("Phone").setAutoWidth(true);
        grid.addColumn(SupplierResponse::getEmail).setHeader("Email").setFlexGrow(2);
        grid.addColumn(SupplierResponse::getAddress).setHeader("Address").setFlexGrow(3);

        grid.addComponentColumn(s -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT), e -> openSupplierDialog(s));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            editBtn.setTooltipText("Edit Supplier");
            actions.add(editBtn);

            if (securityUtils.isAdmin()) {
                Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(s));
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
                deleteBtn.setTooltipText("Delete Supplier (Admin)");
                actions.add(deleteBtn);
            }
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openSupplierDialog(SupplierResponse supplier) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(supplier == null ? "Add New Supplier" : "Edit Supplier");

        TextField nameField = new TextField("Contact Name");
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        TextField companyField = new TextField("Company Name");
        TextField phoneField = new TextField("Phone Number");
        EmailField emailField = new EmailField("Email Address");
        TextField addressField = new TextField("Physical Address");

        FormLayout form = new FormLayout(nameField, companyField, phoneField, emailField, addressField);
        form.setColspan(addressField, 2);

        if (supplier != null) {
            nameField.setValue(supplier.getName() != null ? supplier.getName() : "");
            companyField.setValue(supplier.getCompany() != null ? supplier.getCompany() : "");
            phoneField.setValue(supplier.getPhone() != null ? supplier.getPhone() : "");
            emailField.setValue(supplier.getEmail() != null ? supplier.getEmail() : "");
            addressField.setValue(supplier.getAddress() != null ? supplier.getAddress() : "");
        }

        Button saveBtn = new Button("Save", e -> {
            if (nameField.isEmpty()) {
                Notification.show("Supplier contact name is required", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                SupplierRequest req = new SupplierRequest(
                        nameField.getValue().trim(),
                        companyField.getValue() != null ? companyField.getValue().trim() : null,
                        phoneField.getValue() != null ? phoneField.getValue().trim() : null,
                        emailField.getValue() != null ? emailField.getValue().trim() : null,
                        addressField.getValue() != null ? addressField.getValue().trim() : null
                );

                if (supplier == null) {
                    supplierService.createSupplier(req);
                    Notification.show("Supplier created successfully", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    supplierService.updateSupplier(supplier.getId(), req);
                    Notification.show("Supplier updated successfully", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshGrid();
            } catch (DuplicateResourceException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error saving supplier: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.setWidth("550px");
        dialog.open();
    }

    private void confirmDelete(SupplierResponse supplier) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Deletion");
        confirmDialog.add(new Span("Are you sure you want to delete supplier '" + supplier.getName() + "'?"));

        Button deleteBtn = new Button("Delete", e -> {
            try {
                supplierService.deleteById(supplier.getId());
                Notification.show("Supplier deleted successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                refreshGrid();
            } catch (BusinessException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error deleting supplier: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
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
        List<SupplierResponse> items;
        if (search != null && !search.isBlank()) {
            items = supplierService.search(search.trim(), PageRequest.of(0, 100)).getContent();
        } else {
            items = supplierService.findAll(PageRequest.of(0, 100)).getContent();
        }
        grid.setItems(items);
    }
}
