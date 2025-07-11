package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;


@Entity
@Table(name = "sale_items")
@Data
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;


    private BigDecimal discountAmount = BigDecimal.ZERO;
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

}
