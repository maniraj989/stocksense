# Smart Inventory Management System - Architecture

## 1. Project architecture
The project follows a simple three-layer Java desktop architecture:

- Presentation layer: Swing panels and frames in the `gui` package
- Business layer: services in the `service` package
- Data access layer: DAO classes in the `dao` package

This keeps GUI code separate from database logic and allows beginners to explain each layer clearly during viva.

## 2. Package responsibilities
- `com.inventory.main`: application entry point (`Main.java`)
- `com.inventory.model`: domain classes like `User`, `Product`, `Supplier`, `Sale`, `Purchase`
- `com.inventory.dao`: database operations using JDBC and PreparedStatement
- `com.inventory.service`: business rules such as login, stock validation, reporting, stock monitoring
- `com.inventory.gui`: Java Swing user interface screens
- `com.inventory.util`: database and validation utilities
- `com.inventory.exception`: custom exceptions for invalid data and stock issues

## 3. ER diagram description
The main entities and relationships are:

- `users` has one-to-many relationship with `purchases` and `sales`
- `suppliers` has one-to-many relationship with `products` and `purchases`
- `products` has one-to-many relationship with `purchase_items`, `sale_items`, `stock_movements`, and `alerts`
- `purchases` is linked to `purchase_items`
- `sales` is linked to `sale_items`

The most important relationship is:

- each product belongs to one supplier
- one purchase can contain many products
- one sale can contain many products
- stock changes are tracked through the `stock_movements` table

## 4. Class diagram description
The hierarchy is built around the `User` class:

- `User` is the base class
- `Admin extends User`
- `Staff extends User`

Other core classes:

- `Product`
- `Supplier`
- `Purchase`
- `PurchaseItem`
- `Sale`
- `SaleItem`
- `StockMovement`
- `Alert`

Interfaces used:

- `StockAnalyzer` for low stock, expiry, and reorder detection
- `ReportGenerator` for sales and revenue reporting

This demonstrates abstraction, inheritance, polymorphism, and encapsulation in a straightforward academic style.

## 5. Use-case diagram description
Main use cases:

- Admin: login, manage products, manage suppliers, view sales, view reports, manage alerts
- Staff: login, search products, record sales, record purchase, view stock and alerts
- System: validate input, update stock, create alerts, generate dashboard statistics

## 6. APP requirement mapping
This project satisfies the major APP requirements as follows:

- OOP: classes, inheritance, encapsulation, runtime object usage
- Interfaces: `StockAnalyzer` and `ReportGenerator`
- Exceptions: invalid product, insufficient stock, product not found, duplicate record
- JDBC/MySQL: DAO classes use PreparedStatement and SQL transactions
- Swing GUI: dashboard, login, product, supplier, purchase, sales, alerts, reports
- Multithreading: background monitoring is represented by the `StockMonitoringService` and can be executed in a separate thread
- Role-based access: `Admin` and `Staff` are modelled separately and checked in the application flow

## 7. Team division
- Member 1: GUI and authentication
- Member 2: MySQL schema and DAO layer
- Member 3: Product, supplier, purchase, and sales flows
- Member 4: Stock alerts, monitoring, reorder logic, and reporting

## 8. Viva explanation
A student can explain the project in a viva by describing:

1. User logs in through the Swing login screen.
2. Controller logic validates form input and delegates to a service.
3. The service calls DAO methods that run SQL queries with PreparedStatement.
4. Database updates are committed or rolled back when transactions are used.
5. Background monitoring checks product health without freezing the UI.

## 9. Next modules
The codebase covers the essential design and implementation for the desktop inventory system, with the core modules already created under `src/com/inventory`.
