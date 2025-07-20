package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public List<SalesReportResponse> generateSalesReport(LocalDate startDate, LocalDate endDate, String statusFilter) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> statusFilter == null || statusFilter.isEmpty() ||
                        sale.getStatus().name().equalsIgnoreCase(statusFilter))
                .collect(Collectors.toList());

        return sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                saleList -> {
                                    SalesReportResponse response = new SalesReportResponse();
                                    response.setDate(saleList.get(0).getSaleDate().toLocalDate());
                                    response.setOrderCount(saleList.size());

                                    BigDecimal subtotal = saleList.stream()
                                            .map(Sale::getSubtotal)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    response.setTotalSales(subtotal);
                                    response.setTotalProfit(calculateTotalProfit(saleList));
                                    response.setGrossProfit(calculateGrossProfit(saleList));
                                    response.setNetProfit(calculateNetProfit(saleList));
                                    return response;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(SalesReportResponse::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public SalesSummaryResponse generateSalesSummaryReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        int totalOrders = sales.size();
        int newCustomers = (int) sales.stream()
                .map(Sale::getCustomer)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = calculateTotalProfit(sales);
        BigDecimal grossProfit = calculateGrossProfit(sales);
        BigDecimal netProfit = calculateNetProfit(sales);

        return SalesSummaryResponse.builder()
                .periodStart(startDate != null ? startDate : LocalDate.MIN)
                .periodEnd(endDate != null ? endDate : LocalDate.now())
                .totalOrders(totalOrders)
                .newCustomers(newCustomers)
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .averageOrderValue(totalOrders > 0 ?
                        totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<ProductPerformanceResponse> generateProductPerformanceReport(LocalDate startDate, LocalDate endDate, Long categoryId) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<SaleItem> saleItems = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .filter(item -> categoryId == null || (item.getProduct() != null && item.getProduct().getCategory() != null &&
                        item.getProduct().getCategory().getId().equals(categoryId)))
                .collect(Collectors.toList());

        return saleItems.stream()
                .collect(Collectors.groupingBy(
                        SaleItem::getProduct,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    Product product = items.get(0).getProduct();
                                    ProductPerformanceResponse response = new ProductPerformanceResponse();
                                    response.setProductId(product.getId());
                                    response.setProductName(product.getName());
                                    response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "Unknown");
                                    response.setUnitsSold(items.stream()
                                            .mapToInt(SaleItem::getQuantity)
                                            .sum());

                                    BigDecimal totalRevenue = items.stream()
                                            .map(SaleItem::getTotalPrice)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    response.setTotalRevenue(totalRevenue);

                                    BigDecimal totalCost = product.getCostPrice() != null ?
                                            BigDecimal.valueOf(product.getCostPrice())
                                                    .multiply(BigDecimal.valueOf(response.getUnitsSold())) :
                                            BigDecimal.ZERO;
                                    BigDecimal profit = totalRevenue.subtract(totalCost);
                                    response.setTotalCost(totalCost);
                                    response.setGrossProfit(profit);

                                    if (totalRevenue.compareTo(BigDecimal.ZERO) != 0) {
                                        response.setProfitMargin(profit.divide(
                                                totalRevenue, 4, RoundingMode.HALF_UP));
                                        response.setMarkupPercentage(profit.divide(
                                                totalCost.compareTo(BigDecimal.ZERO) != 0 ? totalCost : BigDecimal.ONE,
                                                4, RoundingMode.HALF_UP));
                                    } else {
                                        response.setProfitMargin(BigDecimal.ZERO);
                                        response.setMarkupPercentage(BigDecimal.ZERO);
                                    }
                                    return response;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(ProductPerformanceResponse::getTotalRevenue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductSalesTrendResponse> generateProductSalesTrendReport(LocalDate startDate, LocalDate endDate, Long productId) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<SaleItem> saleItems = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .filter(item -> productId == null || (item.getProduct() != null && item.getProduct().getId().equals(productId)))
                .collect(Collectors.toList());

        return saleItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getSale().getSaleDate().toLocalDate(),
                        Collectors.groupingBy(
                                SaleItem::getProduct,
                                Collectors.summingInt(SaleItem::getQuantity)
                        )
                ))
                .entrySet().stream()
                .flatMap(dateEntry -> dateEntry.getValue().entrySet().stream()
                        .map(productEntry -> {
                            Product product = productEntry.getKey();
                            return ProductSalesTrendResponse.builder()
                                    .date(dateEntry.getKey())
                                    .productId(product.getId())
                                    .productName(product.getName())
                                    .unitsSold(productEntry.getValue())
                                    .build();
                        }))
                .sorted(Comparator.comparing(ProductSalesTrendResponse::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryValuationResponse> generateInventoryValuationReport() {
        return productRepository.findAll().stream()
                .map(product -> {
                    InventoryValuationResponse response = new InventoryValuationResponse();
                    response.setProductId(product.getId());
                    response.setProductName(product.getName());
                    response.setQuantity(product.getQuantityInStock());
                    response.setUnitCost(product.getCostPrice() != null ?
                            BigDecimal.valueOf(product.getCostPrice()) : BigDecimal.ZERO);

                    BigDecimal totalValue = response.getUnitCost()
                            .multiply(BigDecimal.valueOf(response.getQuantity()));
                    response.setTotalValue(totalValue);

                    List<SaleItem> saleItems = saleItemRepository.findByProduct(product);
                    if (!saleItems.isEmpty()) {
                        int totalSold = saleItems.stream()
                                .mapToInt(SaleItem::getQuantity)
                                .sum();
                        double turnover = response.getQuantity() > 0 ?
                                (double) totalSold / response.getQuantity() : 0.0;
                        response.setInventoryTurnover(BigDecimal.valueOf(turnover));
                    } else {
                        response.setInventoryTurnover(BigDecimal.ZERO);
                    }

                    return response;
                })
                .sorted(Comparator.comparing(InventoryValuationResponse::getTotalValue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<LowStockReportResponse> generateLowStockReport(int threshold) {
        return productRepository.findAll().stream()
                .filter(product -> product.getQuantityInStock() <= threshold)
                .map(product -> LowStockReportResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .currentStock(product.getQuantityInStock())
                        .reorderLevel(product.getReorderLevel())
                        .build())
                .sorted(Comparator.comparing(LowStockReportResponse::getCurrentStock))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpiringItemsReportResponse> generateExpiringItemsReport(LocalDate cutoffDate) {
        LocalDate effectiveCutoff = cutoffDate != null ? cutoffDate : LocalDate.now().plusMonths(1);
        return productRepository.findByExpiryDateBefore(effectiveCutoff).stream()
                .map(product -> ExpiringItemsReportResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(product.getQuantityInStock())
                        .expiryDate(product.getExpiryDate())
                        .daysUntilExpiry((int) (product.getExpiryDate().toEpochDay() - LocalDate.now().toEpochDay()))
                        .build())
                .sorted(Comparator.comparing(ExpiringItemsReportResponse::getDaysUntilExpiry))
                .collect(Collectors.toList());
    }

    @Override
    public ProfitLossResponse generateProfitLossReport(LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("Generating profit/loss report for startDate: {}, endDate: {}", startDate, endDate);

            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            ProfitLossResponse response = ProfitLossResponse.builder()
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .otherIncome(BigDecimal.ZERO)
                    .otherExpenses(BigDecimal.ZERO)
                    .build();

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                    .stream()
                    .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                    .collect(Collectors.toList());

            BigDecimal totalRevenue = sales.stream()
                    .map(sale -> sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalRevenue(totalRevenue);

            BigDecimal totalCOGS = BigDecimal.ZERO;
            for (Sale sale : sales) {
                List<SaleItem> saleItems = saleItemRepository.findBySale_Id(sale.getId());
                for (SaleItem item : saleItems) {
                    if (item.getProduct() != null && item.getProduct().getCostPrice() != null) {
                        BigDecimal costPrice = BigDecimal.valueOf(item.getProduct().getCostPrice());
                        BigDecimal itemCost = costPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalCOGS = totalCOGS.add(itemCost);
                    }
                }
            }
            response.setTotalCost(totalCOGS);

            BigDecimal grossProfit = totalRevenue.subtract(totalCOGS);
            response.setGrossProfit(grossProfit);

            BigDecimal operatingExpenses = calculateTotalExpenses(startDate, endDate);
            response.setExpenses(operatingExpenses);

            BigDecimal netProfit = grossProfit
                    .add(response.getOtherIncome())
                    .subtract(operatingExpenses)
                    .subtract(response.getOtherExpenses());
            response.setNetProfit(netProfit);

            if (totalRevenue.compareTo(BigDecimal.ZERO) != 0) {
                response.setGrossMarginPercentage(
                        grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP));
                response.setNetProfitPercentage(
                        netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP));
            } else {
                response.setGrossMarginPercentage(BigDecimal.ZERO);
                response.setNetProfitPercentage(BigDecimal.ZERO);
            }

            return response;
        } catch (Exception e) {
            logger.error("Error generating profit/loss report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate profit/loss report: " + e.getMessage(), e);
        }
    }

    @Override
    public CashFlowResponse generateCashFlowReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        CashFlowResponse response = new CashFlowResponse();
        response.setPeriodStart(startDate != null ? startDate : LocalDate.MIN);
        response.setPeriodEnd(endDate != null ? endDate : LocalDate.now());

        BigDecimal salesRevenue = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .map(Sale::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setCashInflows(salesRevenue);

        BigDecimal purchaseOutflows = purchaseRepository.findByOrderDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(purchase -> purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED)
                .map(Purchase::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseOutflows = calculateTotalExpenses(startDate, endDate);
        BigDecimal totalOutflows = purchaseOutflows.add(expenseOutflows);
        response.setCashOutflows(totalOutflows);

        BigDecimal netCashFlow = salesRevenue.subtract(totalOutflows);
        response.setNetCashFlow(netCashFlow);

        return response;
    }

    @Override
    public List<SupplierPurchaseResponse> generateSupplierPurchaseReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Purchase> purchases = purchaseRepository.findByOrderDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(purchase -> purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED)
                .collect(Collectors.toList());

        return purchases.stream()
                .collect(Collectors.groupingBy(
                        Purchase::getSupplier,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                supplierPurchases -> {
                                    Supplier supplier = supplierPurchases.get(0).getSupplier();
                                    SupplierPurchaseResponse response = new SupplierPurchaseResponse();
                                    response.setSupplierId(supplier.getId());
                                    response.setSupplierName(supplier.getCompanyName());
                                    response.setPurchaseCount(supplierPurchases.size());

                                    BigDecimal totalSpent = supplierPurchases.stream()
                                            .map(Purchase::getTotalAmount)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    response.setTotalSpent(totalSpent);

                                    response.setAverageOrderValue(supplierPurchases.size() > 0 ?
                                            totalSpent.divide(BigDecimal.valueOf(supplierPurchases.size()), 2, RoundingMode.HALF_UP) :
                                            BigDecimal.ZERO);

                                    return response;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(SupplierPurchaseResponse::getTotalSpent).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public SupplierPerformanceResponse generateSupplierPerformanceReport(LocalDate startDate, LocalDate endDate, Long supplierId) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        List<Purchase> purchases = purchaseRepository.findBySupplierAndOrderDateBetween(supplier, startDateTime, endDateTime)
                .stream()
                .filter(purchase -> purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED)
                .collect(Collectors.toList());

        int totalOrders = purchases.size();
        BigDecimal totalSpent = purchases.stream()
                .map(Purchase::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = totalOrders > 0 ?
                totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        long onTimeDeliveries = purchases.stream()
                .filter(purchase -> purchase.getDeliveryDate() != null &&
                        !purchase.getDeliveryDate().isAfter(purchase.getExpectedDeliveryDate()))
                .count();
        double onTimeRate = totalOrders > 0 ? (double) onTimeDeliveries / totalOrders : 0;

        return SupplierPerformanceResponse.builder()
                .supplierId(supplier.getId())
                .supplierName(supplier.getCompanyName())
                .periodStart(startDate != null ? startDate : LocalDate.MIN)
                .periodEnd(endDate != null ? endDate : LocalDate.now())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(avgOrderValue)
                .onTimeDeliveryRate(BigDecimal.valueOf(onTimeRate))
                .build();
    }

    @Override
    public BusinessPerformanceResponse generateBusinessPerformanceReport(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.MIN;
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();

        ProfitLossResponse pl = generateProfitLossReport(startDate, endDate);

        List<Product> products = productRepository.findAll();
        BigDecimal totalInventoryValue = products.stream()
                .map(p -> p.getCostPrice() != null ?
                        BigDecimal.valueOf(p.getCostPrice()).multiply(BigDecimal.valueOf(p.getQuantityInStock())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCOGS = pl.getTotalCost();
        BigDecimal inventoryTurnover = totalInventoryValue.compareTo(BigDecimal.ZERO) > 0 ?
                totalCOGS.divide(totalInventoryValue, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return BusinessPerformanceResponse.builder()
                .periodStart(effectiveStartDate)
                .periodEnd(effectiveEndDate)
                .totalRevenue(pl.getTotalRevenue())
                .totalCostOfGoodsSold(pl.getTotalCost())
                .grossProfit(pl.getGrossProfit())
                .operatingExpenses(pl.getExpenses())
                .netProfit(pl.getNetProfit())
                .grossMarginPercentage(pl.getGrossMarginPercentage())
                .netProfitPercentage(pl.getNetProfitPercentage())
                .operatingExpenseRatio(
                        pl.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0 ?
                                pl.getExpenses().divide(pl.getTotalRevenue(), 4, RoundingMode.HALF_UP) :
                                BigDecimal.ZERO
                )
                .inventoryTurnover(inventoryTurnover)
                .build();
    }

    @Override
    public DashboardResponse generateDashboardSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.MIN;
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();

        ProfitLossResponse pl = generateProfitLossReport(startDate, endDate);

        List<Product> products = productRepository.findAll();
        BigDecimal inventoryValue = products.stream()
                .map(p -> p.getCostPrice() != null ?
                        BigDecimal.valueOf(p.getCostPrice()).multiply(BigDecimal.valueOf(p.getQuantityInStock())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int lowStockItemsCount = (int) products.stream()
                .filter(p -> p.getQuantityInStock() < p.getReorderLevel())
                .count();

        List<ProductPerformanceResponse> topProducts = generateProductPerformanceReport(startDate, endDate, null)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .periodStart(effectiveStartDate)
                .periodEnd(effectiveEndDate)
                .totalSalesCount((int) saleRepository.countCompletedSalesBetween(
                        effectiveStartDate.atStartOfDay(), effectiveEndDate.atTime(23, 59, 59)))
                .totalRevenue(pl.getTotalRevenue())
                .totalExpenses(pl.getExpenses())
                .netProfit(pl.getNetProfit())
                .topProducts(topProducts)
                .lowStockItemsCount(lowStockItemsCount)
                .inventoryValue(inventoryValue)
                .build();
    }

    @Override
    public List<SalesTrendResponse> generateSalesTrendReport(LocalDate startDate, LocalDate endDate, String period) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        String periodType = (period == null || period.isEmpty()) ? "DAILY" : period.toUpperCase();

        return sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> getPeriodKey(sale.getSaleDate().toLocalDate(), periodType),
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                sale -> sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate[] dateRange = getDateRangeForPeriod(entry.getKey(), periodType);
                    return SalesTrendResponse.builder()
                            .periodLabel(entry.getKey())
                            .totalSales(entry.getValue())
                            .periodStart(dateRange[0])
                            .periodEnd(dateRange[1])
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getPeriodKey(LocalDate date, String periodType) {
        switch (periodType) {
            case "WEEKLY":
                int weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear());
                return String.format("%d-W%02d", date.getYear(), weekNumber);
            case "MONTHLY":
                return String.format("%d-%02d", date.getYear(), date.getMonthValue());
            case "YEARLY":
                return String.valueOf(date.getYear());
            default:
                return date.toString();
        }
    }

    private LocalDate[] getDateRangeForPeriod(String periodKey, String periodType) {
        switch (periodType) {
            case "WEEKLY":
                String[] weekParts = periodKey.split("-W");
                int year = Integer.parseInt(weekParts[0]);
                int week = Integer.parseInt(weekParts[1]);
                LocalDate start = LocalDate.of(year, 1, 1)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new LocalDate[]{start, start.plusDays(6)};
            case "MONTHLY":
                String[] monthParts = periodKey.split("-");
                year = Integer.parseInt(monthParts[0]);
                int month = Integer.parseInt(monthParts[1]);
                LocalDate firstDay = LocalDate.of(year, month, 1);
                return new LocalDate[]{firstDay, firstDay.with(TemporalAdjusters.lastDayOfMonth())};
            case "YEARLY":
                year = Integer.parseInt(periodKey);
                return new LocalDate[]{
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31)
                };
            default:
                LocalDate date = LocalDate.parse(periodKey);
                return new LocalDate[]{date, date};
        }
    }

    @Override
    public ResponseEntity<Resource> exportReport(ExportReportRequest request) {
        byte[] reportBytes;
        String contentType;
        String filename;

        switch (request.getFormat()) {
            case EXCEL:
                reportBytes = generateExcelReport(request);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = request.getReportType().name().toLowerCase() + "_report.xlsx";
                break;
            case PDF:
                reportBytes = generatePdfReport(request);
                contentType = "application/pdf";
                filename = request.getReportType().name().toLowerCase() + "_report.pdf";
                break;
            case CSV:
                reportBytes = generateCsvReport(request);
                contentType = "text/csv";
                filename = request.getReportType().name().toLowerCase() + "_report.csv";
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(reportBytes));
    }

    @Override
    public ResponseEntity<Resource> exportDashboardAsPdf(LocalDate startDate, LocalDate endDate) {
        DashboardResponse dashboardData = generateDashboardSummary(startDate, endDate);
        byte[] pdfBytes = generateDashboardPdf(dashboardData);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dashboard_report.pdf\"")
                .body(new ByteArrayResource(pdfBytes));
    }

    private byte[] generateExcelReport(ExportReportRequest request) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            switch (request.getReportType()) {
                case SALES:
                    List<SalesReportResponse> salesData = generateSalesReport(request.getStartDate(), request.getEndDate(), null);
                    createSalesExcelSheet(sheet, headerRow, headerStyle, salesData);
                    break;
                case SALES_SUMMARY:
                    SalesSummaryResponse salesSummary = generateSalesSummaryReport(request.getStartDate(), request.getEndDate());
                    createSalesSummaryExcelSheet(sheet, headerRow, headerStyle, salesSummary);
                    break;
                case PRODUCTS:
                    List<ProductPerformanceResponse> productsData = generateProductPerformanceReport(request.getStartDate(), request.getEndDate(), null);
                    createProductsExcelSheet(sheet, headerRow, headerStyle, productsData);
                    break;
                case PRODUCT_SALES_TREND:
                    List<ProductSalesTrendResponse> productTrendData = generateProductSalesTrendReport(request.getStartDate(), request.getEndDate(), null);
                    createProductTrendExcelSheet(sheet, headerRow, headerStyle, productTrendData);
                    break;
                case INVENTORY:
                    List<InventoryValuationResponse> inventoryData = generateInventoryValuationReport();
                    createInventoryExcelSheet(sheet, headerRow, headerStyle, inventoryData);
                    break;
                case LOW_STOCK:
                    List<LowStockReportResponse> lowStockData = generateLowStockReport(10);
                    createLowStockExcelSheet(sheet, headerRow, headerStyle, lowStockData);
                    break;
                case EXPIRING_ITEMS:
                    List<ExpiringItemsReportResponse> expiringData = generateExpiringItemsReport(LocalDate.now().plusMonths(1));
                    createExpiringItemsExcelSheet(sheet, headerRow, headerStyle, expiringData);
                    break;
                case PROFIT_LOSS:
                    ProfitLossResponse profitLossData = generateProfitLossReport(request.getStartDate(), request.getEndDate());
                    createProfitLossExcelSheet(sheet, headerRow, headerStyle, profitLossData);
                    break;
                case CASH_FLOW:
                    CashFlowResponse cashFlowData = generateCashFlowReport(request.getStartDate(), request.getEndDate());
                    createCashFlowExcelSheet(sheet, headerRow, headerStyle, cashFlowData);
                    break;
                case SUPPLIER_PURCHASES:
                    List<SupplierPurchaseResponse> supplierData = generateSupplierPurchaseReport(request.getStartDate(), request.getEndDate());
                    createSupplierExcelSheet(sheet, headerRow, headerStyle, supplierData);
                    break;
                case BUSINESS_PERFORMANCE:
                    BusinessPerformanceResponse performanceData = generateBusinessPerformanceReport(request.getStartDate(), request.getEndDate());
                    createBusinessPerformanceExcelSheet(sheet, headerRow, headerStyle, performanceData);
                    break;
                case SALES_TREND:
                    List<SalesTrendResponse> trendData = generateSalesTrendReport(request.getStartDate(), request.getEndDate(), "MONTHLY");
                    createSalesTrendExcelSheet(sheet, headerRow, headerStyle, trendData);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported report type for Excel export");
            }

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void createSalesExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<SalesReportResponse> data) {
        String[] headers = {"Date", "Orders", "Total Sales", "Gross Profit", "Net Profit"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (SalesReportResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getDate().toString());
            row.createCell(1).setCellValue(item.getOrderCount());
            row.createCell(2).setCellValue(item.getTotalSales().doubleValue());
            row.createCell(3).setCellValue(item.getGrossProfit().doubleValue());
            row.createCell(4).setCellValue(item.getNetProfit().doubleValue());
        }
    }

    private void createSalesSummaryExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, SalesSummaryResponse data) {
        String[] headers = {"Metric", "Value"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        createRow(sheet, rowNum++, "Period Start", data.getPeriodStart().toString());
        createRow(sheet, rowNum++, "Period End", data.getPeriodEnd().toString());
        createRow(sheet, rowNum++, "Total Orders", data.getTotalOrders());
        createRow(sheet, rowNum++, "New Customers", data.getNewCustomers());
        createRow(sheet, rowNum++, "Total Revenue", data.getTotalRevenue());
        createRow(sheet, rowNum++, "Total Profit", data.getTotalProfit());
        createRow(sheet, rowNum++, "Gross Profit", data.getGrossProfit());
        createRow(sheet, rowNum++, "Net Profit", data.getNetProfit());
        createRow(sheet, rowNum++, "Average Order Value", data.getAverageOrderValue());
    }

    private void createProductsExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<ProductPerformanceResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Category", "Units Sold", "Total Revenue", "Total Cost",
                "Gross Profit", "Profit Margin %", "Markup %"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ProductPerformanceResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProductId());
            row.createCell(1).setCellValue(item.getProductName());
            row.createCell(2).setCellValue(item.getCategoryName());
            row.createCell(3).setCellValue(item.getUnitsSold());
            row.createCell(4).setCellValue(item.getTotalRevenue().doubleValue());
            row.createCell(5).setCellValue(item.getTotalCost().doubleValue());
            row.createCell(6).setCellValue(item.getGrossProfit().doubleValue());
            row.createCell(7).setCellValue(item.getProfitMargin().multiply(BigDecimal.valueOf(100)).doubleValue());
            row.createCell(8).setCellValue(item.getMarkupPercentage().multiply(BigDecimal.valueOf(100)).doubleValue());
        }
    }

    private void createProductTrendExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<ProductSalesTrendResponse> data) {
        String[] headers = {"Date", "Product ID", "Product Name", "Units Sold"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ProductSalesTrendResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getDate().toString());
            row.createCell(1).setCellValue(item.getProductId());
            row.createCell(2).setCellValue(item.getProductName());
            row.createCell(3).setCellValue(item.getUnitsSold());
        }
    }

    private void createInventoryExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<InventoryValuationResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Quantity", "Unit Cost", "Total Value", "Turnover Ratio"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (InventoryValuationResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProductId());
            row.createCell(1).setCellValue(item.getProductName());
            row.createCell(2).setCellValue(item.getQuantity());
            row.createCell(3).setCellValue(item.getUnitCost().doubleValue());
            row.createCell(4).setCellValue(item.getTotalValue().doubleValue());
            row.createCell(5).setCellValue(item.getInventoryTurnover().doubleValue());
        }
    }

    private void createLowStockExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<LowStockReportResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Current Stock", "Reorder Level"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (LowStockReportResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProductId());
            row.createCell(1).setCellValue(item.getProductName());
            row.createCell(2).setCellValue(item.getCurrentStock());
            row.createCell(3).setCellValue(item.getReorderLevel());
        }
    }

    private void createExpiringItemsExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<ExpiringItemsReportResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Quantity", "Expiry Date", "Days Until Expiry"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ExpiringItemsReportResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProductId());
            row.createCell(1).setCellValue(item.getProductName());
            row.createCell(2).setCellValue(item.getQuantity());
            row.createCell(3).setCellValue(item.getExpiryDate().toString());
            row.createCell(4).setCellValue(item.getDaysUntilExpiry());
        }
    }

    private void createProfitLossExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, ProfitLossResponse data) {
        String[] headers = {"Metric", "Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        createRow(sheet, rowNum++, "Period Start", data.getPeriodStart().toString());
        createRow(sheet, rowNum++, "Period End", data.getPeriodEnd().toString());
        createRow(sheet, rowNum++, "Total Revenue", data.getTotalRevenue());
        createRow(sheet, rowNum++, "Cost of Goods Sold", data.getTotalCost());
        createRow(sheet, rowNum++, "Gross Profit", data.getGrossProfit());
        createRow(sheet, rowNum++, "Operating Expenses", data.getExpenses());
        createRow(sheet, rowNum++, "Other Income", data.getOtherIncome());
        createRow(sheet, rowNum++, "Other Expenses", data.getOtherExpenses());
        createRow(sheet, rowNum++, "Net Profit", data.getNetProfit());
        createRow(sheet, rowNum++, "Gross Margin %", data.getGrossMarginPercentage().multiply(BigDecimal.valueOf(100)));
        createRow(sheet, rowNum++, "Net Profit %", data.getNetProfitPercentage().multiply(BigDecimal.valueOf(100)));
    }

    private void createCashFlowExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, CashFlowResponse data) {
        String[] headers = {"Metric", "Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        createRow(sheet, rowNum++, "Period Start", data.getPeriodStart().toString());
        createRow(sheet, rowNum++, "Period End", data.getPeriodEnd().toString());
        createRow(sheet, rowNum++, "Cash Inflows", data.getCashInflows());
        createRow(sheet, rowNum++, "Cash Outflows", data.getCashOutflows());
        createRow(sheet, rowNum++, "Net Cash Flow", data.getNetCashFlow());
    }

    private void createSupplierExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<SupplierPurchaseResponse> data) {
        String[] headers = {"Supplier ID", "Supplier Name", "Purchase Count", "Total Spent", "Average Order Value"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (SupplierPurchaseResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getSupplierId());
            row.createCell(1).setCellValue(item.getSupplierName());
            row.createCell(2).setCellValue(item.getPurchaseCount());
            row.createCell(3).setCellValue(item.getTotalSpent().doubleValue());
            row.createCell(4).setCellValue(item.getAverageOrderValue().doubleValue());
        }
    }

    private void createBusinessPerformanceExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, BusinessPerformanceResponse data) {
        String[] headers = {"Metric", "Value"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        createRow(sheet, rowNum++, "Period Start", data.getPeriodStart().toString());
        createRow(sheet, rowNum++, "Period End", data.getPeriodEnd().toString());
        createRow(sheet, rowNum++, "Total Revenue", data.getTotalRevenue());
        createRow(sheet, rowNum++, "Cost of Goods Sold", data.getTotalCostOfGoodsSold());
        createRow(sheet, rowNum++, "Gross Profit", data.getGrossProfit());
        createRow(sheet, rowNum++, "Operating Expenses", data.getOperatingExpenses());
        createRow(sheet, rowNum++, "Net Profit", data.getNetProfit());
        createRow(sheet, rowNum++, "Gross Margin %", data.getGrossMarginPercentage().multiply(BigDecimal.valueOf(100)));
        createRow(sheet, rowNum++, "Net Profit %", data.getNetProfitPercentage().multiply(BigDecimal.valueOf(100)));
        createRow(sheet, rowNum++, "Operating Expense Ratio", data.getOperatingExpenseRatio().multiply(BigDecimal.valueOf(100)));
        createRow(sheet, rowNum++, "Inventory Turnover", data.getInventoryTurnover());
    }

    private void createSalesTrendExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<SalesTrendResponse> data) {
        String[] headers = {"Period Start", "Period End", "Total Sales"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (SalesTrendResponse item : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getPeriodStart().toString());
            row.createCell(1).setCellValue(item.getPeriodEnd().toString());
            row.createCell(2).setCellValue(item.getTotalSales().doubleValue());
        }
    }

    private void createRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void createRow(Sheet sheet, int rowNum, String label, int value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void createRow(Sheet sheet, int rowNum, String label, BigDecimal value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value.doubleValue());
    }

    private byte[] generatePdfReport(ExportReportRequest request) {
        // TODO: Implement PDF generation logic
        return new byte[0];
    }

    private byte[] generateDashboardPdf(DashboardResponse data) {
        // TODO: Implement Dashboard PDF generation logic
        return new byte[0];
    }

    private byte[] generateCsvReport(ExportReportRequest request) {
        // TODO: Implement CSV generation logic
        return new byte[0];
    }

    private BigDecimal calculateTotalProfit(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal cost = product != null && product.getCostPrice() != null ?
                            BigDecimal.valueOf(product.getCostPrice()).multiply(BigDecimal.valueOf(item.getQuantity())) :
                            BigDecimal.ZERO;
                    return item.getTotalPrice() != null ? item.getTotalPrice().subtract(cost) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateGrossProfit(List<Sale> sales) {
        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCOGS = sales.stream()
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .map(item -> {
                    Product product = item.getProduct();
                    return product != null && product.getCostPrice() != null ?
                            BigDecimal.valueOf(product.getCostPrice()).multiply(BigDecimal.valueOf(item.getQuantity())) :
                            BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRevenue.subtract(totalCOGS);
    }

    private BigDecimal calculateNetProfit(List<Sale> sales) {
        if (sales.isEmpty()) {
            logger.warn("No sales provided for net profit calculation");
            return BigDecimal.ZERO;
        }
        BigDecimal grossProfit = calculateGrossProfit(sales);
        BigDecimal totalExpenses = calculateTotalExpenses(
                sales.get(0).getSaleDate().toLocalDate(),
                sales.get(sales.size() - 1).getSaleDate().toLocalDate()
        );
        return grossProfit.subtract(totalExpenses);
    }

    private BigDecimal calculateTotalExpenses(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.MIN;
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        BigDecimal totalExpenses = expenseRepository.findByDateBetween(effectiveStartDate, effectiveEndDate)
                .stream()
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.debug("Total Expenses from {} to {}: {}", effectiveStartDate, effectiveEndDate, totalExpenses);
        return totalExpenses;
    }
}