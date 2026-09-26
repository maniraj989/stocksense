package com.stocksense.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private OffsetDateTime purchaseDate;

    @NotEmpty(message = "Purchase items must not be empty")
    @Valid
    private List<PurchaseItemRequest> items = new ArrayList<>();

    public PurchaseRequest() {
    }

    public PurchaseRequest(Long supplierId, List<PurchaseItemRequest> items) {
        this.supplierId = supplierId;
        this.items = items;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public OffsetDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(OffsetDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public List<PurchaseItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemRequest> items) {
        this.items = items;
    }
}
