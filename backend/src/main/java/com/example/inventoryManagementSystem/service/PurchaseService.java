package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.PurchaseRequest;
import com.example.inventoryManagementSystem.dto.response.PurchaseResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PurchaseService {
    @Transactional
    PurchaseResponse receivePurchase(Long purchaseId);
    List<PurchaseResponse> getAllPurchases();
    PurchaseResponse createPurchase(PurchaseRequest request);
    PurchaseResponse getPurchaseById(Long id);
    PurchaseResponse markAsReceived(Long id);
    void deletePurchase(Long id);
    PurchaseResponse updatePurchase(Long id, PurchaseRequest request);

    List<PurchaseResponse> getPendingPurchases();



    PurchaseResponse cancelPurchase(Long id);

}