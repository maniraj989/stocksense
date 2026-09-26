package com.stocksense.inventory.dto;

import com.stocksense.inventory.entity.MovementType;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    private MovementType movementType = MovementType.ADJUSTMENT;

    private Long referenceId;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Long productId, Integer quantity, MovementType movementType, Long referenceId) {
        this.productId = productId;
        this.quantity = quantity;
        this.movementType = movementType;
        this.referenceId = referenceId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}
