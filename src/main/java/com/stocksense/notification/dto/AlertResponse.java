package com.stocksense.notification.dto;

import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertStatus;
import com.stocksense.notification.entity.AlertType;

import java.time.OffsetDateTime;

public class AlertResponse {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private AlertType alertType;
    private String message;
    private AlertSeverity severity;
    private AlertStatus status;
    private OffsetDateTime createdAt;

    public AlertResponse() {
    }

    public static AlertResponse fromEntity(Alert alert) {
        if (alert == null) {
            return null;
        }
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        if (alert.getProduct() != null) {
            response.setProductId(alert.getProduct().getId());
            response.setProductCode(alert.getProduct().getProductCode());
            response.setProductName(alert.getProduct().getName());
        }
        response.setAlertType(alert.getAlertType());
        response.setMessage(alert.getMessage());
        response.setSeverity(alert.getSeverity());
        response.setStatus(alert.getStatus());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
