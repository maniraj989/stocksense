package com.stocksense.ui.inventory;

import com.stocksense.inventory.dto.StockMovementResponse;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.ui.layout.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;

@Route(value = "stock/movements", layout = MainLayout.class)
@PageTitle("Stock Movements — StockSense")
@PermitAll
public class StockMovementsView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StockMovementService stockMovementService;
    private final Grid<StockMovementResponse> grid = new Grid<>();

    public StockMovementsView(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Stock Movement Audit Log");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        configureGrid();
        add(title, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(m -> "#" + m.getId()).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(m -> m.getMovementDate() != null ? m.getMovementDate().format(DATE_FORMATTER) : "-")
                .setHeader("Date & Time").setAutoWidth(true).setSortable(true);
        grid.addColumn(StockMovementResponse::getProductCode).setHeader("Product Code").setAutoWidth(true);
        grid.addColumn(StockMovementResponse::getProductName).setHeader("Product Name").setFlexGrow(2);

        grid.addComponentColumn(m -> {
            Span badge = new Span(m.getMovementType() != null ? m.getMovementType().name() : "-");
            if (m.getMovementType() == MovementType.IN) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
            } else if (m.getMovementType() == MovementType.OUT) {
                badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
            } else {
                badge.getStyle().set("background-color", "#fef3c7").set("color", "#b45309");
            }
            badge.getStyle()
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Movement Type").setAutoWidth(true);

        grid.addColumn(StockMovementResponse::getQuantity).setHeader("Quantity").setAutoWidth(true);
        grid.addColumn(m -> m.getReferenceId() != null ? "#" + m.getReferenceId() : "-")
                .setHeader("Reference ID").setAutoWidth(true);
        grid.addColumn(m -> m.getCreatedByUsername() != null ? m.getCreatedByUsername() : "System")
                .setHeader("Logged By").setAutoWidth(true);
    }

    private void refreshGrid() {
        grid.setItems(stockMovementService.findAll(PageRequest.of(0, 100)).getContent());
    }
}
