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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double rate;

    private String description;
    private boolean isActive = true;
}
