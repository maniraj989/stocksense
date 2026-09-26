# Phase 7 — StockSense Web UI Architecture

## Overview
StockSense Phase 7 implements a modern, responsive, 100% Java-based enterprise Web UI using **Vaadin Flow 24.4.11**, backed by Spring Boot 3.3.4, Spring Security 6, Spring Data JPA, and PostgreSQL 16.4.

No external client-side frameworks (React, Angular, Vue, Next.js) or Node.js runtime layers are used. The entire business, component, validation, and security model executes on the server in Java.

---

## Architectural Layers

```
Browser Client (Vaadin Flow Component Engine / Web Components)
               ↓ ↑ (WebSocket / HTTP push / DOM sync)
Vaadin Flow UI Layer (com.stocksense.ui.*)
  ├── Shell / Navigation: MainLayout (AppLayout)
  ├── Views: LoginView, DashboardView, ProductsView, SuppliersView,
  │          PurchasesView, SalesPosView, SalesHistoryView, StockView,
  │          StockMovementsView, LowStockView, AlertsView, AdminUsersView,
  │          ProfileView
  └── Components: StatCard, DialogForms, Grids, Badges
               ↓
Spring Security Context / AuthenticationContext (RBAC: ADMIN, STAFF)
               ↓
Spring Service Layer (com.stocksense.*.service.*)
  ├── ProductService, SupplierService, PurchaseService,
  │   SaleService, StockMovementService, AlertService, UserService
               ↓
Spring Data JPA Repositories (com.stocksense.*.repository.*)
               ↓
PostgreSQL 16.4 Database (10 Relations, Flyway V1 Baseline)
```

---

## Package Organization
```
com.stocksense.ui
├── layout
│   └── MainLayout.java            # AppLayout shell with header, sidebar, user profile, logout
├── login
│   └── LoginView.java             # Secured login form with error notifications
├── dashboard
│   ├── DashboardView.java         # KPI cards, low stock list, recent activity tables
│   └── component
│       └── StatCard.java          # Reusable statistic summary widget
├── product
│   └── ProductsView.java          # Product catalog, search, filter, dialog form
├── supplier
│   └── SuppliersView.java         # Supplier catalog, search, edit dialog, guarded delete
├── purchase
│   └── PurchasesView.java         # Purchase order creation, multi-item builder, history
├── sales
│   ├── SalesPosView.java          # POS / Checkout cart with stock verification
│   └── SalesHistoryView.java      # Sales audit history and item breakdown
├── inventory
│   ├── StockView.java             # Current inventory levels and stock adjustment dialog
│   ├── StockMovementsView.java    # Full stock movement audit log (IN/OUT/ADJUSTMENT)
│   └── LowStockView.java          # Authoritative low-stock alerts and deficits
├── alert
│   └── AlertsView.java            # Open/resolved alerts list with manual resolution
├── admin
│   └── AdminUsersView.java        # ADMIN-only user management (Role assignment, deactivate)
├── profile
│   └── ProfileView.java           # Current user info and secure password update
└── util
    └── SecurityUtils.java         # Helper for authentication context and role checks
```

---

## Design System Tokens & Aesthetics
- **Theme**: Lumo (Vaadin default enterprise theme) with customized CSS variables.
- **Palette**:
  - Primary: Deep Indigo / Cobalt (`#1e40af`, `#2563eb`)
  - Surface: Pure white `#ffffff` with subtle borders (`#e2e8f0`)
  - Status Indicators:
    - Success (In stock / Resolved): Emerald Green (`#059669`)
    - Warning (Low stock / Medium severity): Amber (`#d97706`)
    - Danger (Out of stock / High severity / Error): Crimson (`#dc2626`)
    - Info (Stock movements / Inbound): Sky Blue (`#0284c7`)
- **Typography**: Inter / system font with clear weights (600 for headers, 500 for labels, 400 for content).
- **Responsive Breakpoints**:
  - Mobile (< 768px): Collapsible drawer, stacked form layouts, compact grids.
  - Desktop (>= 768px): Permanent sidebar navigation, multi-column forms and statistics.
