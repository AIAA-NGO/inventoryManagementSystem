package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    @Column(name = "subtotal", precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total", precision = 19, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal profit = BigDecimal.ZERO;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "applied_discount_code")
    private String appliedDiscountCode;

    @Column(name = "payment_details")
    private String paymentDetails;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SaleItem> items = new ArrayList<>();

    public enum SaleStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        REFUNDED
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.discountAmount = items.stream()
                .map(SaleItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = subtotal.subtract(discountAmount).multiply(new BigDecimal("0.16"));
        this.total = subtotal.subtract(discountAmount).add(taxAmount);

        this.profit = items.stream()
                .map(item -> {
                    BigDecimal revenue = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return revenue.subtract(cost).subtract(item.getDiscountAmount());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}