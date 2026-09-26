package com.stocksense.report.service;

import com.stocksense.product.entity.Product;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.report.dto.InventoryValuationItem;
import com.stocksense.report.dto.InventoryValuationSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryValuationService {

    private final ProductRepository productRepository;

    public InventoryValuationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<InventoryValuationItem> getValuationItems() {
        return productRepository.findAll().stream()
                .map(this::mapToValuationItem)
                .toList();
    }

    public List<InventoryValuationItem> getItemizedValuation() {
        return getValuationItems();
    }

    public InventoryValuationSummary getValuationSummary() {
        BigDecimal totalCost = productRepository.calculateTotalInventoryCostValue();
        BigDecimal totalRevenue = productRepository.calculateTotalInventorySellingValue();
        Long totalUnits = productRepository.calculateTotalUnitsInStock();
        Long productCount = productRepository.count();
        Long lowStockCount = productRepository.countLowStockProducts();
        Long outOfStockCount = productRepository.countOutOfStockProducts();

        return new InventoryValuationSummary(
                totalCost != null ? totalCost.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                totalUnits != null ? totalUnits : 0L,
                productCount,
                lowStockCount != null ? lowStockCount : 0L,
                outOfStockCount != null ? outOfStockCount : 0L
        );
    }

    private InventoryValuationItem mapToValuationItem(Product p) {
        int qty = p.getQuantity() != null ? p.getQuantity() : 0;
        BigDecimal unitCost = p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO;
        BigDecimal sellingPrice = p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO;

        BigDecimal inventoryValue = unitCost.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal potentialRevenue = sellingPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

        String status = "OPTIMAL";
        if (qty == 0) {
            status = "OUT_OF_STOCK";
        } else if (p.getMinimumStock() != null && qty <= p.getMinimumStock()) {
            status = "LOW_STOCK";
        }

        return new InventoryValuationItem(
                p.getId(),
                p.getProductCode(),
                p.getName(),
                p.getCategory() != null ? p.getCategory() : "Uncategorized",
                qty,
                unitCost.setScale(2, RoundingMode.HALF_UP),
                sellingPrice.setScale(2, RoundingMode.HALF_UP),
                inventoryValue,
                potentialRevenue,
                status
        );
    }
}
