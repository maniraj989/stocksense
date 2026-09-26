package com.stocksense.notification.service;

import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.notification.dto.AlertResponse;
import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.entity.AlertType;
import com.stocksense.notification.repository.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<Alert> findAll() {
        return alertRepository.findAll();
    }

    public Page<AlertResponse> findAll(Pageable pageable) {
        return alertRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AlertResponse::fromEntity);
    }

    public Optional<Alert> findById(Long id) {
        return alertRepository.findById(id);
    }

    public AlertResponse getById(Long id) {
        return alertRepository.findById(id)
                .map(AlertResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
    }

    public List<AlertResponse> findOpenAlerts() {
        return alertRepository.findByStatus(AlertStatus.OPEN).stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    public Page<AlertResponse> findByStatus(AlertStatus status, Pageable pageable) {
        return alertRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(AlertResponse::fromEntity);
    }

    public Page<AlertResponse> findByProductId(Long productId, Pageable pageable) {
        return alertRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(AlertResponse::fromEntity);
    }

    public List<Alert> findByProductId(Long productId) {
        return alertRepository.findByProductId(productId);
    }

    public List<Alert> findByAlertType(AlertType type) {
        return alertRepository.findByAlertType(type);
    }

    public List<Alert> findBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    public long countOpenAlerts() {
        return alertRepository.countByStatus(AlertStatus.OPEN);
    }

    @Transactional
    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    public Optional<Alert> resolveAlert(Long id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setStatus(AlertStatus.RESOLVED);
            return alertRepository.save(alert);
        });
    }

    @Transactional
    public AlertResponse markResolved(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alert.setStatus(AlertStatus.RESOLVED);
        Alert saved = alertRepository.save(alert);
        return AlertResponse.fromEntity(saved);
    }
}
