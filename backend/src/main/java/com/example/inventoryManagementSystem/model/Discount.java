package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Data
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private double percentage;

    @Column(nullable = false)
    private boolean Active = true;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String description;
    @Column(name = "discount_code")
    private String discountCode;


}