package com.example.inventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    private LocalDateTime createdAt;
    @Setter
    @Getter
    private LocalDateTime updatedAt;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Setter
    @Getter
    private String sessionId;
    @Getter
    @Setter
    private String shippingAddress;
    @Getter
    @Setter
    private String paymentMethod;
    @Getter
    @Setter
    private BigDecimal subtotal;
    @Getter
    @Setter
    private BigDecimal total;
    @Getter
    @Setter
    private BigDecimal discount;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @Getter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        CART, PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    public enum OrderType {
        ONLINE, POS
    }

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CART;
        this.orderType = OrderType.ONLINE;
    }


    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountAmount;

    public BigDecimal getTotalAmount() {
        return total;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.total = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discount = discountAmount;
    }


}