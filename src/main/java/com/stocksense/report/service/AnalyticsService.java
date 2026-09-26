package com.stocksense.report.service;

import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.repository.PurchaseItemRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import com.stocksense.report.dto.DashboardAnalyticsDto;
import com.stocksense.report.dto.PurchaseAnalyticsSummary;
import com.stocksense.report.dto.SalesAnalyticsSummary;
import com.stocksense.report.dto.TopPurchasedProductMetric;
import com.stocksense.report.dto.TopSellingProductMetric;
import com.stocksense.report.dto.TopSupplierSpendMetric;
import com.stocksense.sales.repository.SaleItemRepository;
import com.stocksense.sales.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public AnalyticsService(SaleRepository saleRepository,
                            SaleItemRepository saleItemRepository,
                            PurchaseRepository purchaseRepository,
                            PurchaseItemRepository purchaseItemRepository,
                            ProductRepository productRepository,
                            SupplierRepository supplierRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    public SalesAnalyticsSummary getSalesAnalytics() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfToday = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        
        YearMonth currentYearMonth = YearMonth.now();
        OffsetDateTime startOfThisMonth = currentYearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        
        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);
        OffsetDateTime startOfPrevMonth = prevYearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfPrevMonth = prevYearMonth.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        BigDecimal totalRevenue = saleRepository.calculateAllTimeRevenue();
        Long totalSalesCount = saleRepository.count();
        BigDecimal averageSale = saleRepository.calculateAverageSaleValue();

        BigDecimal todaySales = saleRepository.calculateTotalRevenueBetweenDates(startOfToday, now);
        BigDecimal thisMonthSales = saleRepository.calculateTotalRevenueBetweenDates(startOfThisMonth, now);
        BigDecimal prevMonthSales = saleRepository.calculateTotalRevenueBetweenDates(startOfPrevMonth, endOfPrevMonth);

        List<TopSellingProductMetric> topSelling = saleItemRepository.findTopSellingProducts(PageRequest.of(0, 5));

        return new SalesAnalyticsSummary(
                scaleMoney(totalRevenue),
                totalSalesCount != null ? totalSalesCount : 0L,
                scaleMoney(averageSale),
                scaleMoney(todaySales),
                scaleMoney(thisMonthSales),
                scaleMoney(prevMonthSales),
                topSelling
        );
    }

    public PurchaseAnalyticsSummary getPurchaseAnalytics() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfToday = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);

        YearMonth currentYearMonth = YearMonth.now();
        OffsetDateTime startOfThisMonth = currentYearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);
        OffsetDateTime startOfPrevMonth = prevYearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfPrevMonth = prevYearMonth.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        BigDecimal totalSpend = purchaseRepository.calculateAllTimeSpend();
        Long totalCount = purchaseRepository.count();
        BigDecimal averagePO = purchaseRepository.calculateAveragePurchaseValue();

        BigDecimal todayPurchases = purchaseRepository.calculateTotalSpendBetweenDates(startOfToday, now);
        BigDecimal thisMonthPurchases = purchaseRepository.calculateTotalSpendBetweenDates(startOfThisMonth, now);
        BigDecimal prevMonthPurchases = purchaseRepository.calculateTotalSpendBetweenDates(startOfPrevMonth, endOfPrevMonth);

        List<TopSupplierSpendMetric> topSuppliers = purchaseRepository.findTopSuppliersBySpend(PageRequest.of(0, 5));
        List<TopPurchasedProductMetric> topPurchased = purchaseItemRepository.findTopPurchasedProducts(PageRequest.of(0, 5));

        return new PurchaseAnalyticsSummary(
                scaleMoney(totalSpend),
                totalCount != null ? totalCount : 0L,
                scaleMoney(averagePO),
                scaleMoney(todayPurchases),
                scaleMoney(thisMonthPurchases),
                scaleMoney(prevMonthPurchases),
                topSuppliers,
                topPurchased
        );
    }

    public DashboardAnalyticsDto getDashboardAnalytics() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfToday = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        YearMonth currentYearMonth = YearMonth.now();
        OffsetDateTime startOfThisMonth = currentYearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        BigDecimal inventoryValue = productRepository.calculateTotalInventoryCostValue();
        BigDecimal todaySales = saleRepository.calculateTotalRevenueBetweenDates(startOfToday, now);
        BigDecimal todayPurchases = purchaseRepository.calculateTotalSpendBetweenDates(startOfToday, now);
        Long lowStockCount = productRepository.countLowStockProducts();
        Long totalProducts = productRepository.count();
        Long totalSuppliers = supplierRepository.count();
        BigDecimal salesThisMonth = saleRepository.calculateTotalRevenueBetweenDates(startOfThisMonth, now);
        BigDecimal purchasesThisMonth = purchaseRepository.calculateTotalSpendBetweenDates(startOfThisMonth, now);

        return new DashboardAnalyticsDto(
                scaleMoney(inventoryValue),
                scaleMoney(todaySales),
                scaleMoney(todayPurchases),
                lowStockCount != null ? lowStockCount : 0L,
                totalProducts,
                totalSuppliers,
                scaleMoney(salesThisMonth),
                scaleMoney(purchasesThisMonth)
        );
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
