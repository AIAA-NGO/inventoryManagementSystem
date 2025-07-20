package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.*;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.exception.*;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final DiscountRepository discountRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(Sale.SaleStatus.COMPLETED);

        List<SaleItem> saleItems = processSaleItems(request.getItems(), sale);
        sale.setItems(saleItems);

        // Calculate subtotal (sum of all item prices without discount)
        BigDecimal subtotal = calculateSubtotal(saleItems);

        // Calculate total discount
        BigDecimal discountAmount = calculateAutomaticDiscounts(saleItems);

        // Calculate total (subtotal - discount)
        BigDecimal total = subtotal.subtract(discountAmount);

        // Calculate profit (sum of (selling price - cost price) for each item)
        BigDecimal profit = calculateSaleProfit(saleItems);

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTotal(total);
        sale.setProfit(profit);

        Sale savedSale = saleRepository.save(sale);
        saleItems.forEach(item -> item.setSale(savedSale));
        saleItemRepository.saveAll(saleItems);

        return mapToSaleResponse(savedSale);
    }

    @Override
    public List<SaleResponse> getAllSales(LocalDate startDate, LocalDate endDate) {
        List<Sale> sales;
        if (startDate != null && endDate != null) {
            sales = saleRepository.findBySaleDateBetween(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59));
        } else {
            sales = saleRepository.findAll();
        }
        return sales.stream().map(this::mapToSaleResponse).collect(Collectors.toList());
    }

    @Override
    public List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateBetween(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59))
                .stream()
                .map(this::mapToSaleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        return mapToSaleResponse(sale);
    }

    @Override
    public List<SaleResponse> getSalesByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return saleRepository.findByCustomer_Id(customerId).stream()
                .map(this::mapToSaleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleResponse> getSalesByStatus(String status) {
        try {
            Sale.SaleStatus saleStatus = Sale.SaleStatus.valueOf(status.toUpperCase());
            return saleRepository.findByStatus(saleStatus).stream()
                    .map(this::mapToSaleResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status value: " + status);
        }
    }

    @Override
    public SaleResponse refundSale(Long saleId) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != Sale.SaleStatus.COMPLETED) {
            throw new BusinessException("Only completed sales can be refunded");
        }

        sale.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
            productRepository.save(product);
        });

        sale.setStatus(Sale.SaleStatus.REFUNDED);
        Sale updatedSale = saleRepository.save(sale);
        return mapToSaleResponse(updatedSale);
    }

    @Override
    public SaleResponse updateSale(Long id, SaleRequest saleRequest) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (saleRequest.getCustomerId() != null) {
            Customer customer = customerRepository.findById(saleRequest.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            sale.setCustomer(customer);
        }

        return mapToSaleResponse(saleRepository.save(sale));
    }

    @Override
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        saleRepository.delete(sale);
    }

    @Override
    public SaleResponse cancelSale(Long id) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        if (sale.getStatus() == Sale.SaleStatus.CANCELLED) {
            throw new BusinessException("Sale is already cancelled");
        }

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
            productRepository.save(product);
        }

        sale.setStatus(Sale.SaleStatus.CANCELLED);
        Sale updatedSale = saleRepository.save(sale);
        return mapToSaleResponse(updatedSale);
    }

    @Override
    public DailySummaryResponse getDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        List<SaleSummary> summaries = sales.stream()
                .map(s -> SaleSummary.builder()
                        .saleId(s.getId())
                        .customerName(s.getCustomer().getName())
                        .totalAmount(s.getTotal())
                        .subtotal(s.getSubtotal())
                        .discountAmount(s.getDiscountAmount())
                        .saleTime(s.getSaleDate())
                        .profit(s.getProfit())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = sales.stream()
                .map(Sale::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = sales.stream()
                .map(Sale::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DailySummaryResponse.builder()
                .date(date)
                .totalSales(sales.size())
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .totalProfit(totalProfit)
                .sales(summaries)
                .build();
    }

    @Override
    public SaleResponse applyDiscount(Long saleId, ApplyDiscountRequest request) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        if (sale.getStatus() != Sale.SaleStatus.COMPLETED) {
            throw new BusinessException("Discount can only be applied to completed sales");
        }

        Discount discount = discountRepository.findByCode(request.getDiscountCode())
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        BigDecimal discountAmount = sale.getSubtotal()
                .multiply(BigDecimal.valueOf(discount.getPercentage() / 100));

        sale.setDiscountAmount(discountAmount);
        sale.setTotal(sale.getSubtotal().subtract(discountAmount));
        Sale updatedSale = saleRepository.save(sale);

        return mapToSaleResponse(updatedSale);
    }

    @Override
    public ReceiptResponse generateReceipt(Long saleId) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        List<ReceiptItem> receiptItems = sale.getItems().stream()
                .map(item -> {
                    BigDecimal itemRevenue = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal itemCost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal itemProfit = itemRevenue.subtract(itemCost);

                    return ReceiptItem.builder()
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .costAmount(itemCost)
                            .profitAmount(itemProfit)
                            .discountAmount(item.getDiscountAmount())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalProfit = receiptItems.stream()
                .map(ReceiptItem::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReceiptResponse.builder()
                .receiptNumber("RCPT-" + sale.getId())
                .date(sale.getSaleDate())
                .customerName(sale.getCustomer().getName())
                .items(receiptItems)
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .totalProfit(totalProfit)
                .build();
    }

    @Override
    public List<SalesTrendResponse> getSalesTrend(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String periodType) {

        String normalizedPeriodType = periodType.toLowerCase();
        if (!Arrays.asList("day", "week", "month").contains(normalizedPeriodType)) {
            throw new BusinessException("Invalid period type. Must be 'day', 'week', or 'month'");
        }

        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(6);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        List<Object[]> results = saleRepository.getSalesTrendNative(
                effectiveStartDate,
                effectiveEndDate,
                normalizedPeriodType);

        return results.stream()
                .map(row -> SalesTrendResponse.builder()
                        .period((String) row[3])
                        .amount((BigDecimal) row[1])
                        .startDate(((Timestamp) row[0]).toLocalDateTime().toLocalDate())
                        .endDate(((Timestamp) row[0]).toLocalDateTime().toLocalDate())
                        .salesCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateSaleProfit(Sale sale) {
        return sale.getItems().stream()
                .map(item -> {
                    BigDecimal revenue = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return revenue.subtract(cost);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSaleProfit(List<SaleItem> items) {
        return items.stream()
                .map(item -> {
                    BigDecimal revenue = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return revenue.subtract(cost);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SaleItem> processSaleItems(List<SaleItemRequest> itemRequests, Sale sale) {
        return itemRequests.stream().map(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantityInStock() < itemRequest.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }

            product.setQuantityInStock(product.getQuantityInStock() - itemRequest.getQuantity());
            productRepository.save(product);

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.getQuantity());
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            saleItem.setUnitPrice(unitPrice);
            BigDecimal quantity = BigDecimal.valueOf(itemRequest.getQuantity());
            saleItem.setTotalPrice(unitPrice.multiply(quantity));

            saleItem.setDiscountAmount(BigDecimal.ZERO);
            saleItem.setSale(sale);

            return saleItem;
        }).collect(Collectors.toList());
    }

    private BigDecimal calculateSubtotal(List<SaleItem> items) {
        return items.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SaleResponse mapToSaleResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomer().getName())
                .items(sale.getItems().stream()
                        .map(this::mapToSaleItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .profit(sale.getProfit())
                .status(sale.getStatus().name())
                .build();
    }

    private SaleItemResponse mapToSaleItemResponse(SaleItem item) {
        BigDecimal revenue = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal profit = revenue.subtract(cost);

        return SaleItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .costAmount(cost)
                .profitAmount(profit)
                .discountAmount(item.getDiscountAmount())
                .build();
    }

    private BigDecimal calculateAutomaticDiscounts(List<SaleItem> saleItems) {
        return saleItems.stream()
                .map(item -> {
                    List<Discount> discounts = findValidDiscountsForProduct(item.getProduct());
                    BigDecimal maxDiscount = calculateMaxDiscountForItem(item.getTotalPrice(), discounts);
                    item.setDiscountAmount(maxDiscount);
                    return maxDiscount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Discount> findValidDiscountsForProduct(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findByApplicableProductsContaining(product).stream()
                .filter(discount -> discount.isActive() &&
                        (discount.getValidFrom() == null || !now.isBefore(discount.getValidFrom())) &&
                        (discount.getValidTo() == null || !now.isAfter(discount.getValidTo())))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateMaxDiscountForItem(BigDecimal itemPrice, List<Discount> discounts) {
        Optional<BigDecimal> maxDiscount = discounts.stream()
                .map(discount -> itemPrice.multiply(BigDecimal.valueOf(discount.getPercentage() / 100)))
                .max(BigDecimal::compareTo);
        return maxDiscount.orElse(BigDecimal.ZERO);
    }
}