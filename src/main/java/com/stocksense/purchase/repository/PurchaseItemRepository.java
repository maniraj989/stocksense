package com.stocksense.purchase.repository;

import com.stocksense.purchase.entity.PurchaseItem;
import com.stocksense.report.dto.TopPurchasedProductMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findByPurchaseId(Long purchaseId);

    List<PurchaseItem> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    @Query("SELECT new com.stocksense.report.dto.TopPurchasedProductMetric(pi.product.productCode, pi.product.name, SUM(pi.quantity), SUM(pi.subtotal)) " +
           "FROM PurchaseItem pi GROUP BY pi.product.productCode, pi.product.name ORDER BY SUM(pi.quantity) DESC")
    List<TopPurchasedProductMetric> findTopPurchasedProducts(Pageable pageable);
}
