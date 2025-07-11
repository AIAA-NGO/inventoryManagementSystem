package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 19, scale = 2)
    private BigDecimal finalAmount;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Business methods
    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
    }

    public void markAsReceived() {
        this.status = PurchaseStatus.RECEIVED;
        this.receivedDate = LocalDateTime.now();
    }

    public void cancel() {
        this.status = PurchaseStatus.CANCELLED;
        this.cancellationDate = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == PurchaseStatus.PENDING;
    }

    public boolean isReceived() {
        return this.status == PurchaseStatus.RECEIVED;
    }

    public boolean isCancelled() {
        return this.status == PurchaseStatus.CANCELLED;
    }

    public enum PurchaseStatus {
        PENDING,
        RECEIVED,
        CANCELLED
    }


    private LocalDateTime deliveryDate;
    private LocalDateTime expectedDeliveryDate;

    // Add getters
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
}