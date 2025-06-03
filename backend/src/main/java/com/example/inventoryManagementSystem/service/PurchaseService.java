package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.PurchaseRequest;
import com.example.inventoryManagementSystem.dto.response.PurchaseResponse;
import java.util.List;

public interface PurchaseService {
    List<PurchaseResponse> getAllPurchases();
    PurchaseResponse createPurchase(PurchaseRequest request);
    PurchaseResponse getPurchaseById(Long id);
    PurchaseResponse markAsReceived(Long id);
    void deletePurchase(Long id);
}