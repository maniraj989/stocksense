package com.stocksense.notification.repository;

import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    org.springframework.data.domain.Page<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status, org.springframework.data.domain.Pageable pageable);

    List<Alert> findByProductId(Long productId);

    org.springframework.data.domain.Page<Alert> findByProductIdOrderByCreatedAtDesc(Long productId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Alert> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);

    List<Alert> findByAlertType(AlertType alertType);

    List<Alert> findBySeverity(AlertSeverity severity);

    long countByStatus(AlertStatus status);
}
