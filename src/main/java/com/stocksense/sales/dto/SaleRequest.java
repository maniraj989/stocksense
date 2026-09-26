package com.stocksense.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleRequest {

    private OffsetDateTime saleDate;

    @NotEmpty(message = "Sale items must not be empty")
    @Valid
    private List<SaleItemRequest> items = new ArrayList<>();

    public SaleRequest() {
    }

    public SaleRequest(List<SaleItemRequest> items) {
        this.items = items;
    }

    public OffsetDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(OffsetDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public List<SaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequest> items) {
        this.items = items;
    }
}
