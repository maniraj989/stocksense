# StockSense — Phase 6 Service Layer Audit & Architecture Plan

**Document Version**: 1.0.0  
**Phase**: Phase 6 — Production-Grade Spring Service & REST API Layer

---

## 1. Executive Summary

This audit assesses the initial Phase 4 persistence services, reviews the legacy Swing JDBC DAOs in `src/com/inventory/dao/`, identifies gaps in transaction management, validation, and domain boundaries, and outlines the target DTO and Service Layer architecture for Phase 6.

---

## 2. Service Audit & Gap Analysis

| Domain Service | Current Phase 4 State | Legacy DAO Equivalent | Identified Gaps in Phase 4 | Target Phase 6 Enhancements |
| :--- | :--- | :--- | :--- | :--- |
| **ProductService** | Basic CRUD on entity | `ProductDAO` (`addProduct`, `updateProduct`, `updateStock`, `searchProducts`, `getLowStockProducts`) | Exposes raw entities; no duplicate SKU/product_code validation; no pagination; no DTO abstraction. | `ProductRequest`/`Response` DTOs; duplicate code detection with `DuplicateResourceException`; `Pageable` query support; input validation. |
| **SupplierService** | Basic CRUD on entity | `SupplierDAO` (`addSupplier`, `updateSupplier`, `deleteSupplier`, `searchSuppliers`) | Exposes raw entities; no FK constraint guard (deleting supplier referenced by purchases). | `SupplierRequest`/`Response` DTOs; conflict detection; guarded deletion; `Pageable` list. |
| **PurchaseService** | Simple entity CRUD | `PurchaseDAO` (`addPurchase` with manual JDBC transaction, `getPurchaseById`, `getAllPurchases`) | Completely lacks purchase creation business logic: did not validate items, did not increment product stock, did not record `STOCK IN` movements! | Atomic `@Transactional` purchase creation: validates supplier & products, calculates totals, updates stock, records movements, returns `PurchaseResponse`. |
| **SaleService** | Simple entity CRUD | `SaleDAO` (`addSale` with manual JDBC transaction, `getAllSales`, `getSaleById`) | Completely lacks sale creation business logic: did not check stock sufficiency, did not decrement stock, did not create `STOCK OUT` movements! | Atomic `@Transactional` sale creation: verifies stock availability (`InsufficientStockException`), updates inventory, auto-triggers low stock alerts, records movements. |
| **StockMovementService**| Single record insert | `StockMovementDAO` (`addMovement`, `getMovementsByProduct`) | No support for manual stock adjustments; exposes raw entities; no pagination. | Manual adjustment workflow (`MovementType.ADJUSTMENT`); DTO responses; paginated movement history. |
| **AlertService** | Basic CRUD on entity | `AlertDAO` (`addAlert`, `getActiveAlerts`, `resolveAlert`) | Exposes raw entities; no pagination. | `AlertResponse` DTO; resolve workflow; paginated queries by status and severity. |

---

## 3. Transaction Boundaries & Atomicity Plan

In the legacy code, `PurchaseDAO` and `SaleDAO` relied on error-prone manual JDBC transaction management:
```java
// Legacy manual JDBC pattern in PurchaseDAO.java:
connection.setAutoCommit(false);
// ... multiple statements ...
connection.commit();
// with rollback in catch block
```

In Phase 6, this is replaced by Declarative Spring Transaction Management (`@Transactional`):
1. **Purchase Workflow Boundary**:
   - Begins transaction on `PurchaseService.createPurchase()`.
   - Persists `Purchase` entity with cascading `PurchaseItem` entities.
   - For every item, fetches `Product`, increments `quantity`, and persists update.
   - Invokes `StockMovementService.recordMovement()` for every item with `MovementType.IN`.
   - Any failure (e.g. invalid product, negative qty/price) triggers full database rollback.
2. **Sale Workflow Boundary**:
   - Begins transaction on `SaleService.createSale()`.
   - For every item, checks `Product.quantity >= requestedQuantity`. If insufficient, throws `InsufficientStockException`.
   - Persists `Sale` entity with cascading `SaleItem` entities.
   - For every item, decrements `Product.quantity`.
   - If new quantity $\le$ `minimumStock`, creates `Alert` with `AlertType.LOW_STOCK`.
   - Invokes `StockMovementService.recordMovement()` with `MovementType.OUT`.
   - Full rollback if any failure occurs.

---

## 4. DTO Strategy & Information Protection

* **Strict Entity Isolation**: JPA entities are never exposed across `@RestController` endpoints.
* **Separation of Concerns**: Dedicated `*Request` and `*Response` DTOs prevent mass-assignment vulnerabilities.
* **Sensitive Field Redaction**: User passwords and internal entity proxies are never serialized to JSON.
* **Circular Reference Prevention**: Bidirectional relations (`Purchase` $\leftrightarrow$ `PurchaseItem`, `Sale` $\leftrightarrow$ `SaleItem`) use clean child DTOs to avoid recursion.
