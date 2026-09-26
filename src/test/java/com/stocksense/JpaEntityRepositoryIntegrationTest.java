package com.stocksense;

import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.entity.StockMovement;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.entity.AlertType;
import com.stocksense.notification.repository.AlertRepository;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.purchase.entity.PurchaseItem;
import com.stocksense.purchase.repository.PurchaseItemRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.sales.entity.Sale;
import com.stocksense.sales.entity.SaleItem;
import com.stocksense.sales.repository.SaleItemRepository;
import com.stocksense.sales.repository.SaleRepository;
import com.stocksense.user.entity.User;
import com.stocksense.user.entity.UserRole;
import com.stocksense.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaEntityRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    @DisplayName("User Entity & UserRepository: Save, find by username, role, and ID")
    void testUserPersistence() {
        User user = new User("Test Admin", "testadmin", "password123", UserRole.ADMIN, "testadmin@stocksense.com");
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<User> found = userRepository.findByUsername("testadmin");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(found.get().getName()).isEqualTo("Test Admin");

        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        assertThat(admins).extracting(User::getUsername).contains("testadmin");
    }

    @Test
    @DisplayName("Supplier Entity & SupplierRepository: Save, find by company and name")
    void testSupplierPersistence() {
        Supplier supplier = new Supplier("Alpha Supply Co", "Alpha Corp", "1234567890", "alpha@supply.com", "123 Industrial Way");
        Supplier saved = supplierRepository.save(supplier);

        assertThat(saved.getId()).isNotNull();

        List<Supplier> suppliers = supplierRepository.findByNameContainingIgnoreCase("alpha");
        assertThat(suppliers).isNotEmpty();
        assertThat(suppliers.get(0).getCompany()).isEqualTo("Alpha Corp");
    }

    @Test
    @DisplayName("Product Entity & ProductRepository: Save with BigDecimal money, supplier relation, and stock queries")
    void testProductPersistence() {
        Supplier supplier = supplierRepository.save(new Supplier("Beta Tech", "Beta Corp", "9876543210", "beta@corp.com", "456 Tech Park"));

        Product product = new Product(
                "SKU-TEST-001",
                "Ergonomic Keyboard",
                "Electronics",
                "Mechanical keyboard",
                supplier,
                new BigDecimal("45.5000"),
                new BigDecimal("89.9900"),
                5,
                10,
                50,
                LocalDate.now().plusMonths(6)
        );

        Product saved = productRepository.save(product);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<Product> found = productRepository.findByProductCode("SKU-TEST-001");
        assertThat(found).isPresent();
        assertThat(found.get().getSupplier().getName()).isEqualTo("Beta Tech");
        assertThat(found.get().getPurchasePrice()).isEqualByComparingTo(new BigDecimal("45.5000"));
        assertThat(found.get().getSellingPrice()).isEqualByComparingTo(new BigDecimal("89.9900"));

        List<Product> lowStock = productRepository.findLowStockProducts();
        assertThat(lowStock).extracting(Product::getProductCode).contains("SKU-TEST-001");
    }

    @Test
    @DisplayName("Purchase & PurchaseItem: Save cascading purchase items, verify relations and totals")
    void testPurchaseAndItemPersistence() {
        User user = userRepository.save(new User("Staff Purchaser", "purchaser", "secret", UserRole.STAFF, "purchaser@stocksense.com"));
        Supplier supplier = supplierRepository.save(new Supplier("Delta Distro", "Delta LLC", "5551234567", "delta@distro.com", "789 Freight Rd"));
        Product product = productRepository.save(new Product(
                "SKU-PUR-001",
                "USB Cable",
                "Accessories",
                "Type-C Cable",
                supplier,
                new BigDecimal("3.0000"),
                new BigDecimal("8.0000"),
                100,
                20,
                200,
                null
        ));

        Purchase purchase = new Purchase(supplier, new BigDecimal("60.0000"), user);
        PurchaseItem item = new PurchaseItem(purchase, product, 20, new BigDecimal("3.0000"), new BigDecimal("60.0000"));
        purchase.addItem(item);

        Purchase savedPurchase = purchaseRepository.save(purchase);
        assertThat(savedPurchase.getId()).isNotNull();
        assertThat(savedPurchase.getItems()).hasSize(1);

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(savedPurchase.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProduct().getProductCode()).isEqualTo("SKU-PUR-001");
        assertThat(items.get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("60.0000"));
    }

    @Test
    @DisplayName("Sale & SaleItem: Save cascading sale items, calculate revenue and verify relations")
    void testSaleAndItemPersistence() {
        User user = userRepository.save(new User("Cashier One", "cashier1", "secret", UserRole.STAFF, "cashier1@stocksense.com"));
        Supplier supplier = supplierRepository.save(new Supplier("Gamma Goods", "Gamma Ltd", "7778889999", "gamma@goods.com", "321 Retail St"));
        Product product = productRepository.save(new Product(
                "SKU-SALE-001",
                "Wireless Mouse",
                "Electronics",
                "Optical mouse",
                supplier,
                new BigDecimal("10.0000"),
                new BigDecimal("25.0000"),
                50,
                10,
                100,
                null
        ));

        Sale sale = new Sale(new BigDecimal("50.0000"), user);
        SaleItem item = new SaleItem(sale, product, 2, new BigDecimal("25.0000"), new BigDecimal("50.0000"));
        sale.addItem(item);

        Sale savedSale = saleRepository.save(sale);
        assertThat(savedSale.getId()).isNotNull();
        assertThat(savedSale.getItems()).hasSize(1);

        List<SaleItem> items = saleItemRepository.findBySaleId(savedSale.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);

        OffsetDateTime start = OffsetDateTime.now().minusHours(1);
        OffsetDateTime end = OffsetDateTime.now().plusHours(1);
        BigDecimal revenue = saleRepository.calculateTotalRevenueBetweenDates(start, end);
        assertThat(revenue).isGreaterThanOrEqualTo(new BigDecimal("50.0000"));
    }

    @Test
    @DisplayName("StockMovement: Save movement, verify MovementType enum and product relation")
    void testStockMovementPersistence() {
        User user = userRepository.save(new User("Stock Manager", "stockmgr", "pass", UserRole.ADMIN, "mgr@stocksense.com"));
        Product product = productRepository.save(new Product(
                "SKU-STK-001",
                "HDMI Cable",
                "Cables",
                "High Speed HDMI",
                null,
                new BigDecimal("2.5000"),
                new BigDecimal("7.0000"),
                30,
                5,
                100,
                null
        ));

        StockMovement movement = new StockMovement(product, MovementType.IN, 15, 101L, user);
        StockMovement saved = stockMovementRepository.save(movement);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMovementDate()).isNotNull();
        assertThat(saved.getMovementType()).isEqualTo(MovementType.IN);

        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByMovementDateDesc(product.getId());
        assertThat(movements).isNotEmpty();
        assertThat(movements.get(0).getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("Alert: Save alert, verify enums (type, severity, status) and status filtering")
    void testAlertPersistence() {
        Product product = productRepository.save(new Product(
                "SKU-ALT-001",
                "Flash Drive 64GB",
                "Storage",
                "USB 3.0",
                null,
                new BigDecimal("5.0000"),
                new BigDecimal("12.0000"),
                2,
                10,
                50,
                null
        ));

        Alert alert = new Alert(product, AlertType.LOW_STOCK, "Stock is below minimum threshold (2 remaining)", AlertSeverity.HIGH);
        Alert saved = alertRepository.save(alert);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(saved.getAlertType()).isEqualTo(AlertType.LOW_STOCK);

        List<Alert> openAlerts = alertRepository.findByStatus(AlertStatus.OPEN);
        assertThat(openAlerts).extracting(Alert::getId).contains(saved.getId());

        saved.setStatus(AlertStatus.RESOLVED);
        alertRepository.save(saved);

        Optional<Alert> updated = alertRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(AlertStatus.RESOLVED);
    }
}
