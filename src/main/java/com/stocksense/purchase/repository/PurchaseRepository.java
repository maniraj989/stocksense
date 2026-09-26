package com.stocksense.purchase.repository;

import com.stocksense.purchase.entity.Purchase;
import com.stocksense.report.dto.TopSupplierSpendMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsBySupplierId(Long supplierId);

    List<Purchase> findBySupplierId(Long supplierId);

    org.springframework.data.domain.Page<Purchase> findBySupplierId(Long supplierId, org.springframework.data.domain.Pageable pageable);

    List<Purchase> findByCreatedById(Long userId);

    @Query("SELECT p FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate ORDER BY p.purchaseDate DESC")
    List<Purchase> findPurchasesBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                            @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSpendBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                               @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p")
    BigDecimal calculateAllTimeSpend();

    @Query("SELECT COALESCE(AVG(p.totalAmount), 0) FROM Purchase p")
    BigDecimal calculateAveragePurchaseValue();

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate")
    Long countPurchasesBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                    @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new com.stocksense.report.dto.TopSupplierSpendMetric(p.supplier.name, COUNT(p), SUM(p.totalAmount)) " +
           "FROM Purchase p GROUP BY p.supplier.name ORDER BY SUM(p.totalAmount) DESC")
    List<TopSupplierSpendMetric> findTopSuppliersBySpend(Pageable pageable);
}
