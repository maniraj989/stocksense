package com.stocksense;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.DuplicateResourceException;
import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.dto.StockMovementResponse;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.service.AlertService;
import com.stocksense.product.dto.ProductRequest;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.dto.SupplierRequest;
import com.stocksense.product.dto.SupplierResponse;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.purchase.dto.PurchaseResponse;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleRequest;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.repository.SaleRepository;
import com.stocksense.sales.service.SaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CoreServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private AlertService alertService;

    // =========================================================================
    // PRODUCT TESTS
    // =========================================================================

    @Test
    @DisplayName("Product: Create valid product succeeds and assigns ID")
    void testCreateProductSuccess() {
        Supplier supplier = supplierRepository.save(new Supplier("CoreProdSup", "Core Company", "555-0100", "core@sup.com", "123 Way"));

        ProductRequest req = new ProductRequest(
                "PRD-SRV-01", "Core Service Prod", "Gadgets", "Description",
                supplier.getId(), new BigDecimal("25.0000"), new BigDecimal("45.0000"),
                50, 10, 200, null
        );

        ProductResponse res = productService.createProduct(req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        assertThat(res.getProductCode()).isEqualTo("PRD-SRV-01");
        assertThat(res.getQuantity()).isEqualTo(50);
        assertThat(res.getSupplierId()).isEqualTo(supplier.getId());
    }

    @Test
    @DisplayName("Product: Duplicate product code throws DuplicateResourceException")
    void testCreateDuplicateProductCodeThrowsException() {
        ProductRequest req1 = new ProductRequest(
                "PRD-DUP-01", "Prod First", "Cat", "Desc",
                null, new BigDecimal("10.0000"), new BigDecimal("15.0000"),
                20, 5, 100, null
        );
        productService.createProduct(req1);

        ProductRequest req2 = new ProductRequest(
                "PRD-DUP-01", "Prod Duplicate", "Cat", "Desc",
                null, new BigDecimal("12.0000"), new BigDecimal("18.0000"),
                10, 5, 100, null
        );

        assertThatThrownBy(() -> productService.createProduct(req2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("PRD-DUP-01");
    }

    @Test
    @DisplayName("Product: Update product successfully alters fields")
    void testUpdateProductSuccess() {
        ProductRequest req = new ProductRequest(
                "PRD-UPD-01", "Initial Name", "Cat", "Desc",
                null, new BigDecimal("10.0000"), new BigDecimal("20.0000"),
                15, 5, 50, null
        );
        ProductResponse created = productService.createProduct(req);

        ProductRequest updateReq = new ProductRequest(
                "PRD-UPD-01", "Updated Name", "Cat Updated", "New Desc",
                null, new BigDecimal("12.0000"), new BigDecimal("24.0000"),
                25, 5, 50, null
        );

        ProductResponse updated = productService.updateProduct(created.getId(), updateReq);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getCategory()).isEqualTo("Cat Updated");
        assertThat(updated.getSellingPrice()).isEqualByComparingTo(new BigDecimal("24.0000"));
    }

    @Test
    @DisplayName("Product: Find by ID and ProductCode returns matching response")
    void testFindProductByIdAndCode() {
        ProductRequest req = new ProductRequest(
                "PRD-FND-01", "Find Me Prod", "Searchable", "Desc",
                null, new BigDecimal("5.0000"), new BigDecimal("10.0000"),
                30, 10, 100, null
        );
        ProductResponse created = productService.createProduct(req);

        ProductResponse byId = productService.getById(created.getId());
        assertThat(byId.getName()).isEqualTo("Find Me Prod");

        ProductResponse byCode = productService.getByProductCode("PRD-FND-01");
        assertThat(byCode.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Product: Find non-existent product throws ResourceNotFoundException")
    void testFindNonExistentProductThrowsException() {
        assertThatThrownBy(() -> productService.getById(999999L))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> productService.getByProductCode("NON-EXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================================
    // SUPPLIER TESTS
    // =========================================================================

    @Test
    @DisplayName("Supplier: Create valid supplier succeeds")
    void testCreateSupplierSuccess() {
        SupplierRequest req = new SupplierRequest("Alpha Supplies", "Alpha Corp", "555-1234", "alpha@supplies.com", "777 Market St");
        SupplierResponse res = supplierService.createSupplier(req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        assertThat(res.getName()).isEqualTo("Alpha Supplies");
    }

    @Test
    @DisplayName("Supplier: Duplicate supplier name throws DuplicateResourceException")
    void testDuplicateSupplierNameThrowsException() {
        SupplierRequest req1 = new SupplierRequest("Beta Supplies", "Beta Corp", "555-2345", "beta@corp.com", "Addr 1");
        supplierService.createSupplier(req1);

        SupplierRequest req2 = new SupplierRequest("Beta Supplies", "Beta Different", "555-9999", "beta2@corp.com", "Addr 2");
        assertThatThrownBy(() -> supplierService.createSupplier(req2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Beta Supplies");
    }

    @Test
    @DisplayName("Supplier: Guarded delete prevents deletion if referenced by product")
    void testSupplierGuardedDeleteWithProducts() {
        Supplier supplier = supplierRepository.save(new Supplier("Guarded Sup", "Guard Co", "555-3333", "guard@co.com", "Guard St"));
        Product product = new Product("PRD-GUARD-01", "Guarded Prod", "Guard", "Desc", supplier,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 10, 2, 50, null);
        productRepository.save(product);

        assertThatThrownBy(() -> supplierService.deleteById(supplier.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("products are associated");
    }

    // =========================================================================
    // PURCHASE TESTS
    // =========================================================================

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Purchase: Valid purchase creates purchase, increases stock, and records STOCK IN movement")
    void testCreatePurchaseSuccess() {
        Supplier supplier = supplierRepository.save(new Supplier("Purchasing Sup", "Purch Co", "555-4444", "purch@co.com", "Purch St"));
        Product product = productRepository.save(new Product("PRD-PUR-01", "Purch Prod", "Cat", "Desc", supplier,
                new BigDecimal("15.0000"), new BigDecimal("30.0000"), 10, 5, 100, null));

        PurchaseRequest purchaseReq = new PurchaseRequest(
                supplier.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 25, new BigDecimal("15.0000")))
        );

        PurchaseResponse purchaseRes = purchaseService.createPurchase(purchaseReq);

        assertThat(purchaseRes).isNotNull();
        assertThat(purchaseRes.getId()).isNotNull();
        assertThat(purchaseRes.getTotalAmount()).isEqualByComparingTo(new BigDecimal("375.0000"));
        assertThat(purchaseRes.getItems()).hasSize(1);

        // Verify stock increased
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(35); // 10 + 25

        // Verify stock movement recorded
        List<StockMovementResponse> movements = stockMovementService.findByProductId(product.getId(), PageRequest.of(0, 10)).getContent();
        assertThat(movements).isNotEmpty();
        assertThat(movements.get(0).getMovementType()).isEqualTo(MovementType.IN);
        assertThat(movements.get(0).getQuantity()).isEqualTo(25);
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Purchase: Nonexistent supplier rejected")
    void testPurchaseInvalidSupplierRejected() {
        Product product = productRepository.save(new Product("PRD-PUR-02", "Purch Prod 2", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 10, 5, 100, null));

        PurchaseRequest purchaseReq = new PurchaseRequest(
                999999L,
                List.of(new PurchaseItemRequest(product.getId(), 10, new BigDecimal("10.0000")))
        );

        assertThatThrownBy(() -> purchaseService.createPurchase(purchaseReq))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Purchase: Empty items list rejected")
    void testPurchaseEmptyItemsRejected() {
        Supplier supplier = supplierRepository.save(new Supplier("Purch Sup 2", "Purch Co 2", "555-5555", "p2@co.com", "Addr"));

        PurchaseRequest purchaseReq = new PurchaseRequest(supplier.getId(), List.of());

        assertThatThrownBy(() -> purchaseService.createPurchase(purchaseReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("items must not be empty");
    }

    // =========================================================================
    // SALE TESTS
    // =========================================================================

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Sale: Valid sale creates sale, decreases stock, and records STOCK OUT movement")
    void testCreateSaleSuccess() {
        Product product = productRepository.save(new Product("PRD-SAL-01", "Sale Prod", "Cat", "Desc", null,
                new BigDecimal("20.0000"), new BigDecimal("35.0000"), 50, 10, 100, null));

        SaleRequest saleReq = new SaleRequest(
                List.of(new SaleItemRequest(product.getId(), 15, new BigDecimal("35.0000")))
        );

        SaleResponse saleRes = saleService.createSale(saleReq);

        assertThat(saleRes).isNotNull();
        assertThat(saleRes.getId()).isNotNull();
        assertThat(saleRes.getTotalAmount()).isEqualByComparingTo(new BigDecimal("525.0000"));

        // Verify stock decreased
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(35); // 50 - 15

        // Verify stock movement
        List<StockMovementResponse> movements = stockMovementService.findByProductId(product.getId(), PageRequest.of(0, 10)).getContent();
        assertThat(movements).isNotEmpty();
        assertThat(movements.get(0).getMovementType()).isEqualTo(MovementType.OUT);
        assertThat(movements.get(0).getQuantity()).isEqualTo(15);
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Sale: Insufficient stock throws InsufficientStockException")
    void testSaleInsufficientStockThrowsException() {
        Product product = productRepository.save(new Product("PRD-SAL-02", "Low Qty Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 5, 2, 50, null));

        SaleRequest saleReq = new SaleRequest(
                List.of(new SaleItemRequest(product.getId(), 20, new BigDecimal("20.0000")))
        );

        assertThatThrownBy(() -> saleService.createSale(saleReq))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        // Verify stock remained unchanged
        Product unchanged = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(5);
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Sale: Low stock threshold generates alert automatically")
    void testSaleTriggersLowStockAlert() {
        Product product = productRepository.save(new Product("PRD-ALERT-01", "Alert Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 10, 5, 50, null));

        // Sell 6 units, leaving 4 units (below minStock of 5)
        SaleRequest saleReq = new SaleRequest(
                List.of(new SaleItemRequest(product.getId(), 6, new BigDecimal("20.0000")))
        );

        saleService.createSale(saleReq);

        Page<com.stocksense.notification.dto.AlertResponse> alerts = alertService.findByProductId(product.getId(), PageRequest.of(0, 5));
        assertThat(alerts.getContent()).isNotEmpty();
        assertThat(alerts.getContent().get(0).getMessage()).contains("below minimum threshold");
    }

    // =========================================================================
    // STOCK ADJUSTMENT TESTS
    // =========================================================================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Stock: Adjustment updates stock and records movement")
    void testStockAdjustmentSuccess() {
        Product product = productRepository.save(new Product("PRD-ADJ-01", "Adj Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("15.0000"), 20, 5, 50, null));

        StockAdjustmentRequest req = new StockAdjustmentRequest(product.getId(), 10, MovementType.IN, null);
        StockMovementResponse res = stockMovementService.recordAdjustment(req);

        assertThat(res.getQuantity()).isEqualTo(10);
        assertThat(res.getMovementType()).isEqualTo(MovementType.IN);

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(30);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Stock: Negative adjustment beyond available stock throws InsufficientStockException")
    void testNegativeStockAdjustmentThrowsException() {
        Product product = productRepository.save(new Product("PRD-ADJ-02", "Adj Prod 2", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("15.0000"), 5, 2, 50, null));

        StockAdjustmentRequest req = new StockAdjustmentRequest(product.getId(), -10, MovementType.ADJUSTMENT, null);

        assertThatThrownBy(() -> stockMovementService.recordAdjustment(req))
                .isInstanceOf(InsufficientStockException.class);
    }
}
