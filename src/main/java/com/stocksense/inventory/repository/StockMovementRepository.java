package com.stocksense.inventory.repository;

import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId);

    org.springframework.data.domain.Page<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId, org.springframework.data.domain.Pageable pageable);

    List<StockMovement> findByMovementType(MovementType movementType);

    org.springframework.data.domain.Page<StockMovement> findByMovementTypeOrderByMovementDateDesc(MovementType movementType, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<StockMovement> findAllByOrderByMovementDateDesc(org.springframework.data.domain.Pageable pageable);

    List<StockMovement> findByCreatedById(Long userId);

    List<StockMovement> findByReferenceId(Long referenceId);

    @Query("SELECT sm FROM StockMovement sm WHERE " +
           "(:productId IS NULL OR sm.product.id = :productId) AND " +
           "(:movementType IS NULL OR sm.movementType = :movementType) AND " +
           "(CAST(:startDate AS java.time.OffsetDateTime) IS NULL OR sm.movementDate >= :startDate) AND " +
           "(CAST(:endDate AS java.time.OffsetDateTime) IS NULL OR sm.movementDate <= :endDate) " +
           "ORDER BY sm.movementDate DESC")
    List<StockMovement> findFilteredMovements(@Param("productId") Long productId,
                                             @Param("movementType") MovementType movementType,
                                             @Param("startDate") OffsetDateTime startDate,
                                             @Param("endDate") OffsetDateTime endDate);
}
