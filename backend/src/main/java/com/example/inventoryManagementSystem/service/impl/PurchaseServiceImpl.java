package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.PurchaseItemRequest;
import com.example.inventoryManagementSystem.dto.request.PurchaseRequest;
import com.example.inventoryManagementSystem.dto.response.PurchaseItemResponse;
import com.example.inventoryManagementSystem.dto.response.PurchaseResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.PurchaseService;
import com.example.inventoryManagementSystem.model.Supplier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;


    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setStatus("PENDING");
        purchase.setOrderDate(LocalDateTime.now());

        Purchase savedPurchase = purchaseRepository.save(purchase);

        double totalAmount = 0;
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));
            PurchaseItem item = new PurchaseItem();
            item.setPurchase(savedPurchase);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTotalPrice(itemRequest.getQuantity() * itemRequest.getUnitPrice());

            purchaseItemRepository.save(item);
            totalAmount += item.getTotalPrice();
        }

        savedPurchase.setTotalAmount(totalAmount);
        Purchase updatedPurchase = purchaseRepository.save(savedPurchase);

        return mapToPurchaseResponse(updatedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse markAsReceived(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        purchase.setStatus("RECEIVED");
        purchase.setReceivedDate(LocalDateTime.now());
        for (PurchaseItem item : purchase.getItems()) {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
            productRepository.save(product);
        }

        return mapToPurchaseResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        return mapToPurchaseResponse(purchase);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        if ("RECEIVED".equals(purchase.getStatus())) {
            throw new IllegalStateException("Cannot delete received purchase");
        }

        purchaseItemRepository.deleteByPurchaseId(id);
        purchaseRepository.deleteById(id);
    }

    private PurchaseResponse mapToPurchaseResponse(Purchase purchase) {
        PurchaseResponse response = new PurchaseResponse();
        response.setId(purchase.getId());
        response.setSupplierId(purchase.getSupplier().getId());
        response.setSupplierName(purchase.getSupplier().getName());
        response.setOrderDate(purchase.getOrderDate());
        response.setReceivedDate(purchase.getReceivedDate());
        response.setStatus(purchase.getStatus());
        response.setTotalAmount(purchase.getTotalAmount());
        response.setCreatedAt(purchase.getCreatedAt());

        response.setItems(purchase.getItems().stream()
                .map(item -> {
                    PurchaseItemResponse itemResponse = new PurchaseItemResponse();
                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    itemResponse.setTotalPrice(item.getTotalPrice());
                    return itemResponse;
                })
                .collect(Collectors.toList()));

        return response;
    }
}