package com.stocksense.sales.repository;

import com.stocksense.report.dto.TopSellingProductMetric;
import com.stocksense.sales.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleId(Long saleId);

    List<SaleItem> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    @Query("SELECT new com.stocksense.report.dto.TopSellingProductMetric(si.product.productCode, si.product.name, SUM(si.quantity), SUM(si.subtotal)) " +
           "FROM SaleItem si GROUP BY si.product.productCode, si.product.name ORDER BY SUM(si.quantity) DESC")
    List<TopSellingProductMetric> findTopSellingProducts(Pageable pageable);
}
