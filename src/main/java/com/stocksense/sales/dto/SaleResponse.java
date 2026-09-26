package com.stocksense.sales.dto;

import com.stocksense.sales.entity.Sale;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleResponse {

    private Long id;
    private OffsetDateTime saleDate;
    private BigDecimal totalAmount;
    private String createdByUsername;
    private List<SaleItemResponse> items = new ArrayList<>();

    public SaleResponse() {
    }

    public static SaleResponse fromEntity(Sale sale) {
        if (sale == null) {
            return null;
        }
        SaleResponse response = new SaleResponse();
        response.setId(sale.getId());
        response.setSaleDate(sale.getSaleDate());
        response.setTotalAmount(sale.getTotalAmount());
        if (sale.getCreatedBy() != null) {
            response.setCreatedByUsername(sale.getCreatedBy().getUsername());
        }
        if (sale.getItems() != null) {
            response.setItems(sale.getItems().stream()
                    .map(SaleItemResponse::fromEntity)
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

    public OffsetDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(OffsetDateTime saleDate) {
        this.saleDate = saleDate;
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

    public List<SaleItemResponse> getItems() {
        return items;
    }

    public void setItems(List<SaleItemResponse> items) {
        this.items = items;
    }
}
