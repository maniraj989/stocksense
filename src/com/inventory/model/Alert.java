package com.inventory.model;

import java.time.LocalDateTime;

public class Alert {
    private int id;
    private int productId;
    private String alertType;
    private String message;
    private String severity;
    private LocalDateTime createdAt;
    private String status;

    public Alert() {
    }

    public Alert(int id, int productId, String alertType, String message, String severity, LocalDateTime createdAt, String status) {
        this.id = id;
        this.productId = productId;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
