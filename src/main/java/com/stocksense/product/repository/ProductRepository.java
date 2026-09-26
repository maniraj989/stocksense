package com.stocksense.product.repository;

import com.stocksense.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    boolean existsBySupplierId(Long supplierId);

    List<Product> findByCategoryIgnoreCase(String category);

    org.springframework.data.domain.Page<Product> findByCategoryIgnoreCase(String category, org.springframework.data.domain.Pageable pageable);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByNameContainingIgnoreCase(String name);

    org.springframework.data.domain.Page<Product> findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(String name, String productCode, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.minimumStock")
    List<Product> findLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= p.minimumStock")
    Long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    Long countOutOfStockProducts();

    @Query("SELECT COALESCE(SUM(p.quantity * p.purchasePrice), 0) FROM Product p")
    BigDecimal calculateTotalInventoryCostValue();

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p")
    BigDecimal calculateTotalInventorySellingValue();

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Product p")
    Long calculateTotalUnitsInStock();

    @Query("SELECT p FROM Product p WHERE p.expiryDate IS NOT NULL AND p.expiryDate <= :targetDate")
    List<Product> findExpiringProducts(@Param("targetDate") LocalDate targetDate);
}
