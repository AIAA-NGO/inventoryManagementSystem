package com.example.inventoryManagementSystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Table(name = "mpesa_transactions")
@Data
public class MpesaTransactions {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String id;

    @Column(name = "merchant_request_id", unique = true)
    private String merchantRequestId;

    @Column(name = "checkout_request_id", unique = true)
    private String checkoutRequestId;
    @Column(name = "mpesa_receipt_number") // This will be paymentId from callback
    private String mpesaReceiptNumber;
    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "transaction_amount")
    private Double transactionAmount;

    @Column(name = "status")
    private String status;
    @Column(name = "initial_amount")
    private Double initialAmount;

    @Column(name = "initial_phone_number")
    private String initialPhoneNumber;

    @Column(name = "stk_response_code")
    private String stkResponseCode; // To store "0" or other codes from STK Push response

    @Column(name = "stk_response_description")
    private String stkResponseDescription;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

