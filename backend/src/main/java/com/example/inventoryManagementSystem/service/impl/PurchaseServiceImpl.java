package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.PurchaseItemRequest;
import com.example.inventoryManagementSystem.dto.request.PurchaseRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.exception.BusinessException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseResponse> getPendingPurchases() {
        return purchaseRepository.findByStatus(Purchase.PurchaseStatus.PENDING)
                .stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setStatus(Purchase.PurchaseStatus.PENDING);
        purchase.setOrderDate(LocalDateTime.now());

        request.getItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            PurchaseItem item = new PurchaseItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());

            BigDecimal totalPrice = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            item.setTotalPrice(totalPrice);

            purchase.addItem(item);
        });

        BigDecimal total = purchase.getItems().stream()
                .map(PurchaseItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        purchase.setTotalAmount(total);

        Purchase savedPurchase = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(savedPurchase);
    }

    @Override
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        return mapToPurchaseResponse(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse receivePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        if (purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED) {
            throw new BusinessException("Purchase already received");
        }

        if (purchase.getStatus() == Purchase.PurchaseStatus.CANCELLED) {
            throw new BusinessException("Cannot receive cancelled purchase");
        }

        purchase.setStatus(Purchase.PurchaseStatus.RECEIVED);
        purchase.setReceivedDate(LocalDateTime.now());
        updateInventoryStock(purchase);

        Purchase updatedPurchase = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(updatedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse markAsReceived(Long id) {
        return receivePurchase(id);
    }

    @Override
    @Transactional
    public PurchaseResponse cancelPurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new BusinessException("Only pending purchases can be cancelled");
        }

        purchase.setStatus(Purchase.PurchaseStatus.CANCELLED);
        purchase.setCancellationDate(LocalDateTime.now());

        Purchase updatedPurchase = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(updatedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse updatePurchase(Long id, PurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new BusinessException("Only pending purchases can be modified");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        purchase.setSupplier(supplier);
        purchase.setOrderDate(LocalDateTime.now());
        purchase.getItems().clear();

        request.getItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            PurchaseItem item = new PurchaseItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTotalPrice(itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            purchase.addItem(item);
        });

        BigDecimal total = purchase.getItems().stream()
                .map(PurchaseItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        purchase.setTotalAmount(total);

        Purchase updatedPurchase = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(updatedPurchase);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchaseRepository.delete(purchase);
    }

    private void updateInventoryStock(Purchase purchase) {
        for (PurchaseItem item : purchase.getItems()) {
            Product product = item.getProduct();
            int newQuantity = product.getQuantityInStock() + item.getQuantity();
            product.setQuantityInStock(newQuantity);
            productRepository.save(product);
        }
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        if (supplier == null) return null;

        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .website(supplier.getWebsite())
                .rating(supplier.getRating())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .categories(mapSupplierCategories(supplier.getSuppliedCategories()))
                .build();
    }

    private List<SupplierResponse.SupplierCategoryResponse> mapSupplierCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) return null;

        return categories.stream()
                .map(category -> SupplierResponse.SupplierCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private PurchaseResponse mapToPurchaseResponse(Purchase purchase) {
        String purchaseCategory = purchase.getItems().stream()
                .map(item -> item.getProduct().getCategory())
                .filter(Objects::nonNull)
                .findFirst()
                .map(Category::getName)
                .orElse(null);

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplier(mapToSupplierResponse(purchase.getSupplier()))
                .productCategory(purchaseCategory)
                .orderDate(purchase.getOrderDate())
                .receivedDate(purchase.getReceivedDate())
                .cancellationDate(purchase.getCancellationDate())
                .status(purchase.getStatus().name())
                .totalAmount(purchase.getTotalAmount())
                .discountAmount(purchase.getDiscountAmount())
                .finalAmount(purchase.getFinalAmount())
                .items(mapPurchaseItems(purchase.getItems()))
                .build();
    }

    private List<PurchaseItemResponse> mapPurchaseItems(List<PurchaseItem> items) {
        return items.stream().map(item -> {
            Product product = item.getProduct();
            return PurchaseItemResponse.builder()
                    .id(item.getId())
                    .productId(product != null ? product.getId() : null)
                    .productName(product != null ? product.getName() : null)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .product(product != null ? mapToProductResponse(product) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .quantityInStock(product.getQuantityInStock())
                .lowStockThreshold(product.getLowStockThreshold())
                .category(product.getCategory() != null ?
                        mapToCategoryResponse(product.getCategory()) : null)
                .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}