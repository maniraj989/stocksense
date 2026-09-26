package com.stocksense.sales.repository;

import com.stocksense.sales.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCreatedById(Long userId);

    org.springframework.data.domain.Page<Sale> findByCreatedById(Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findSalesBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                    @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenueBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                                 @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s")
    BigDecimal calculateAllTimeRevenue();

    @Query("SELECT COALESCE(AVG(s.totalAmount), 0) FROM Sale s")
    BigDecimal calculateAverageSaleValue();

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Long countSalesBetweenDates(@Param("startDate") OffsetDateTime startDate,
                                @Param("endDate") OffsetDateTime endDate);
}
