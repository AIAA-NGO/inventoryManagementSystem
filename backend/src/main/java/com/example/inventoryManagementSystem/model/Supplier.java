package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String companyName;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(length = 200)
    private String website;

    @ManyToMany
    @JoinTable(
            name = "supplier_categories",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> suppliedCategories = new ArrayList<>();

    @OneToMany(
            mappedBy = "supplier",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH},
            orphanRemoval = true
    )
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "supplier")
    private List<Purchase> purchases = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addProduct(Product product) {
        products.add(product);
        product.setSupplier(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setSupplier(null);
    }

    private Double rating;

    public List<String> getProductNames() {
        return products.stream()
                .map(Product::getName)
                .toList();
    }
}