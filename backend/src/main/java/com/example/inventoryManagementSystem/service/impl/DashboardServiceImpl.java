package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.exception.BusinessException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int DEFAULT_EXPIRY_WARNING_DAYS = 30;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        try {
            BigDecimal totalSalesAmount = saleRepository.getCompletedSalesSubtotal() != null ?
                    saleRepository.getCompletedSalesSubtotal() : BigDecimal.ZERO;
            BigDecimal totalDiscounts = saleRepository.getCompletedSalesDiscounts() != null ?
                    saleRepository.getCompletedSalesDiscounts() : BigDecimal.ZERO;
            BigDecimal totalRevenue = saleRepository.getCompletedSalesRevenue() != null ?
                    saleRepository.getCompletedSalesRevenue() : BigDecimal.ZERO;
            BigDecimal totalProfit = saleRepository.getCompletedSalesProfit() != null ?
                    saleRepository.getCompletedSalesProfit() : BigDecimal.ZERO;

            long totalSalesCount = saleRepository.countCompletedSales();
            long totalInventoryItems = productRepository.count();
            long totalCustomers = customerRepository.count();
            long expiredItemsCount = productRepository.countByExpiryDateBefore(LocalDate.now());
            long lowStockItemsCount = productRepository.countByQuantityInStockLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD);

            return DashboardSummaryResponse.builder()
                    .totalSalesAmount(totalSalesAmount)
                    .totalDiscounts(totalDiscounts)
                    .totalRevenue(totalRevenue)
                    .totalProfit(totalProfit)
                    .totalSalesCount(totalSalesCount)
                    .totalInventoryItems(totalInventoryItems)
                    .totalCustomers(totalCustomers)
                    .expiredItemsCount(expiredItemsCount)
                    .lowStockItemsCount(lowStockItemsCount)
                    .build();
        } catch (Exception e) {
            throw new BusinessException("Error generating dashboard summary: " + e.getMessage());
        }
    }

    @Override
    public List<SalesTrendResponse> getSalesTrend(String periodType) {
        try {
            // Validate period type
            String normalizedPeriodType = periodType.toLowerCase();
            if (!Arrays.asList("day", "week", "month").contains(normalizedPeriodType)) {
                normalizedPeriodType = "month";
            }

            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = determineStartDate(normalizedPeriodType, endDate);

            List<Object[]> results = saleRepository.getSalesTrendNative(
                    startDate,
                    endDate,
                    normalizedPeriodType);

            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }

            return results.stream()
                    .map(row -> {
                        try {
                            return SalesTrendResponse.builder()
                                    .period((String) row[5])
                                    .amount((BigDecimal) row[1])
                                    .startDate(((Timestamp) row[2]).toLocalDateTime().toLocalDate())
                                    .endDate(((Timestamp) row[3]).toLocalDateTime().toLocalDate())
                                    .salesCount(((Number) row[4]).longValue())
                                    .build();
                        } catch (Exception e) {
                            throw new BusinessException("Error mapping sales trend data: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException("Failed to fetch sales trend: " + e.getMessage());
        }
    }







    private LocalDateTime determineStartDate(String periodType, LocalDateTime endDate) {
        switch (periodType) {
            case "day":
                return endDate.minusDays(30);
            case "week":
                return endDate.minusWeeks(12);
            case "month":
                return endDate.minusMonths(12);
            default:
                return endDate.minusMonths(6);
        }
    }

    private List<SalesTrendResponse> fillMissingPeriods(List<SalesTrendResponse> trends,
                                                        LocalDate start,
                                                        LocalDate end,
                                                        String periodType) {
        if ("MONTHLY".equalsIgnoreCase(periodType)) {
            return fillMissingMonths(trends, start, end);
        } else if ("WEEKLY".equalsIgnoreCase(periodType)) {
            return fillMissingWeeks(trends, start, end);
        } else if ("DAILY".equalsIgnoreCase(periodType)) {
            return fillMissingDays(trends, start, end);
        }
        return trends;
    }

    private List<SalesTrendResponse> fillMissingMonths(List<SalesTrendResponse> trends,
                                                       LocalDate start,
                                                       LocalDate end) {
        YearMonth startMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        Map<YearMonth, SalesTrendResponse> trendMap = trends.stream()
                .collect(Collectors.toMap(
                        trend -> YearMonth.from(trend.getPeriodStart()),
                        trend -> trend
                ));

        List<SalesTrendResponse> filledTrends = new ArrayList<>();

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            LocalDate monthStart = month.atDay(1);
            LocalDate monthEnd = month.atEndOfMonth();

            SalesTrendResponse existing = trendMap.get(month);
            if (existing != null) {
                filledTrends.add(existing);
            } else {
                filledTrends.add(SalesTrendResponse.builder()
                        .periodLabel(month.toString())
                        .totalSales(BigDecimal.ZERO)
                        .periodStart(monthStart)
                        .periodEnd(monthEnd)
                        .saleCount(0)
                        .build());
            }
        }

        return filledTrends;
    }

    private List<SalesTrendResponse> fillMissingWeeks(List<SalesTrendResponse> trends,
                                                      LocalDate start,
                                                      LocalDate end) {
        Map<LocalDate, SalesTrendResponse> trendMap = trends.stream()
                .collect(Collectors.toMap(
                        SalesTrendResponse::getPeriodStart,
                        trend -> trend
                ));

        List<SalesTrendResponse> filledTrends = new ArrayList<>();
        LocalDate currentWeekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endWeekStart = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        while (!currentWeekStart.isAfter(endWeekStart)) {
            LocalDate weekStart = currentWeekStart;
            LocalDate weekEnd = currentWeekStart.plusDays(6);

            SalesTrendResponse existing = trendMap.get(currentWeekStart);
            if (existing != null) {
                filledTrends.add(existing);
            } else {
                filledTrends.add(SalesTrendResponse.builder()
                        .periodLabel("Week " + currentWeekStart.get(WeekFields.ISO.weekOfWeekBasedYear()))
                        .totalSales(BigDecimal.ZERO)
                        .periodStart(weekStart)
                        .periodEnd(weekEnd)
                        .saleCount(0)
                        .build());
            }
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }

        return filledTrends;
    }

    private List<SalesTrendResponse> fillMissingDays(List<SalesTrendResponse> trends,
                                                     LocalDate start,
                                                     LocalDate end) {
        Map<LocalDate, SalesTrendResponse> trendMap = trends.stream()
                .collect(Collectors.toMap(
                        SalesTrendResponse::getPeriodStart,
                        trend -> trend
                ));

        List<SalesTrendResponse> filledTrends = new ArrayList<>();
        LocalDate currentDate = start;

        while (!currentDate.isAfter(end)) {
            SalesTrendResponse existing = trendMap.get(currentDate);
            if (existing != null) {
                filledTrends.add(existing);
            } else {
                filledTrends.add(SalesTrendResponse.builder()
                        .periodLabel(currentDate.toString())
                        .totalSales(BigDecimal.ZERO)
                        .periodStart(currentDate)
                        .periodEnd(currentDate)
                        .saleCount(0)
                        .build());
            }
            currentDate = currentDate.plusDays(1);
        }

        return filledTrends;
    }

    @Override
    public List<TopProductResponse> getTopSellingProducts(int limit) {
        try {
            List<Object[]> results = saleItemRepository.findTopSellingProducts(limit);

            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }

            return results.stream()
                    .map(result -> {
                        try {
                            return TopProductResponse.builder()
                                    .productId(((Number) result[0]).longValue())
                                    .productName((String) result[1])
                                    .productImage(result[2] != null ? result[2].toString() : null)
                                    .unitsSold(((Number) result[3]).intValue())
                                    .revenue(result[4] != null ? new BigDecimal(result[4].toString()) : BigDecimal.ZERO)
                                    .build();
                        } catch (Exception e) {
                            throw new BusinessException("Error mapping top selling products: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException("Failed to fetch top selling products: " + e.getMessage());
        }
    }

    @Override
    public List<LowStockItemResponse> getCriticalLowStockItems() {
        return productRepository.findByQuantityInStockLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD).stream()
                .map(product -> LowStockItemResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .productImage(product.getImageUrl()) // Changed from getImageData() to getImageUrl()
                        .currentStock(product.getQuantityInStock())
                        .lowStockThreshold(product.getLowStockThreshold() != null ?
                                product.getLowStockThreshold() : DEFAULT_LOW_STOCK_THRESHOLD)
                        .build())
                .sorted(Comparator.comparing(LowStockItemResponse::getCurrentStock))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentSaleResponse> getRecentSales(int limit) {
        return saleRepository.findTop5ByStatusOrderBySaleDateDesc(Sale.SaleStatus.COMPLETED).stream()
                .limit(limit)
                .map(sale -> RecentSaleResponse.builder()
                        .saleId(sale.getId())
                        .customerName(sale.getCustomer() != null ?
                                sale.getCustomer().getName() : "Walk-in Customer")
                        .saleDate(sale.getSaleDate())
                        .amount(sale.getTotal())
                        .status(sale.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpiringItemResponse> getSoonToExpireItems() {
        LocalDate thresholdDate = LocalDate.now().plusDays(DEFAULT_EXPIRY_WARNING_DAYS);
        return productRepository.findByExpiryDateBetween(LocalDate.now(), thresholdDate).stream()
                .map(product -> ExpiringItemResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .productImage(product.getImageUrl()) // Changed from getImageData() to getImageUrl()
                        .expiryDate(product.getExpiryDate())
                        .remainingDays((int) ChronoUnit.DAYS.between(LocalDate.now(), product.getExpiryDate()))
                        .currentStock(product.getQuantityInStock())
                        .build())
                .sorted(Comparator.comparing(ExpiringItemResponse::getRemainingDays))
                .collect(Collectors.toList());
    }
}