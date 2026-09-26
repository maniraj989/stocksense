# StockSense — Safe MySQL to PostgreSQL Data Migration Procedure

This guide describes how to migrate existing MySQL database rows to the new PostgreSQL database without data loss, downtime risk, or schema corruption.

---

## Migration Pipeline Architecture

```text
[ Existing MySQL 8 Database ]
              │
              ▼  (1) Logical Dump / Export (mysqldump / CSV)
   [ Staged Export Files ]
              │
              ▼  (2) Schema Baseline (Flyway V1 creates empty PostgreSQL schema)
[ Target PostgreSQL Database ]
              │
              ▼  (3) Data Transformation (Type normalization, identity sequences)
   [ Transformed Inserts / COPY ]
              │
              ▼  (4) Import & Sequence Alignment
[ PostgreSQL with Full Legacy Data ]
              │
              ▼  (5) Row-Count & Checksum Validation
     [ Verification Passed ]
```

---

## Step-by-Step Execution

### Step 1: Export Legacy MySQL Data
Dump data only (without DDL) using `--no-create-info` and `--complete-insert`:
```bash
mysqldump -u root -p --no-create-info --complete-insert --skip-triggers smart_inventory > legacy_data_dump.sql
```

### Step 2: Flyway Initial Schema Creation
Let Flyway execute `V1__initial_postgresql_schema.sql` on the PostgreSQL `stocksense` database:
```bash
mvn flyway:migrate
```

### Step 3: Import Order to Respect Foreign Keys
Import data in dependency order:
1. `users`
2. `suppliers`
3. `products`
4. `purchases`
5. `purchase_items`
6. `sales`
7. `sale_items`
8. `stock_movements`
9. `alerts`

### Step 4: Synchronize PostgreSQL Identity Sequences
After inserting legacy rows with explicit primary key IDs, update the identity sequences to avoid primary key collisions:
```sql
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('suppliers', 'id'), COALESCE(MAX(id), 1)) FROM suppliers;
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE(MAX(id), 1)) FROM products;
SELECT setval(pg_get_serial_sequence('purchases', 'id'), COALESCE(MAX(id), 1)) FROM purchases;
SELECT setval(pg_get_serial_sequence('purchase_items', 'id'), COALESCE(MAX(id), 1)) FROM purchase_items;
SELECT setval(pg_get_serial_sequence('sales', 'id'), COALESCE(MAX(id), 1)) FROM sales;
SELECT setval(pg_get_serial_sequence('sale_items', 'id'), COALESCE(MAX(id), 1)) FROM sale_items;
SELECT setval(pg_get_serial_sequence('stock_movements', 'id'), COALESCE(MAX(id), 1)) FROM stock_movements;
SELECT setval(pg_get_serial_sequence('alerts', 'id'), COALESCE(MAX(id), 1)) FROM alerts;
```

### Step 5: Verification & Reconciliation
Execute row-count parity check between MySQL and PostgreSQL:
```sql
SELECT 'users' AS tbl, count(*) FROM users
UNION ALL SELECT 'suppliers', count(*) FROM suppliers
UNION ALL SELECT 'products', count(*) FROM products
UNION ALL SELECT 'purchases', count(*) FROM purchases
UNION ALL SELECT 'purchase_items', count(*) FROM purchase_items
UNION ALL SELECT 'sales', count(*) FROM sales
UNION ALL SELECT 'sale_items', count(*) FROM sale_items
UNION ALL SELECT 'stock_movements', count(*) FROM stock_movements
UNION ALL SELECT 'alerts', count(*) FROM alerts;
```
