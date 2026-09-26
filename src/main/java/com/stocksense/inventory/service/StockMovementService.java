package com.stocksense.inventory.service;

import com.stocksense.common.exception.InsufficientStockException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.dto.StockMovementResponse;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.entity.StockMovement;
import com.stocksense.inventory.repository.StockMovementRepository;
import com.stocksense.product.entity.Product;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.user.entity.User;
import com.stocksense.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<StockMovement> findAll() {
        return stockMovementRepository.findAll();
    }

    public Page<StockMovementResponse> findAll(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByMovementDateDesc(pageable)
                .map(StockMovementResponse::fromEntity);
    }

    public Optional<StockMovement> findById(Long id) {
        return stockMovementRepository.findById(id);
    }

    public StockMovementResponse getById(Long id) {
        return stockMovementRepository.findById(id)
                .map(StockMovementResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
    }

    public List<StockMovement> findByProductId(Long productId) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId);
    }

    public Page<StockMovementResponse> findByProductId(Long productId, Pageable pageable) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId, pageable)
                .map(StockMovementResponse::fromEntity);
    }

    public List<StockMovement> findByMovementType(MovementType type) {
        return stockMovementRepository.findByMovementType(type);
    }

    public Page<StockMovementResponse> findByMovementType(MovementType type, Pageable pageable) {
        return stockMovementRepository.findByMovementTypeOrderByMovementDateDesc(type, pageable)
                .map(StockMovementResponse::fromEntity);
    }

    @Transactional
    public StockMovement recordMovement(StockMovement movement) {
        return stockMovementRepository.save(movement);
    }
    public List<StockMovementResponse> findFilteredMovements(Long productId, MovementType movementType, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.time.OffsetDateTime start = startDate != null ? startDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC) : null;
        java.time.OffsetDateTime end = endDate != null ? endDate.atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC) : null;
        return stockMovementRepository.findFilteredMovements(productId, movementType, start, end).stream()
                .map(StockMovementResponse::fromEntity)
                .toList();
    }

    @Transactional
    public StockMovement recordMovement(Product product, MovementType type, int quantity, Long referenceId, User createdBy) {
        StockMovement movement = new StockMovement(product, type, quantity, referenceId, createdBy);
        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovementResponse recordAdjustment(StockAdjustmentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        User currentUser = getCurrentUser();
        MovementType type = request.getMovementType() != null ? request.getMovementType() : MovementType.ADJUSTMENT;
        int qty = request.getQuantity();

        int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
        int newStock;

        if (type == MovementType.IN) {
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity for IN movement must be positive");
            }
            newStock = currentStock + qty;
        } else if (type == MovementType.OUT) {
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity for OUT movement must be positive");
            }
            if (currentStock < qty) {
                throw new InsufficientStockException("Insufficient stock for product " + product.getProductCode()
                        + ". Available: " + currentStock + ", Requested: " + qty);
            }
            newStock = currentStock - qty;
        } else {
            // ADJUSTMENT
            newStock = currentStock + qty;
            if (newStock < 0) {
                throw new InsufficientStockException("Adjustment would result in negative stock (" + newStock
                        + ") for product " + product.getProductCode());
            }
        }

        product.setQuantity(newStock);
        productRepository.save(product);

        StockMovement movement = new StockMovement(product, type, Math.abs(qty), request.getReferenceId(), currentUser);
        StockMovement saved = stockMovementRepository.save(movement);
        return StockMovementResponse.fromEntity(saved);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
