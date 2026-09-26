# StockSense — Persistence Architecture & JPA Notes

This document details the Spring Data JPA and Hibernate 6 architecture implemented in **Phase 4** for the **StockSense** inventory system.

---

## 1. Domain Model to PostgreSQL Mapping

| Entity | Table Name | Key Mapping Highlights | Relationships & Fetch Policy |
| :--- | :--- | :--- | :--- |
| `User` | `users` | `id` (Identity), `role` (`UserRole` ENUM: `ADMIN`, `STAFF`), `createdAt` (`OffsetDateTime`) | Standalone principal entity. |
| `Supplier` | `suppliers` | `id` (Identity), `name`, `company`, `phone`, `email`, `address` | Root supplier entity. |
| `Product` | `products` | `id` (Identity), `productCode` (Unique), `purchasePrice` & `sellingPrice` (`BigDecimal` precision 19, scale 4), `expiryDate` (`LocalDate`), `createdAt`/`updatedAt` (`OffsetDateTime`) | `@ManyToOne(fetch = LAZY)` to `Supplier`. |
| `Purchase` | `purchases` | `id` (Identity), `purchaseDate` (`OffsetDateTime`), `totalAmount` (`BigDecimal` 19,4) | `@ManyToOne(fetch = LAZY)` to `Supplier` and `User` (`createdBy`). Bidirectional `@OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)` to `PurchaseItem`. |
| `PurchaseItem` | `purchase_items` | `id` (Identity), `unitPrice` & `subtotal` (`BigDecimal` 19,4), `quantity` (`Integer`) | `@ManyToOne(fetch = LAZY)` to `Purchase` and `Product`. |
| `Sale` | `sales` | `id` (Identity), `saleDate` (`OffsetDateTime`), `totalAmount` (`BigDecimal` 19,4) | `@ManyToOne(fetch = LAZY)` to `User` (`createdBy`). Bidirectional `@OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)` to `SaleItem`. |
| `SaleItem` | `sale_items` | `id` (Identity), `unitPrice` & `subtotal` (`BigDecimal` 19,4), `quantity` (`Integer`) | `@ManyToOne(fetch = LAZY)` to `Sale` and `Product`. |
| `StockMovement` | `stock_movements` | `id` (Identity), `movementType` (`MovementType` ENUM: `IN`, `OUT`, `ADJUSTMENT`), `quantity` (`Integer`), `referenceId` (`Long`), `movementDate` (`OffsetDateTime`) | `@ManyToOne(fetch = LAZY)` to `Product` and `User` (`createdBy`). |
| `Alert` | `alerts` | `id` (Identity), `alertType` (`AlertType` ENUM: `LOW_STOCK`, `EXPIRY`, `REORDER`), `severity` (`AlertSeverity` ENUM: `LOW`, `MEDIUM`, `HIGH`), `status` (`AlertStatus` ENUM: `OPEN`, `RESOLVED`) | `@ManyToOne(fetch = LAZY)` to `Product`. |

---

## 2. Hibernate Validation Strategy

The persistence layer is configured with:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```
* **Schema Ownership**: Flyway owns all DDL mutations (`V1__initial_postgresql_schema.sql`).
* **Validation**: Hibernate validates all entity definitions against the live PostgreSQL metadata during startup. The application will fail fast if any column type, nullability, primary key, or foreign key constraint mismatches.

---

## 3. Concurrency Control & Future Locking Strategy

During high-concurrency warehouse and POS operations, concurrent stock updates can cause lost updates or race conditions (e.g. two cashiers selling the last unit simultaneously):

1. **Operations Requiring Concurrency Control**:
   - `Product.quantity` deduction during sale checkout.
   - `Product.quantity` addition during purchase receiving.
   - Manual inventory adjustments and audits.
2. **Planned Locking Mechanisms (Future Phases)**:
   - **Pessimistic Locking**: `PESSIMISTIC_WRITE` via `@Lock(LockModeType.PESSIMISTIC_WRITE)` in `ProductRepository` during transactional order checkout:
     ```java
     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT p FROM Product p WHERE p.id = :id")
     Optional<Product> findByIdWithLock(@Param("id") Long id);
     ```
   - **Optimistic Locking**: Will be evaluated when adding an explicit `version` column in future Flyway migrations (`V...`) for multi-step workflows.

---

## 4. Coexistence with Legacy JDBC Layer

* **Legacy JDBC Layer**: [`src/com/inventory/dao/`](file:///D:/stockesense-main/src/com/inventory/dao) and raw JDBC connections remain 100% operational and untouched.
* **Modern Spring Data Layer**: Operates side-by-side using the shared PostgreSQL database and transaction managers.
