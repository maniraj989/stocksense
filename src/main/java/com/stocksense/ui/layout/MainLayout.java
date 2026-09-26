package com.stocksense.ui.layout;

import com.stocksense.ui.admin.AdminUsersView;
import com.stocksense.ui.alert.AlertsView;
import com.stocksense.ui.dashboard.DashboardView;
import com.stocksense.ui.inventory.LowStockView;
import com.stocksense.ui.inventory.StockMovementsView;
import com.stocksense.ui.inventory.StockView;
import com.stocksense.ui.product.ProductsView;
import com.stocksense.ui.profile.ProfileView;
import com.stocksense.ui.purchase.PurchasesView;
import com.stocksense.ui.sales.SalesHistoryView;
import com.stocksense.ui.sales.SalesPosView;
import com.stocksense.ui.supplier.SuppliersView;
import com.stocksense.ui.util.SecurityUtils;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MainLayout extends AppLayout {

    private final SecurityUtils securityUtils;

    public MainLayout(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;

        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        H2 appTitle = new H2("StockSense");
        appTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0")
                .set("font-weight", "700")
                .set("color", "#1e3a8a");

        Span subtitle = new Span("Smart Inventory");
        subtitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "#64748b")
                .set("margin-left", "8px");

        HorizontalLayout branding = new HorizontalLayout(appTitle, subtitle);
        branding.setAlignItems(FlexComponent.Alignment.BASELINE);

        // User info & Logout on right
        String username = securityUtils.getCurrentUsername();
        boolean isAdmin = securityUtils.isAdmin();

        Span userLabel = new Span(username);
        userLabel.getStyle().set("font-weight", "600");

        Span roleBadge = new Span(isAdmin ? "ADMIN" : "STAFF");
        roleBadge.getElement().getThemeList().add("badge");
        if (isAdmin) {
            roleBadge.getStyle()
                    .set("background-color", "#f3e8ff")
                    .set("color", "#7e22ce")
                    .set("font-size", "0.75rem")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("font-weight", "600");
        } else {
            roleBadge.getStyle()
                    .set("background-color", "#e0f2fe")
                    .set("color", "#0369a1")
                    .set("font-size", "0.75rem")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("font-weight", "600");
        }

        Button profileBtn = new Button(new Icon(VaadinIcon.USER), e -> getUI().ifPresent(ui -> ui.navigate(ProfileView.class)));
        profileBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        profileBtn.setTooltipText("My Profile");

        Button logoutBtn = new Button("Log out", new Icon(VaadinIcon.SIGN_OUT), e -> securityUtils.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout userArea = new HorizontalLayout(userLabel, roleBadge, profileBtn, logoutBtn);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(toggle, branding, userArea);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(branding);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        header.getStyle()
                .set("border-bottom", "1px solid #e2e8f0")
                .set("padding", "8px 16px");

        addToNavbar(true, header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        // 1. Overview
        SideNavItem dashboardItem = new SideNavItem("Dashboard", "", VaadinIcon.DASHBOARD.create());
        nav.addItem(dashboardItem);

        // 2. Catalog & Inventory
        SideNavItem productsItem = new SideNavItem("Products", "products", VaadinIcon.PACKAGE.create());
        SideNavItem stockItem = new SideNavItem("Stock Overview", "stock", VaadinIcon.STORAGE.create());
        SideNavItem lowStockItem = new SideNavItem("Low Stock Items", "stock/low", VaadinIcon.WARNING.create());
        SideNavItem movementsItem = new SideNavItem("Stock Movements", "stock/movements", VaadinIcon.EXCHANGE.create());
        nav.addItem(productsItem, stockItem, lowStockItem, movementsItem);

        // 3. Purchasing
        SideNavItem suppliersItem = new SideNavItem("Suppliers", "suppliers", VaadinIcon.TRUCK.create());
        SideNavItem purchasesItem = new SideNavItem("Purchases", "purchases", VaadinIcon.CART.create());
        nav.addItem(suppliersItem, purchasesItem);

        // 4. Sales / POS
        SideNavItem salesPosItem = new SideNavItem("Point of Sale (POS)", "sales", VaadinIcon.CASH.create());
        SideNavItem salesHistoryItem = new SideNavItem("Sales History", "sales/history", VaadinIcon.RECORDS.create());
        nav.addItem(salesPosItem, salesHistoryItem);

        // 5. Intelligence & Reports
        SideNavItem reportsItem = new SideNavItem("Reports & Analytics", "reports", VaadinIcon.CHART.create());
        nav.addItem(reportsItem);

        // 6. System
        SideNavItem alertsItem = new SideNavItem("Alerts", "alerts", VaadinIcon.BELL.create());
        SideNavItem profileItem = new SideNavItem("My Profile", "profile", VaadinIcon.USER.create());
        nav.addItem(alertsItem, profileItem);

        // Administration (Visible only to ADMIN)
        if (securityUtils.isAdmin()) {
            SideNavItem adminItem = new SideNavItem("Administration", "admin/users", VaadinIcon.USERS.create());
            nav.addItem(adminItem);
        }

        Scroller scroller = new Scroller(nav);
        scroller.setClassName("p-s");

        H4 navTitle = new H4("Navigation");
        navTitle.getStyle()
                .set("padding", "16px 16px 8px 16px")
                .set("margin", "0")
                .set("color", "#94a3b8")
                .set("font-size", "0.75rem")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em");

        VerticalLayout drawerLayout = new VerticalLayout(navTitle, scroller);
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setSizeFull();

        addToDrawer(drawerLayout);
    }
}
