package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final TaxRateRepository taxRateRepository;

    @Override
    public List<SalesReportResponse> generateSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Sale> sales = saleRepository.findBySaleDateBetween(
                        startDateTime != null ? startDateTime : LocalDateTime.MIN,
                        endDateTime != null ? endDateTime : LocalDateTime.now()
                ).stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
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
                                    response.setTotalSales(saleList.stream()
                                            .map(Sale::getTotal)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    response.setTotalTax(saleList.stream()
                                            .map(Sale::getTaxAmount)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    response.setTotalProfit(calculateTotalProfit(saleList));
                                    return response;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(SalesReportResponse::getDate))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalProfit(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal cost = BigDecimal.valueOf(product.getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return item.getTotalPrice() != null ? item.getTotalPrice().subtract(cost) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<ProductPerformanceResponse> generateProductPerformanceReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<SaleItem> saleItems = saleRepository.findBySaleDateBetween(
                        startDateTime != null ? startDateTime : LocalDateTime.MIN,
                        endDateTime != null ? endDateTime : LocalDateTime.now()
                ).stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
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
                                    response.setUnitsSold(items.stream()
                                            .mapToInt(SaleItem::getQuantity)
                                            .sum());
                                    response.setTotalRevenue(items.stream()
                                            .map(SaleItem::getTotalPrice)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    BigDecimal totalCost = BigDecimal.valueOf(product.getCostPrice())
                                            .multiply(BigDecimal.valueOf(response.getUnitsSold()));
                                    BigDecimal profit = response.getTotalRevenue().subtract(totalCost);
                                    if (response.getTotalRevenue().compareTo(BigDecimal.ZERO) != 0) {
                                        response.setProfitMargin(profit.divide(
                                                response.getTotalRevenue(),
                                                4,
                                                RoundingMode.HALF_UP
                                        ));
                                    } else {
                                        response.setProfitMargin(BigDecimal.ZERO);
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
    public List<InventoryValuationResponse> generateInventoryValuationReport() {
        return productRepository.findAll().stream()
                .map(product -> {
                    InventoryValuationResponse response = new InventoryValuationResponse();
                    response.setProductId(product.getId());
                    response.setProductName(product.getName());
                    response.setQuantity(product.getQuantityInStock());
                    response.setUnitCost(BigDecimal.valueOf(product.getCostPrice()));
                    response.setTotalValue(response.getUnitCost()
                            .multiply(BigDecimal.valueOf(response.getQuantity())));
                    return response;
                })
                .sorted(Comparator.comparing(InventoryValuationResponse::getTotalValue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ProfitLossResponse generateProfitLossReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        ProfitLossResponse response = new ProfitLossResponse();
        response.setPeriodStart(startDate != null ? startDate : LocalDate.MIN);
        response.setPeriodEnd(endDate != null ? endDate : LocalDate.now());

        // Calculate revenue from completed sales
        List<Sale> sales = saleRepository.findBySaleDateBetween(
                        startDateTime != null ? startDateTime : LocalDateTime.MIN,
                        endDateTime != null ? endDateTime : LocalDateTime.now()
                ).stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        response.setTotalRevenue(sales.stream()
                .map(Sale::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Calculate cost of goods sold
        List<SaleItem> saleItems = sales.stream()
                .flatMap(sale -> saleItemRepository.findBySale_Id(sale.getId()).stream())
                .collect(Collectors.toList());

        BigDecimal totalCost = saleItems.stream()
                .map(item -> BigDecimal.valueOf(item.getProduct().getCostPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCost(totalCost);

        // Calculate gross profit
        response.setGrossProfit(response.getTotalRevenue().subtract(totalCost));

        // Calculate expenses (placeholder - should include salaries, rent)
        response.setExpenses(BigDecimal.valueOf(0));

        // Calculate net profit
        response.setNetProfit(response.getGrossProfit().subtract(response.getExpenses()));

        return response;
    }

    @Override
    public TaxReportResponse generateTaxReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        TaxReportResponse response = new TaxReportResponse();
        response.setPeriodStart(startDate != null ? startDate : LocalDate.MIN);
        response.setPeriodEnd(endDate != null ? endDate : LocalDate.now());

        // Get default tax rate
        Optional<TaxRate> activeTaxRate = taxRateRepository.findFirstByIsActive(true);
        if (!activeTaxRate.isPresent()) {
            throw new RuntimeException("No active tax rate found");
        }
        TaxRate taxRate = activeTaxRate.get();
        response.setTaxRate(taxRate.getName() + " (" + taxRate.getRate() + "%)");

        // Calculate taxable sales and tax collected
        List<Sale> sales = saleRepository.findBySaleDateBetween(
                        startDateTime != null ? startDateTime : LocalDateTime.MIN,
                        endDateTime != null ? endDateTime : LocalDateTime.now()
                ).stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal taxableSales = sales.stream()
                .map(sale -> sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTaxableSales(taxableSales);

        BigDecimal taxCollected = sales.stream()
                .map(sale -> sale.getTaxAmount() != null ? sale.getTaxAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTaxCollected(taxCollected);

        return response;
    }

    @Override
    public List<SupplierPurchaseResponse> generateSupplierPurchaseReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Purchase> purchases = purchaseRepository.findByOrderDateBetween(
                        startDateTime != null ? startDateTime : LocalDateTime.MIN,
                        endDateTime != null ? endDateTime : LocalDateTime.now()
                ).stream()
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
                                    response.setTotalSpent(supplierPurchases.stream()
                                            .map(Purchase::getTotalAmount)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
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

    private byte[] generateExcelReport(ExportReportRequest request) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            switch (request.getReportType()) {
                case SALES:
                    List<SalesReportResponse> salesData = generateSalesReport(request.getStartDate(), request.getEndDate());
                    createSalesExcelSheet(sheet, headerRow, headerStyle, salesData);
                    break;
                case PRODUCTS:
                    List<ProductPerformanceResponse> productsData = generateProductPerformanceReport(request.getStartDate(), request.getEndDate());
                    createProductsExcelSheet(sheet, headerRow, headerStyle, productsData);
                    break;
                case INVENTORY:
                    List<InventoryValuationResponse> inventoryData = generateInventoryValuationReport();
                    createInventoryExcelSheet(sheet, headerRow, headerStyle, inventoryData);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported report type for Excel export");
            }

            // Auto-size columns
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
        String[] headers = {"Date", "Orders", "Total Sales", "Total Tax", "Total Profit"};
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
            row.createCell(3).setCellValue(item.getTotalTax().doubleValue());
            row.createCell(4).setCellValue(item.getTotalProfit().doubleValue());
        }
    }

    private void createProductsExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<ProductPerformanceResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Units Sold", "Total Revenue", "Profit Margin"};
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
            row.createCell(2).setCellValue(item.getUnitsSold());
            row.createCell(3).setCellValue(item.getTotalRevenue().doubleValue());
            row.createCell(4).setCellValue(item.getProfitMargin().doubleValue());
        }
    }

    private void createInventoryExcelSheet(Sheet sheet, Row headerRow, CellStyle headerStyle, List<InventoryValuationResponse> data) {
        String[] headers = {"Product ID", "Product Name", "Quantity", "Unit Cost", "Total Value"};
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
        }
    }

    private byte[] generatePdfReport(ExportReportRequest request) {
        return new byte[0];
    }

    private byte[] generateCsvReport(ExportReportRequest request) {
        return new byte[0];
    }
}