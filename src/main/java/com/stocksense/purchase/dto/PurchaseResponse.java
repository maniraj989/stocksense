package com.stocksense.purchase.dto;

import com.stocksense.purchase.entity.Purchase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseResponse {

    private Long id;
    private Long supplierId;
    private String supplierName;
    private OffsetDateTime purchaseDate;
    private BigDecimal totalAmount;
    private String createdByUsername;
    private List<PurchaseItemResponse> items = new ArrayList<>();

    public PurchaseResponse() {
    }

    public static PurchaseResponse fromEntity(Purchase purchase) {
        if (purchase == null) {
            return null;
        }
        PurchaseResponse response = new PurchaseResponse();
        response.setId(purchase.getId());
        if (purchase.getSupplier() != null) {
            response.setSupplierId(purchase.getSupplier().getId());
            response.setSupplierName(purchase.getSupplier().getName());
        }
        response.setPurchaseDate(purchase.getPurchaseDate());
        response.setTotalAmount(purchase.getTotalAmount());
        if (purchase.getCreatedBy() != null) {
            response.setCreatedByUsername(purchase.getCreatedBy().getUsername());
        }
        if (purchase.getItems() != null) {
            response.setItems(purchase.getItems().stream()
                    .map(PurchaseItemResponse::fromEntity)
                    .toList());
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public OffsetDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(OffsetDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public List<PurchaseItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemResponse> items) {
        this.items = items;
    }
}
