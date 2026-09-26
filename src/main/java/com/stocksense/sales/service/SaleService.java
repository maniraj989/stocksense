package com.stocksense.sales.service;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertType;
import com.stocksense.notification.service.AlertService;
import com.stocksense.product.entity.Product;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleItemResponse;
import com.stocksense.sales.dto.SaleRequest;
import com.stocksense.sales.dto.SaleResponse;
import com.stocksense.sales.entity.Sale;
import com.stocksense.sales.entity.SaleItem;
import com.stocksense.sales.repository.SaleRepository;
import com.stocksense.user.entity.User;
import com.stocksense.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       StockMovementService stockMovementService,
                       AlertService alertService,
                       UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockMovementService = stockMovementService;
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Page<SaleResponse> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable)
                .map(SaleResponse::fromEntity);
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    public SaleResponse getById(Long id) {
        return saleRepository.findById(id)
                .map(SaleResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    public List<Sale> findByUserId(Long userId) {
        return saleRepository.findByCreatedById(userId);
    }

    public Page<SaleResponse> findByUserId(Long userId, Pageable pageable) {
        return saleRepository.findByCreatedById(userId, pageable)
                .map(SaleResponse::fromEntity);
    }

    public BigDecimal calculateRevenue(OffsetDateTime start, OffsetDateTime end) {
        return saleRepository.calculateTotalRevenueBetweenDates(start, end);
    }

    @Transactional
    public Sale save(Sale sale) {
        return saleRepository.save(sale);
    }

    @Transactional(rollbackFor = Exception.class)
    public SaleResponse createSale(SaleRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Sale items must not be empty");
        }

        User currentUser = getCurrentUser();

        Sale sale = new Sale();
        sale.setCreatedBy(currentUser);
        sale.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : OffsetDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> productsToUpdate = new ArrayList<>();
        List<SaleItem> itemsToRecord = new ArrayList<>();
        List<Product> lowStockAlerts = new ArrayList<>();

        for (SaleItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product id: " + itemReq.getProductId());
            }
            if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price must be positive or zero for product id: " + itemReq.getProductId());
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
            if (currentStock < itemReq.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product " + product.getProductCode()
                        + " (" + product.getName() + "). Available: " + currentStock
                        + ", Requested: " + itemReq.getQuantity());
            }

            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            SaleItem saleItem = new SaleItem(sale, product, itemReq.getQuantity(), itemReq.getUnitPrice(), subtotal);
            sale.addItem(saleItem);
            itemsToRecord.add(saleItem);

            int remainingStock = currentStock - itemReq.getQuantity();
            product.setQuantity(remainingStock);
            productsToUpdate.add(product);

            if (product.getMinimumStock() != null && remainingStock <= product.getMinimumStock()) {
                lowStockAlerts.add(product);
            }
        }

        sale.setTotalAmount(totalAmount);

        // Deduct product stock
        productRepository.saveAll(productsToUpdate);

        // Save sale and items
        Sale savedSale = saleRepository.save(sale);

        // Record stock movements atomically
        for (SaleItem item : itemsToRecord) {
            stockMovementService.recordMovement(
                    item.getProduct(),
                    MovementType.OUT,
                    item.getQuantity(),
                    savedSale.getId(),
                    currentUser
            );
        }

        // Trigger low-stock alerts if stock reached threshold
        for (Product lowStockProduct : lowStockAlerts) {
            alertService.createAlert(new Alert(
                    lowStockProduct,
                    AlertType.LOW_STOCK,
                    "Stock level (" + lowStockProduct.getQuantity() + ") is at or below minimum threshold (" + lowStockProduct.getMinimumStock() + ")",
                    AlertSeverity.HIGH
            ));
        }

        return SaleResponse.fromEntity(savedSale);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!saleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sale not found with id: " + id);
        }
        saleRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
