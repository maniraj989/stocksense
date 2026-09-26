# StockSense — Legacy DAO to Spring Service & API Migration Matrix

This matrix documents the functional mapping from legacy JDBC DAOs (`src/com/inventory/dao/`) to modern Spring Data JPA repositories, transactional services, and REST APIs.

---

## 1. Migration Matrix

| Domain | Legacy DAO Method (`src/com/inventory/dao/`) | Spring Data Repository (`com.stocksense.*.repository`) | Spring Service Method (`com.stocksense.*.service`) | REST API Endpoint (`com.stocksense.*.controller`) |
| :--- | :--- | :--- | :--- | :--- |
| **Product** | `ProductDAO.addProduct()` | `ProductRepository.save()` | `ProductService.createProduct()` | `POST /api/products` |
| | `ProductDAO.updateProduct()` | `ProductRepository.save()` | `ProductService.updateProduct()` | `PUT /api/products/{id}` |
| | `ProductDAO.deleteProduct()` | `ProductRepository.deleteById()` | `ProductService.deleteProduct()` | `DELETE /api/products/{id}` |
| | `ProductDAO.getProductById()` | `ProductRepository.findById()` | `ProductService.getProductById()` | `GET /api/products/{id}` |
| | `ProductDAO.getProductByCode()` | `ProductRepository.findByProductCode()`| `ProductService.getProductByCode()` | `GET /api/products/code/{code}` |
| | `ProductDAO.getAllProducts()` | `ProductRepository.findAll(Pageable)` | `ProductService.getAllProducts(Pageable)`| `GET /api/products` |
| | `ProductDAO.searchProducts()` | `ProductRepository.findByNameContainingIgnoreCase()` | `ProductService.searchProducts(name, Pageable)` | `GET /api/products/search?q=...` |
| | `ProductDAO.getLowStockProducts()` | `ProductRepository.findLowStockProducts()` | `ProductService.getLowStockProducts()` | `GET /api/products/low-stock` |
| | `ProductDAO.updateStock()` | `ProductRepository.save()` | Handled via Purchase / Sale workflows | N/A (Atomic workflows) |
| **Supplier**| `SupplierDAO.addSupplier()` | `SupplierRepository.save()` | `SupplierService.createSupplier()` | `POST /api/suppliers` |
| | `SupplierDAO.updateSupplier()` | `SupplierRepository.save()` | `SupplierService.updateSupplier()` | `PUT /api/suppliers/{id}` |
| | `SupplierDAO.deleteSupplier()` | `SupplierRepository.deleteById()` | `SupplierService.deleteSupplier()` | `DELETE /api/suppliers/{id}` |
| | `SupplierDAO.getAllSuppliers()` | `SupplierRepository.findAll(Pageable)`| `SupplierService.getAllSuppliers(Pageable)`| `GET /api/suppliers` |
| | `SupplierDAO.getSupplierById()` | `SupplierRepository.findById()` | `SupplierService.getSupplierById()` | `GET /api/suppliers/{id}` |
| | `SupplierDAO.searchSuppliers()` | `SupplierRepository.findByNameContainingIgnoreCase()` | `SupplierService.searchSuppliers(name, Pageable)` | `GET /api/suppliers/search?q=...` |
| **Purchase**| `PurchaseDAO.addPurchase()` (manual JDBC TX) | `PurchaseRepository.save()` + `PurchaseItemRepository.saveAll()` | `PurchaseService.createPurchase()` (Atomic `@Transactional` + stock + movement) | `POST /api/purchases` |
| | `PurchaseDAO.getAllPurchases()` | `PurchaseRepository.findAll(Pageable)`| `PurchaseService.getAllPurchases(Pageable)`| `GET /api/purchases` |
| | `PurchaseDAO.getPurchaseById()` | `PurchaseRepository.findById()` | `PurchaseService.getPurchaseById()` | `GET /api/purchases/{id}` |
| **Sale** | `SaleDAO.addSale()` (manual JDBC TX) | `SaleRepository.save()` + `SaleItemRepository.saveAll()` | `SaleService.createSale()` (Atomic `@Transactional` + stock check + movement) | `POST /api/sales` |
| | `SaleDAO.getAllSales()` | `SaleRepository.findAll(Pageable)` | `SaleService.getAllSales(Pageable)` | `GET /api/sales` |
| | `SaleDAO.getSaleById()` | `SaleRepository.findById()` | `SaleService.getSaleById()` | `GET /api/sales/{id}` |
| **Stock** | `StockMovementDAO.addMovement()` | `StockMovementRepository.save()` | `StockMovementService.recordMovement()` | `POST /api/stock/adjustments` |
| | `StockMovementDAO.getAllMovements()`| `StockMovementRepository.findAll(Pageable)` | `StockMovementService.getAllMovements(Pageable)` | `GET /api/stock/movements` |
| | `StockMovementDAO.getMovementsByProduct()` | `StockMovementRepository.findByProductId()` | `StockMovementService.getMovementsByProduct(id, Pageable)` | `GET /api/stock/movements/product/{id}`|
| **Alert** | `AlertDAO.addAlert()` | `AlertRepository.save()` | `AlertService.createAlert()` | `POST /api/alerts` |
| | `AlertDAO.getActiveAlerts()` | `AlertRepository.findByStatus()` | `AlertService.getOpenAlerts(Pageable)` | `GET /api/alerts/open` |
| | `AlertDAO.resolveAlert()` | `AlertRepository.save()` | `AlertService.resolveAlert(id)` | `PUT /api/alerts/{id}/resolve` |
| | `AlertDAO.getAllAlerts()` | `AlertRepository.findAll(Pageable)` | `AlertService.getAllAlerts(Pageable)` | `GET /api/alerts` |

---

## 2. Legacy Preservation Guarantee

All classes in `src/com/inventory/` remain untouched. Swing views and legacy DAOs remain operational.
