package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.inventoryManagementSystem.service.impl.SaleServiceImpl.TAX_RATE;

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

    @Column(name = "tax_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "pre_tax_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal preTaxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total", precision = 19, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

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

        this.preTaxAmount = subtotal.subtract(discountAmount);
        this.taxAmount = preTaxAmount.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        this.total = preTaxAmount.add(taxAmount);
    }

    public BigDecimal calculateProfit() {
        return items.stream()
                .map(item -> {
                    BigDecimal cost = BigDecimal.valueOf(item.getProduct().getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal revenue = item.getTotalPrice().subtract(itemTax(item.getTotalPrice()));
                    return revenue.subtract(cost);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal itemTax(BigDecimal amount) {
        return amount.subtract(amount.divide(BigDecimal.ONE.add(TAX_RATE), 2, RoundingMode.HALF_UP));
    }

    public BigDecimal getTotalAmount() {
        return this.total;
    }
}