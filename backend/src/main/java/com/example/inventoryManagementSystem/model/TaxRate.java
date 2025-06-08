package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tax_rates")
@Data
public class TaxRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double rate;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;
}