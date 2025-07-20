package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.*;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.exception.*;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.CartService;
import com.example.inventoryManagementSystem.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final DiscountRepository discountRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(Sale.SaleStatus.COMPLETED);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setAppliedDiscountCode(request.getAppliedDiscountCode());

        List<SaleItem> saleItems = processSaleItems(request.getItems(), sale);
        sale.setItems(saleItems);
        sale.calculateTotals();

        Sale savedSale = saleRepository.save(sale);
        saleItems.forEach(item -> item.setSale(savedSale));
        saleItemRepository.saveAll(saleItems);

        return mapToSaleResponse(savedSale);
    }

    @Override
    @Transactional
    public SaleResponse createSaleFromCart(String username, CheckoutRequest request) {
        CartResponse cart = cartService.getCart(username);
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout empty cart");
        }

        Customer customer = request.getCustomerId() != null ?
                customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found")) :
                null;

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(Sale.SaleStatus.COMPLETED);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setAppliedDiscountCode(cart.getAppliedDiscountCode());

        List<SaleItem> saleItems = cart.getItems().stream()
                .map(cartItem -> createSaleItemFromCartItem(cartItem))
                .peek(item -> item.setSale(sale))
                .collect(Collectors.toList());

        sale.setItems(saleItems);
        sale.calculateTotals();

        Sale savedSale = saleRepository.save(sale);
        saleItemRepository.saveAll(saleItems);
        cartService.clearCart(username);

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
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        return mapToSaleResponse(sale);
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
    public SaleResponse refundSale(Long saleId) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

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
    public List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateBetween(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59))
                .stream()
                .map(this::mapToSaleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DailySummaryResponse getDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        List<SaleSummary> summaries = sales.stream()
                .map(s -> SaleSummary.builder()
                        .saleId(s.getId())
                        .customerName(s.getCustomer() != null ? s.getCustomer().getName() : "Guest")
                        .totalAmount(s.getTotal())
                        .subtotal(s.getSubtotal())
                        .discountAmount(s.getDiscountAmount())
                        .taxAmount(s.getTaxAmount())
                        .saleTime(s.getSaleDate())
                        .profit(s.getProfit())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = sales.stream()
                .map(Sale::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = sales.stream()
                .map(Sale::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = sales.stream()
                .map(Sale::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DailySummaryResponse.builder()
                .date(date)
                .totalSales(sales.size())
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .totalProfit(totalProfit)
                .sales(summaries)
                .build();
    }

    @Override
    public List<SalesTrendResponse> getSalesTrend(LocalDateTime startDate, LocalDateTime endDate, String periodType) {
        // Implement sales trend logic here
        return Collections.emptyList();
    }

    @Override
    public ReceiptResponse generateReceipt(Long saleId) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        List<ReceiptItemResponse> items = sale.getItems().stream()
                .map(this::mapToReceiptItemResponse)
                .collect(Collectors.toList());

        return ReceiptResponse.builder()
                .receiptNumber("RCPT-" + sale.getId())
                .date(sale.getSaleDate())
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : "Guest")
                .items(items)
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .total(sale.getTotal())
                .paymentMethod(sale.getPaymentMethod())
                .appliedDiscountCode(sale.getAppliedDiscountCode())
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

        BigDecimal additionalDiscount = sale.getSubtotal()
                .multiply(BigDecimal.valueOf(discount.getPercentage() / 100));

        sale.setDiscountAmount(sale.getDiscountAmount().add(additionalDiscount));
        sale.setTotal(sale.getTotal().subtract(additionalDiscount));
        sale.setProfit(sale.getProfit().subtract(additionalDiscount));
        sale.setAppliedDiscountCode(discount.getCode());

        Sale updatedSale = saleRepository.save(sale);
        return mapToSaleResponse(updatedSale);
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

            return createSaleItem(product, itemRequest.getQuantity(), sale);
        }).collect(Collectors.toList());
    }

    private SaleItem createSaleItem(Product product, int quantity, Sale sale) {
        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

        List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product);
        BigDecimal discountAmount = calculateMaxDiscountForItem(totalPrice, discounts);

        BigDecimal costAmount = BigDecimal.valueOf(product.getCostPrice())
                .multiply(BigDecimal.valueOf(quantity));
        BigDecimal profitAmount = totalPrice.subtract(discountAmount).subtract(costAmount);

        SaleItem saleItem = new SaleItem();
        saleItem.setProduct(product);
        saleItem.setQuantity(quantity);
        saleItem.setUnitPrice(unitPrice);
        saleItem.setDiscountAmount(discountAmount);
        saleItem.setTotalPrice(totalPrice.subtract(discountAmount));
        saleItem.setCostAmount(costAmount);
        saleItem.setProfitAmount(profitAmount);
        saleItem.setSale(sale);

        return saleItem;
    }

    private SaleItem createSaleItemFromCartItem(CartItemResponse cartItem) {
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantityInStock() < cartItem.getQuantity()) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }

        product.setQuantityInStock(product.getQuantityInStock() - cartItem.getQuantity());
        productRepository.save(product);

        BigDecimal unitPrice = cartItem.getUnitPrice();
        BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
        BigDecimal totalPrice = unitPrice.multiply(quantity);
        BigDecimal discountAmount = totalPrice.multiply(
                cartItem.getDiscountPercentage().divide(BigDecimal.valueOf(100)));
        BigDecimal costAmount = BigDecimal.valueOf(product.getCostPrice()).multiply(quantity);
        BigDecimal profitAmount = totalPrice.subtract(discountAmount).subtract(costAmount);

        SaleItem saleItem = new SaleItem();
        saleItem.setProduct(product);
        saleItem.setQuantity(cartItem.getQuantity());
        saleItem.setUnitPrice(unitPrice);
        saleItem.setDiscountAmount(discountAmount);
        saleItem.setTotalPrice(totalPrice.subtract(discountAmount));
        saleItem.setCostAmount(costAmount);
        saleItem.setProfitAmount(profitAmount);

        return saleItem;
    }

    private BigDecimal calculateMaxDiscountForItem(BigDecimal itemPrice, List<Discount> discounts) {
        return discounts.stream()
                .map(discount -> itemPrice.multiply(BigDecimal.valueOf(discount.getPercentage() / 100)))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private SaleResponse mapToSaleResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : "Guest")
                .items(sale.getItems().stream()
                        .map(this::mapToSaleItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .total(sale.getTotal())
                .profit(sale.getProfit())
                .paymentMethod(sale.getPaymentMethod())
                .appliedDiscountCode(sale.getAppliedDiscountCode())
                .status(sale.getStatus().name())
                .build();
    }

    private SaleItemResponse mapToSaleItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private ReceiptItemResponse mapToReceiptItemResponse(SaleItem item) {
        return ReceiptItemResponse.builder()
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}