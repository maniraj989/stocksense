# StockSense — Legacy MySQL Schema Reference
**Original Source**: `sql/smart_inventory.sql`  
**Preservation Status**: Preserved intact (Read-only reference)

---

## 1. Tables Overview

| Table | Columns | Primary Key | Foreign Keys | Unique Keys |
| :--- | :--- | :--- | :--- | :--- |
| `users` | 7 | `id` (INT) | None | `username` |
| `suppliers` | 6 | `id` (INT) | None | None |
| `products` | 13 | `id` (INT) | `supplier_id` → `suppliers(id)` | `product_code` |
| `purchases` | 5 | `id` (INT) | `supplier_id` → `suppliers(id)`, `created_by` → `users(id)` | None |
| `purchase_items` | 6 | `id` (INT) | `purchase_id` → `purchases(id)`, `product_id` → `products(id)` | None |
| `sales` | 4 | `id` (INT) | `created_by` → `users(id)` | None |
| `sale_items` | 6 | `id` (INT) | `sale_id` → `sales(id)`, `product_id` → `products(id)` | None |
| `stock_movements` | 7 | `id` (INT) | `product_id` → `products(id)`, `created_by` → `users(id)` | None |
| `alerts` | 7 | `id` (INT) | `product_id` → `products(id)` | None |

---

## 2. Table Specifications

### `users`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `name`: VARCHAR(100) NOT NULL
* `username`: VARCHAR(50) NOT NULL UNIQUE
* `password`: VARCHAR(255) NOT NULL
* `role`: ENUM('ADMIN', 'STAFF') NOT NULL
* `email`: VARCHAR(100)
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### `suppliers`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `name`: VARCHAR(100) NOT NULL
* `company`: VARCHAR(150)
* `phone`: VARCHAR(30)
* `email`: VARCHAR(100)
* `address`: VARCHAR(255)

### `products`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `product_code`: VARCHAR(50) NOT NULL UNIQUE
* `name`: VARCHAR(150) NOT NULL
* `category`: VARCHAR(100)
* `description`: TEXT
* `supplier_id`: INT NULL (FK → suppliers.id ON DELETE SET NULL)
* `purchase_price`: DECIMAL(10,2) NOT NULL
* `selling_price`: DECIMAL(10,2) NOT NULL
* `quantity`: INT NOT NULL DEFAULT 0
* `minimum_stock`: INT NOT NULL DEFAULT 0
* `maximum_stock`: INT NOT NULL DEFAULT 0
* `expiry_date`: DATE NULL
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
* `updated_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

### `purchases`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `supplier_id`: INT NOT NULL (FK → suppliers.id ON DELETE RESTRICT)
* `purchase_date`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
* `total_amount`: DECIMAL(12,2) NOT NULL
* `created_by`: INT NULL (FK → users.id ON DELETE SET NULL)

### `purchase_items`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `purchase_id`: INT NOT NULL (FK → purchases.id ON DELETE CASCADE)
* `product_id`: INT NOT NULL (FK → products.id ON DELETE RESTRICT)
* `quantity`: INT NOT NULL
* `unit_price`: DECIMAL(10,2) NOT NULL
* `subtotal`: DECIMAL(12,2) NOT NULL

### `sales`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `sale_date`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
* `total_amount`: DECIMAL(12,2) NOT NULL
* `created_by`: INT NULL (FK → users.id ON DELETE SET NULL)

### `sale_items`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `sale_id`: INT NOT NULL (FK → sales.id ON DELETE CASCADE)
* `product_id`: INT NOT NULL (FK → products.id ON DELETE RESTRICT)
* `quantity`: INT NOT NULL
* `unit_price`: DECIMAL(10,2) NOT NULL
* `subtotal`: DECIMAL(12,2) NOT NULL

### `stock_movements`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `product_id`: INT NOT NULL (FK → products.id ON DELETE CASCADE)
* `movement_type`: ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL
* `quantity`: INT NOT NULL
* `reference_id`: INT NULL
* `movement_date`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
* `created_by`: INT NULL (FK → users.id ON DELETE SET NULL)

### `alerts`
* `id`: INT AUTO_INCREMENT PRIMARY KEY
* `product_id`: INT NOT NULL (FK → products.id ON DELETE CASCADE)
* `alert_type`: ENUM('LOW_STOCK', 'EXPIRY', 'REORDER') NOT NULL
* `message`: VARCHAR(255) NOT NULL
* `severity`: ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL
* `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
* `status`: ENUM('OPEN', 'RESOLVED') DEFAULT 'OPEN'
