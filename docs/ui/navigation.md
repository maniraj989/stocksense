# StockSense UI Navigation Sitemap

## Application Routes

| Path | View Class | Title | Required Role | Layout | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/login` | `LoginView` | Login — StockSense | Anonymous | None | Secure login screen with credentials authentication |
| `/` | `DashboardView` | Dashboard — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Main dashboard with inventory metrics & charts |
| `/dashboard` | `DashboardView` | Dashboard — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Explicit dashboard route |
| `/products` | `ProductsView` | Products — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Product catalog with search, filter, and CRUD dialogs |
| `/suppliers` | `SuppliersView` | Suppliers — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Supplier directory and contact details |
| `/purchases` | `PurchasesView` | Purchases — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Purchase order history and multi-item purchase builder |
| `/sales` | `SalesPosView` | Point of Sale — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Sales POS checkout terminal with real-time stock check |
| `/sales/history` | `SalesHistoryView` | Sales History — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Audit history of past completed sales |
| `/stock` | `StockView` | Stock Management — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Current inventory levels & manual stock adjustment |
| `/stock/movements`| `StockMovementsView` | Stock Movements — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Audit log of all IN, OUT, and ADJUSTMENT transfers |
| `/stock/low` | `LowStockView` | Low Stock Items — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Dedicated view for inventory items at or below minimum threshold |
| `/alerts` | `AlertsView` | System Alerts — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Real-time and persistent alerts with resolution action |
| `/admin/users` | `AdminUsersView` | User Management — StockSense | `ADMIN` only | `MainLayout` | User creation, role change, and status management |
| `/profile` | `ProfileView` | My Profile — StockSense | `STAFF`, `ADMIN` | `MainLayout` | Account details and authenticated password change |

---

## Navigation Drawer Hierarchy
1. **Overview**
   - Dashboard (`/dashboard`)
2. **Catalog & Inventory**
   - Products (`/products`)
   - Stock Overview (`/stock`)
   - Low Stock Alerts (`/stock/low`)
   - Stock Movements (`/stock/movements`)
3. **Purchasing & Vendors**
   - Suppliers (`/suppliers`)
   - Purchase Orders (`/purchases`)
4. **Sales & POS**
   - Point of Sale (`/sales`)
   - Sales History (`/sales/history`)
5. **System**
   - Alerts (`/alerts`)
   - Profile (`/profile`)
   - Administration (`/admin/users`) *[Visible only to ADMIN]*
