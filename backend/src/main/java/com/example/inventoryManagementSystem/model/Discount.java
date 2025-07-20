package com.example.inventoryManagementSystem.model;

import com.example.inventoryManagementSystem.dto.response.DiscountResponse;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "discounts")
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private double percentage;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String description;

    @Transient
    @JsonProperty("isActive")
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return (validFrom == null || now.isAfter(validFrom)) &&
                (validTo == null || now.isBefore(validTo));
    }

    @ManyToMany
    @JoinTable(
            name = "discount_products",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonIgnoreProperties("discounts") // Assuming Product has a discounts collection
    private Set<Product> applicableProducts = new HashSet<>();

    // Helper method to convert to DTO
    public DiscountResponse toResponse() {
        DiscountResponse response = new DiscountResponse();
        response.setId(this.id);
        response.setCode(this.code);
        response.setPercentage(this.percentage);
        response.setValidFrom(this.validFrom);
        response.setValidTo(this.validTo);
        response.setDescription(this.description);
        response.setActive(this.isActive());
        response.setProductIds(this.applicableProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toSet()));
        return response;
    }
}