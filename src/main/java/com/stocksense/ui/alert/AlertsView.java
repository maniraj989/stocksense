package com.stocksense.ui.alert;

import com.stocksense.notification.dto.AlertResponse;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.service.AlertService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "alerts", layout = MainLayout.class)
@PageTitle("Alerts — StockSense")
@PermitAll
public class AlertsView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AlertService alertService;
    private final Grid<AlertResponse> grid = new Grid<>();
    private final ComboBox<AlertStatus> statusFilter = new ComboBox<>("Filter by Status");

    public AlertsView(AlertService alertService) {
        this.alertService = alertService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("System & Stock Alerts");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        HorizontalLayout toolbar = createToolbar();
        configureGrid();

        add(title, toolbar, grid);
        refreshGrid();
    }

    private HorizontalLayout createToolbar() {
        statusFilter.setItems(AlertStatus.values());
        statusFilter.setItemLabelGenerator(AlertStatus::name);
        statusFilter.setPlaceholder("All Statuses");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(statusFilter);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(a -> "#" + a.getId()).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(a -> a.getCreatedAt() != null ? a.getCreatedAt().format(DATE_FORMATTER) : "-")
                .setHeader("Created At").setAutoWidth(true).setSortable(true);

        grid.addComponentColumn(a -> {
            Span badge = new Span(a.getAlertType() != null ? a.getAlertType().name() : "-");
            badge.getStyle()
                    .set("background-color", "#e0e7ff")
                    .set("color", "#3730a3")
                    .set("padding", "2px 8px")
                    .set("border-radius", "8px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Alert Type").setAutoWidth(true);

        grid.addComponentColumn(a -> {
            Span badge = new Span(a.getSeverity() != null ? a.getSeverity().name() : "-");
            if (a.getSeverity() == AlertSeverity.HIGH) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#dc2626");
            } else if (a.getSeverity() == AlertSeverity.MEDIUM) {
                badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
            } else {
                badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0284c7");
            }
            badge.getStyle()
                    .set("padding", "2px 8px")
                    .set("border-radius", "8px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Severity").setAutoWidth(true);

        grid.addColumn(AlertResponse::getMessage).setHeader("Message").setFlexGrow(3);

        grid.addComponentColumn(a -> {
            boolean isOpen = a.getStatus() == AlertStatus.OPEN;
            Span badge = new Span(isOpen ? "OPEN" : "RESOLVED");
            badge.getStyle()
                    .set("background-color", isOpen ? "#fef2f2" : "#f1f5f9")
                    .set("color", isOpen ? "#991b1b" : "#64748b")
                    .set("padding", "2px 8px")
                    .set("border-radius", "8px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(a -> {
            if (a.getStatus() == AlertStatus.OPEN) {
                Button resolveBtn = new Button("Resolve", new Icon(VaadinIcon.CHECK), e -> {
                    alertService.markResolved(a.getId());
                    Notification.show("Alert #" + a.getId() + " marked as resolved", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refreshGrid();
                });
                resolveBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SUCCESS);
                return resolveBtn;
            } else {
                return new Span("Resolved");
            }
        }).setHeader("Action").setAutoWidth(true);
    }

    private void refreshGrid() {
        AlertStatus status = statusFilter.getValue();
        List<AlertResponse> items;
        if (status != null) {
            items = alertService.findByStatus(status, PageRequest.of(0, 100)).getContent();
        } else {
            items = alertService.findAll(PageRequest.of(0, 100)).getContent();
        }
        grid.setItems(items);
    }
}
