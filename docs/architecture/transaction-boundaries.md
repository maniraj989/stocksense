# StockSense — Transaction Boundaries & Atomicity Architecture

This document defines the transaction boundaries, isolation levels, propagation rules, and rollback semantics implemented in **Phase 6**.

---

## 1. Principles of Business Atomicity

StockSense manages critical physical and financial inventory state. Partial operations lead to inventory discrepancy and financial loss. Therefore:
* All multi-step mutations are wrapped in Spring `@Transactional` boundaries.
* If any step fails (e.g. stock deficit, constraint violation, foreign key failure), the transaction rolls back completely.
* Read operations explicitly declare `@Transactional(readOnly = true)` to optimize Hibernate dirty-checking and connection allocation.

---

## 2. Core Workflow Transaction Boundaries

### A. Purchase Receiving Workflow (`PurchaseService.createPurchase`)

```text
CLIENT REQUEST
     │
     ▼
[ BEGIN TRANSACTION ] ──────────────────────────────────────────┐
     │                                                          │
     ├─► 1. Verify Supplier existence in DB                     │
     ├─► 2. Validate non-empty items & non-negative amounts     │
     ├─► 3. Insert Purchase header record                       │
     ├─► 4. Insert PurchaseItem records (Cascaded)              │
     ├─► 5. For each item:                                      │
     │      ├─► Fetch Product entity                            │
     │      ├─► Increment Product.quantity (+qty)               │
     │      └─► Save updated Product                            │
     ├─► 6. For each item:                                      │
     │      └─► Record StockMovement (Type: IN, Ref: PURCHASE)  │
     │                                                          │
     ▼                                                          ▼
[ COMMIT TRANSACTION ]                                [ ROLLBACK ON ANY ERROR ]
```

### B. Sale Checkout Workflow (`SaleService.createSale`)

```text
CLIENT REQUEST
     │
     ▼
[ BEGIN TRANSACTION ] ──────────────────────────────────────────┐
     │                                                          │
     ├─► 1. Validate non-empty items & positive quantities      │
     ├─► 2. For each item:                                      │
     │      ├─► Fetch Product entity                            │
     │      └─► Verify Product.quantity >= item.quantity        │
     │          (If False: throw InsufficientStockException) ───┤ ──► [ ROLLBACK ]
     ├─► 3. Insert Sale header record                           │
     ├─► 4. Insert SaleItem records (Cascaded)                  │
     ├─► 5. For each item:                                      │
     │      ├─► Decrement Product.quantity (-qty)               │
     │      └─► Save updated Product                            │
     ├─► 6. For each item:                                      │
     │      ├─► If quantity <= minimumStock:                    │
     │      │   └─► Create Alert (Type: LOW_STOCK)              │
     │      └─► Record StockMovement (Type: OUT, Ref: SALE)     │
     │                                                          │
     ▼                                                          ▼
[ COMMIT TRANSACTION ]                                [ ROLLBACK ON ANY ERROR ]
```

### C. Manual Stock Adjustment Workflow (`StockMovementService.recordAdjustment`)

```text
CLIENT REQUEST
     │
     ▼
[ BEGIN TRANSACTION ] ──────────────────────────────────────────┐
     │                                                          │
     ├─► 1. Fetch Product entity                                │
     ├─► 2. Verify (product.quantity + adjustment) >= 0         │
     │      (If False: throw InsufficientStockException) ───┤ ──► [ ROLLBACK ]
     ├─► 3. Update Product.quantity                             │
     ├─► 4. If quantity <= minimumStock:                        │
     │      └─► Create Alert (Type: LOW_STOCK)                  │
     ├─► 5. Insert StockMovement (Type: ADJUSTMENT)             │
     │                                                          │
     ▼                                                          ▼
[ COMMIT TRANSACTION ]                                [ ROLLBACK ON ANY ERROR ]
```
