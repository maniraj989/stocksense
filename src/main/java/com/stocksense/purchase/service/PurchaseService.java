package com.stocksense.purchase.service;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.purchase.dto.PurchaseResponse;
import com.stocksense.purchase.entity.Purchase;
import com.stocksense.purchase.entity.PurchaseItem;
import com.stocksense.purchase.repository.PurchaseRepository;
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
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final UserRepository userRepository;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           StockMovementService stockMovementService,
                           UserRepository userRepository) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementService = stockMovementService;
        this.userRepository = userRepository;
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    public Page<PurchaseResponse> findAll(Pageable pageable) {
        return purchaseRepository.findAll(pageable)
                .map(PurchaseResponse::fromEntity);
    }

    public Optional<Purchase> findById(Long id) {
        return purchaseRepository.findById(id);
    }

    public PurchaseResponse getById(Long id) {
        return purchaseRepository.findById(id)
                .map(PurchaseResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
    }

    public List<Purchase> findBySupplierId(Long supplierId) {
        return purchaseRepository.findBySupplierId(supplierId);
    }

    public Page<PurchaseResponse> findBySupplierId(Long supplierId, Pageable pageable) {
        return purchaseRepository.findBySupplierId(supplierId, pageable)
                .map(PurchaseResponse::fromEntity);
    }

    @Transactional
    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Purchase items must not be empty");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        User currentUser = getCurrentUser();

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setCreatedBy(currentUser);
        purchase.setPurchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate() : OffsetDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> productsToUpdate = new ArrayList<>();
        List<PurchaseItem> itemsToRecord = new ArrayList<>();

        for (PurchaseItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product id: " + itemReq.getProductId());
            }
            if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price must be positive or zero for product id: " + itemReq.getProductId());
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            PurchaseItem purchaseItem = new PurchaseItem(purchase, product, itemReq.getQuantity(), itemReq.getUnitPrice(), subtotal);
            purchase.addItem(purchaseItem);
            itemsToRecord.add(purchaseItem);

            int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
            product.setQuantity(currentStock + itemReq.getQuantity());
            productsToUpdate.add(product);
        }

        purchase.setTotalAmount(totalAmount);

        // Save products with updated stock
        productRepository.saveAll(productsToUpdate);

        // Save purchase along with cascaded items
        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Record stock movements atomically
        for (PurchaseItem item : itemsToRecord) {
            stockMovementService.recordMovement(
                    item.getProduct(),
                    MovementType.IN,
                    item.getQuantity(),
                    savedPurchase.getId(),
                    currentUser
            );
        }

        return PurchaseResponse.fromEntity(savedPurchase);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!purchaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Purchase not found with id: " + id);
        }
        purchaseRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
