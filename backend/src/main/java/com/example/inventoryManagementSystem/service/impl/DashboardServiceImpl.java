package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.model.Sale;
import com.example.inventoryManagementSystem.model.Purchase;
import com.example.inventoryManagementSystem.model.Purchase.PurchaseStatus;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CustomerRepository customerRepository;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    @Override
    public DashboardSummaryResponse getBusinessSummary() {
        // Total revenue from completed sales
        BigDecimal totalRevenue = saleRepository.sumTotalByStatus(Sale.SaleStatus.COMPLETED)
                .orElse(BigDecimal.ZERO);

        // Calculate profit (revenue - cost)
        BigDecimal totalProfit = saleItemRepository.findAll().stream()
                .filter(item -> item.getSale().getStatus() == Sale.SaleStatus.COMPLETED)
                .map(item -> {
                    BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return item.getTotalPrice().subtract(cost);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count metrics
        long totalSales = saleRepository.countByStatus(Sale.SaleStatus.COMPLETED);
        long totalProducts = productRepository.count();
        long lowStockItems = productRepository.countByQuantityInStockLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD);
        long pendingPurchases = purchaseRepository.countByStatus(PurchaseStatus.PENDING);

        return DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .totalSales((int) totalSales)
                .totalProducts((int) totalProducts)
                .lowStockItems((int) lowStockItems)
                .pendingPurchases((int) pendingPurchases)
                .build();
    }

    @Override
    public List<SalesTrendResponse> getSalesTrend(String periodType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (periodType.toUpperCase()) {
            case "DAILY" -> endDate.minusDays(30);
            case "WEEKLY" -> endDate.minusWeeks(12);
            case "MONTHLY" -> endDate.minusMonths(12);
            default -> endDate.minusMonths(6);
        };

        return saleRepository.getSalesTrend(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                periodType.toUpperCase()
        );
    }

    @Override
    public List<TopProductResponse> getTopSellingProducts(int limit) {
        return saleItemRepository.findTopSellingProducts(limit).stream()
                .map(result -> {
                    String imageUrl = result[2] != null ? result[2].toString() : null;
                    return TopProductResponse.builder()
                            .productId(((Number) result[0]).longValue())
                            .productName((String) result[1])
                            .productImage(imageUrl)
                            .unitsSold(((Number) result[3]).intValue())
                            .revenue((BigDecimal) result[4])
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LowStockItemResponse> getCriticalLowStockItems() {
        return productRepository.findByQuantityInStockLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD).stream()
                .map(product -> {
                    String imageUrl = product.getImageData() != null ?
                            "/api/products/" + product.getId() + "/image" : null;
                    return LowStockItemResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .productImage(imageUrl)
                            .currentStock(product.getQuantityInStock())
                            .lowStockThreshold(product.getLowStockThreshold() != null ?
                                    product.getLowStockThreshold() : DEFAULT_LOW_STOCK_THRESHOLD)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentSaleResponse> getRecentSales(int limit) {
        return saleRepository.findTop5ByStatusOrderBySaleDateDesc(Sale.SaleStatus.COMPLETED).stream()
                .map(sale -> RecentSaleResponse.builder()
                        .saleId(sale.getId())
                        .customerName(sale.getCustomer().getName())
                        .saleDate(sale.getSaleDate())
                        .amount(sale.getTotal())
                        .status(sale.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpiringItemResponse> getSoonToExpireItems() {
        LocalDate thresholdDate = LocalDate.now().plusDays(30);
        return productRepository.findByExpiryDateBetween(LocalDate.now(), thresholdDate).stream()
                .map(product -> {
                    String imageUrl = product.getImageData() != null ?
                            "/api/products/" + product.getId() + "/image" : null;
                    return ExpiringItemResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .productImage(imageUrl)
                            .expiryDate(product.getExpiryDate())
                            .remainingDays((int) ChronoUnit.DAYS.between(LocalDate.now(), product.getExpiryDate()))
                            .currentStock(product.getQuantityInStock())
                            .build();
                })
                .sorted(Comparator.comparing(ExpiringItemResponse::getRemainingDays))
                .collect(Collectors.toList());
    }
}