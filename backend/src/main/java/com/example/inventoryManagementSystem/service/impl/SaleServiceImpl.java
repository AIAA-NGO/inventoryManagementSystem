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
import java.math.RoundingMode;
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
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
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

        BigDecimal subtotal = calculateSubtotal(saleItems);
        BigDecimal totalDiscount = calculateAutomaticDiscounts(saleItems);
        BigDecimal taxableAmount = subtotal.subtract(totalDiscount);
        BigDecimal taxAmount = taxableAmount.multiply(BigDecimal.valueOf(0.16))
                .setScale(2, RoundingMode.HALF_UP);

        sale.setSubtotal(subtotal);
        sale.setTaxAmount(taxAmount);
        sale.setDiscountAmount(totalDiscount);
        sale.setTotal(taxableAmount.add(taxAmount));

        Sale savedSale = saleRepository.save(sale);
        saleItems.forEach(item -> item.setSale(savedSale));
        saleItemRepository.saveAll(saleItems);

        return mapToSaleResponse(saleRepository.findByIdWithItems(savedSale.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found after creation")));
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
        Sale sale = saleRepository.findById(id)
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
        Sale sale = saleRepository.findById(saleId)
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
    public SaleResponse updateSale(Long id, SaleRequest request) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (request.getStatus() != null) {
            try {
                Sale.SaleStatus newStatus = Sale.SaleStatus.valueOf(request.getStatus());
                validateStatusTransition(sale.getStatus(), newStatus);
                sale.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status value: " + request.getStatus());
            }
        }

        if (request.getSubtotal() != null || request.getTaxAmount() != null
                || request.getDiscountAmount() != null || request.getTotal() != null) {
            throw new BusinessException("Financial fields cannot be updated directly");
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
        Sale sale = saleRepository.findById(id)
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
                        .saleTime(s.getSaleDate())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DailySummaryResponse.builder()
                .date(date)
                .totalSales(sales.size())
                .totalRevenue(totalRevenue)
                .sales(summaries)
                .build();
    }

    @Override
    public SaleResponse applyDiscount(Long saleId, ApplyDiscountRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        if (sale.getStatus() != Sale.SaleStatus.COMPLETED) {
            throw new BusinessException("Discount can only be applied to completed sales");
        }

        Discount discount = discountRepository.findByCode(request.getDiscountCode())
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        BigDecimal discountAmount = sale.getSubtotal()
                .multiply(BigDecimal.valueOf(discount.getPercentage() / 100));

        sale.setDiscountAmount(discountAmount);
        sale.setTotal(sale.getSubtotal().add(sale.getTaxAmount()).subtract(discountAmount));
        Sale updatedSale = saleRepository.save(sale);

        return mapToSaleResponse(updatedSale);
    }

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        Cart cart = getOrCreateCart(request.getCartId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            item.setTotalPrice(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(request.getQuantity())));
            item.setCart(cart);
            cart.getItems().add(item);
        }

        recalculateCartTotals(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    public ReceiptResponse generateReceipt(Long saleId) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        List<ReceiptItem> receiptItems = sale.getItems().stream()
                .map(item -> ReceiptItem.builder()
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .discountAmount(item.getDiscountAmount())
                        .build())
                .collect(Collectors.toList());

        return ReceiptResponse.builder()
                .receiptNumber("RCPT-" + sale.getId())
                .date(sale.getSaleDate())
                .customerName(sale.getCustomer().getName())
                .items(receiptItems)
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .total(sale.getTotal())
                .build();
    }

    private void validateStatusTransition(Sale.SaleStatus current, Sale.SaleStatus newStatus) {
        if (current == Sale.SaleStatus.CANCELLED || current == Sale.SaleStatus.REFUNDED) {
            throw new BusinessException("Cannot modify a " + current + " sale");
        }
        if (newStatus == Sale.SaleStatus.COMPLETED && current != Sale.SaleStatus.PENDING) {
            throw new BusinessException("Can only complete pending sales");
        }
    }

    private List<SaleItem> processSaleItems(List<CartItemRequest> itemRequests, Sale sale) {
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
            saleItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            saleItem.setTotalPrice(saleItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
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

    private BigDecimal calculateTaxAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.16));
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
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .status(sale.getStatus().name())
                .build();
    }

    private SaleItemResponse mapToSaleItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .discountAmount(item.getDiscountAmount())
                .build();
    }

    private Cart getOrCreateCart(Long cartId) {
        if (cartId != null) {
            return cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        }
        return cartRepository.save(new Cart());
    }

    private void recalculateCartTotals(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = cart.getItems().stream()
                .map(CartItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxableAmount = subtotal.subtract(totalDiscount);
        BigDecimal taxAmount = calculateTaxAmount(taxableAmount);

        cart.setSubtotal(subtotal);
        cart.setDiscountAmount(totalDiscount);
        cart.setTaxAmount(taxAmount);
        cart.setTotal(subtotal.add(taxAmount).subtract(totalDiscount));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getId())
                .items(cart.getItems().stream()
                        .map(this::mapToCartItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(cart.getSubtotal())
                .discountAmount(cart.getDiscountAmount())
                .taxAmount(cart.getTaxAmount())
                .total(cart.getTotal())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
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
        return discounts.stream()
                .map(discount -> itemPrice.multiply(BigDecimal.valueOf(discount.getPercentage() / 100)))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}