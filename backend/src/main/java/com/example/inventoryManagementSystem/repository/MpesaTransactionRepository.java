package com.example.inventoryManagementSystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inventoryManagementSystem.model.MpesaTransactions;

@Repository
public interface MpesaTransactionRepository extends JpaRepository<MpesaTransactions, String> {
    Optional<MpesaTransactions> findByCheckoutRequestId(String checkoutRequestId);
    Optional<MpesaTransactions> findByCheckoutRequestIdAndMerchantRequestId(String checkoutRequestId, String merchantRequestId);
}

