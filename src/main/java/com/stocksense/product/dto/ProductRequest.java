package com.stocksense.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductRequest {

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private String description;

    private Long supplierId;

    @NotNull(message = "Purchase price is required")
    @PositiveOrZero(message = "Purchase price must be positive or zero")
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @PositiveOrZero(message = "Selling price must be positive or zero")
    private BigDecimal sellingPrice;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be positive or zero")
    private Integer quantity = 0;

    @NotNull(message = "Minimum stock is required")
    @PositiveOrZero(message = "Minimum stock must be positive or zero")
    private Integer minimumStock = 0;

    @NotNull(message = "Maximum stock is required")
    @PositiveOrZero(message = "Maximum stock must be positive or zero")
    private Integer maximumStock = 0;

    private LocalDate expiryDate;

    public ProductRequest() {
    }

    public ProductRequest(String productCode, String name, String category, String description,
                          Long supplierId, BigDecimal purchasePrice, BigDecimal sellingPrice,
                          Integer quantity, Integer minimumStock, Integer maximumStock, LocalDate expiryDate) {
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.description = description;
        this.supplierId = supplierId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity != null ? quantity : 0;
        this.minimumStock = minimumStock != null ? minimumStock : 0;
        this.maximumStock = maximumStock != null ? maximumStock : 0;
        this.expiryDate = expiryDate;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Integer getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(Integer maximumStock) {
        this.maximumStock = maximumStock;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
