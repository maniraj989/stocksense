package com.stocksense.product.service;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.DuplicateResourceException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.product.dto.ProductRequest;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.repository.PurchaseItemRepository;
import com.stocksense.sales.repository.SaleItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SaleItemRepository saleItemRepository;

    public ProductService(ProductRepository productRepository,
                          SupplierRepository supplierRepository,
                          PurchaseItemRepository purchaseItemRepository,
                          SaleItemRepository saleItemRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.saleItemRepository = saleItemRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductResponse::fromEntity);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Optional<Product> findByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }

    public ProductResponse getByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + productCode));
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    public Page<ProductResponse> findByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryIgnoreCase(category, pageable)
                .map(ProductResponse::fromEntity);
    }

    public Page<ProductResponse> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(query, query, pageable)
                .map(ProductResponse::fromEntity);
    }

    public List<ProductResponse> findLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public List<ProductResponse> findExpiringProducts(LocalDate targetDate) {
        return productRepository.findExpiringProducts(targetDate).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional
    public Product save(Product product) {
        if (product.getId() == null) {
            if (productRepository.existsByProductCode(product.getProductCode())) {
                throw new DuplicateResourceException("Product with code '" + product.getProductCode() + "' already exists");
            }
        } else {
            if (productRepository.existsByProductCodeAndIdNot(product.getProductCode(), product.getId())) {
                throw new DuplicateResourceException("Product with code '" + product.getProductCode() + "' already exists");
            }
        }
        return productRepository.save(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new DuplicateResourceException("Product with code '" + request.getProductCode() + "' already exists");
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        }

        Product product = new Product(
                request.getProductCode(),
                request.getName(),
                request.getCategory(),
                request.getDescription(),
                supplier,
                request.getPurchasePrice(),
                request.getSellingPrice(),
                request.getQuantity() != null ? request.getQuantity() : 0,
                request.getMinimumStock() != null ? request.getMinimumStock() : 0,
                request.getMaximumStock() != null ? request.getMaximumStock() : 0,
                request.getExpiryDate()
        );

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (productRepository.existsByProductCodeAndIdNot(request.getProductCode(), id)) {
            throw new DuplicateResourceException("Product with code '" + request.getProductCode() + "' already exists");
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        }

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setSupplier(supplier);
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getMinimumStock() != null) {
            product.setMinimumStock(request.getMinimumStock());
        }
        if (request.getMaximumStock() != null) {
            product.setMaximumStock(request.getMaximumStock());
        }
        product.setExpiryDate(request.getExpiryDate());

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        if (purchaseItemRepository.existsByProductId(id)) {
            throw new BusinessException("Cannot delete product because it has purchase history records");
        }

        if (saleItemRepository.existsByProductId(id)) {
            throw new BusinessException("Cannot delete product because it has sales history records");
        }

        productRepository.deleteById(id);
    }
}
