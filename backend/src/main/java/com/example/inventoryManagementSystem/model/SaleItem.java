package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "cost_amount", precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "profit_amount", precision = 19, scale = 2)
    private BigDecimal profitAmount;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;
}