package com.stocksense.ui.dashboard.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class StatCard extends Div {

    public StatCard(String title, String value, Icon icon, String color, String caption) {
        getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("box-shadow", "0 1px 3px 0 rgba(0, 0, 0, 0.05)")
                .set("min-width", "200px")
                .set("flex", "1");

        icon.setSize("24px");
        icon.setColor(color);

        Div iconContainer = new Div(icon);
        iconContainer.getStyle()
                .set("background-color", color + "15") // 15% opacity hex
                .set("border-radius", "8px")
                .set("padding", "10px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "44px")
                .set("height", "44px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("color", "#64748b")
                .set("font-size", "0.875rem")
                .set("font-weight", "500");

        H3 valueHeader = new H3(value);
        valueHeader.getStyle()
                .set("margin", "4px 0 0 0")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#0f172a");

        VerticalLayout textLayout = new VerticalLayout(titleSpan, valueHeader);
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        HorizontalLayout topRow = new HorizontalLayout(iconContainer, textLayout);
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setSpacing(true);

        add(topRow);

        if (caption != null && !caption.isBlank()) {
            Span captionSpan = new Span(caption);
            captionSpan.getStyle()
                    .set("color", "#94a3b8")
                    .set("font-size", "0.75rem")
                    .set("margin-top", "8px")
                    .set("display", "block");
            add(captionSpan);
        }
    }
}
