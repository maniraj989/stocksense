package com.stocksense.inventory.dto;

import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.entity.StockMovement;

import java.time.OffsetDateTime;

public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private MovementType movementType;
    private Integer quantity;
    private Long referenceId;
    private OffsetDateTime movementDate;
    private String createdByUsername;

    public StockMovementResponse() {
    }

    public static StockMovementResponse fromEntity(StockMovement movement) {
        if (movement == null) {
            return null;
        }
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        if (movement.getProduct() != null) {
            response.setProductId(movement.getProduct().getId());
            response.setProductCode(movement.getProduct().getProductCode());
            response.setProductName(movement.getProduct().getName());
        }
        response.setMovementType(movement.getMovementType());
        response.setQuantity(movement.getQuantity());
        response.setReferenceId(movement.getReferenceId());
        response.setMovementDate(movement.getMovementDate());
        if (movement.getCreatedBy() != null) {
            response.setCreatedByUsername(movement.getCreatedBy().getUsername());
        }
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

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public OffsetDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(OffsetDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }
}
