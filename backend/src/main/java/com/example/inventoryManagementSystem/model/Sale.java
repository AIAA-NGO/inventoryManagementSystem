package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }



    private String saleStatus;


    @Column(precision = 19, scale = 2)
    private BigDecimal discountAmount;

    private String paymentReference;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}