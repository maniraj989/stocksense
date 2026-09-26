package com.stocksense;

import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.purchase.repository.PurchaseItemRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleRequest;
import com.stocksense.sales.repository.SaleItemRepository;
import com.stocksense.sales.repository.SaleRepository;
import com.stocksense.sales.service.SaleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransactionRollbackIntegrationTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private Long testSupplierId;
    private Long testProductId1;
    private Long testProductId2;

    @AfterEach
    void tearDown() {
        // Clean up test data if present
        if (testProductId1 != null && productRepository.existsById(testProductId1)) {
            stockMovementRepository.findByProductIdOrderByMovementDateDesc(testProductId1)
                    .forEach(stockMovementRepository::delete);
            productRepository.deleteById(testProductId1);
        }
        if (testProductId2 != null && productRepository.existsById(testProductId2)) {
            stockMovementRepository.findByProductIdOrderByMovementDateDesc(testProductId2)
                    .forEach(stockMovementRepository::delete);
            productRepository.deleteById(testProductId2);
        }
        if (testSupplierId != null && supplierRepository.existsById(testSupplierId)) {
            supplierRepository.deleteById(testSupplierId);
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Transaction: Purchase containing valid and invalid product rolls back atomically")
    void testPurchaseRollbackAtomicity() {
        Supplier supplier = supplierRepository.save(new Supplier("Rollback Sup", "Rollback Co", "555-9001", "rb@co.com", "Addr"));
        testSupplierId = supplier.getId();

        Product product1 = productRepository.save(new Product(
                "PRD-TX-01", "Tx Valid Prod 1", "Tx", "Desc", supplier,
                new BigDecimal("20.0000"), new BigDecimal("35.0000"), 50, 10, 200, null
        ));
        testProductId1 = product1.getId();

        long initialPurchasesCount = purchaseRepository.count();
        long initialPurchaseItemsCount = purchaseItemRepository.count();
        long initialMovementsCount = stockMovementRepository.count();

        // Attempt purchase with 1 valid product and 1 nonexistent product (ID: 999999L)
        PurchaseRequest purchaseRequest = new PurchaseRequest(
                supplier.getId(),
                List.of(
                        new PurchaseItemRequest(product1.getId(), 10, new BigDecimal("20.0000")),
                        new PurchaseItemRequest(999999L, 5, new BigDecimal("15.0000"))
                )
        );

        assertThatThrownBy(() -> purchaseService.createPurchase(purchaseRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999999");

        // Verify full rollback:
        // 1. Product 1 stock remains at initial 50
        Product reloadedProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        assertThat(reloadedProduct1.getQuantity()).isEqualTo(50);

        // 2. No purchase record created
        assertThat(purchaseRepository.count()).isEqualTo(initialPurchasesCount);

        // 3. No purchase items created
        assertThat(purchaseItemRepository.count()).isEqualTo(initialPurchaseItemsCount);

        // 4. No stock movement created
        assertThat(stockMovementRepository.count()).isEqualTo(initialMovementsCount);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Transaction: Sale containing item with insufficient stock rolls back atomically")
    void testSaleRollbackAtomicity() {
        Product product1 = productRepository.save(new Product(
                "PRD-TX-02", "Tx Prod Sufficient", "Tx", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 50, 10, 200, null
        ));
        testProductId1 = product1.getId();

        Product product2 = productRepository.save(new Product(
                "PRD-TX-03", "Tx Prod Insufficient", "Tx", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 5, 2, 50, null
        ));
        testProductId2 = product2.getId();

        long initialSalesCount = saleRepository.count();
        long initialSaleItemsCount = saleItemRepository.count();
        long initialMovementsCount = stockMovementRepository.count();

        // Attempt sale: item 1 requests 10 (available: 50), item 2 requests 20 (available: only 5)
        SaleRequest saleRequest = new SaleRequest(
                List.of(
                        new SaleItemRequest(product1.getId(), 10, new BigDecimal("20.0000")),
                        new SaleItemRequest(product2.getId(), 20, new BigDecimal("20.0000"))
                )
        );

        assertThatThrownBy(() -> saleService.createSale(saleRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        // Verify full rollback:
        // 1. Product 1 stock was NOT decremented
        Product reloadedProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        assertThat(reloadedProduct1.getQuantity()).isEqualTo(50);

        // 2. Product 2 stock was NOT changed
        Product reloadedProduct2 = productRepository.findById(product2.getId()).orElseThrow();
        assertThat(reloadedProduct2.getQuantity()).isEqualTo(5);

        // 3. No sale record was persisted
        assertThat(saleRepository.count()).isEqualTo(initialSalesCount);

        // 4. No sale item was persisted
        assertThat(saleItemRepository.count()).isEqualTo(initialSaleItemsCount);

        // 5. No stock movement was persisted
        assertThat(stockMovementRepository.count()).isEqualTo(initialMovementsCount);
    }
}
