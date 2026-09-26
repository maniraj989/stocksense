# StockSense — Core REST API Reference

**Base URL**: `/api`  
**Security**: HTTP Basic Authentication or Session Cookie. Standard REST APIs require `ROLE_STAFF` or `ROLE_ADMIN`. Administrative routes require `ROLE_ADMIN`.

---

## 1. Endpoints Overview

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | **Public** | System health verification |
| `GET` | `/api/auth/me` | Authenticated | Retrieve authenticated user profile |
| `POST`| `/api/auth/change-password` | Authenticated | Change current user password |
| `GET` | `/api/admin/users` | `ADMIN` only | List all system users |
| `POST`| `/api/admin/users` | `ADMIN` only | Create a new user |
| `DELETE`| `/api/admin/users/{id}` | `ADMIN` only | Delete a user |
| `GET` | `/api/products` | Authenticated | List products (paginated) |
| `GET` | `/api/products/{id}` | Authenticated | Get product by ID |
| `GET` | `/api/products/code/{code}` | Authenticated | Get product by SKU/Code |
| `GET` | `/api/products/search?q={query}` | Authenticated | Search products by name/category |
| `GET` | `/api/products/low-stock` | Authenticated | List products at or below minimum threshold |
| `POST`| `/api/products` | `ADMIN` only | Create new product |
| `PUT` | `/api/products/{id}` | `ADMIN` only | Update product details |
| `DELETE`| `/api/products/{id}` | `ADMIN` only | Delete product |
| `GET` | `/api/suppliers` | Authenticated | List suppliers (paginated) |
| `GET` | `/api/suppliers/{id}` | Authenticated | Get supplier by ID |
| `GET` | `/api/suppliers/search?q={query}`| Authenticated | Search suppliers |
| `POST`| `/api/suppliers` | `ADMIN` only | Create new supplier |
| `PUT` | `/api/suppliers/{id}` | `ADMIN` only | Update supplier details |
| `DELETE`| `/api/suppliers/{id}` | `ADMIN` only | Delete supplier (guarded) |
| `GET` | `/api/purchases` | Authenticated | List purchases (paginated) |
| `GET` | `/api/purchases/{id}` | Authenticated | Get purchase details with items |
| `POST`| `/api/purchases` | Authenticated | Create atomic purchase & update stock |
| `GET` | `/api/sales` | Authenticated | List sales (paginated) |
| `GET` | `/api/sales/{id}` | Authenticated | Get sale details with items |
| `POST`| `/api/sales` | Authenticated | Create atomic sale & deduct stock |
| `GET` | `/api/stock/movements` | Authenticated | List stock movements (paginated) |
| `GET` | `/api/stock/movements/product/{id}`| Authenticated | List movements for a product |
| `POST`| `/api/stock/adjustments` | `ADMIN` only | Record manual stock adjustment |
| `GET` | `/api/alerts` | Authenticated | List all alerts (paginated) |
| `GET` | `/api/alerts/open` | Authenticated | List open alerts |
| `PUT` | `/api/alerts/{id}/resolve` | Authenticated | Resolve an alert |

---

## 2. Standard Error Response Format

```json
{
  "timestamp": "2026-09-26T12:00:00Z",
  "status": 400,
  "error": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock for product P1001: requested 10, available 4",
  "path": "/api/sales"
}
```
