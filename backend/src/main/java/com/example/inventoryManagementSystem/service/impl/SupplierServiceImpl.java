package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.SupplierRequest;
import com.example.inventoryManagementSystem.dto.response.SupplierResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Purchase;
import com.example.inventoryManagementSystem.model.Supplier;
import com.example.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventoryManagementSystem.repository.PurchaseRepository;
import com.example.inventoryManagementSystem.repository.SupplierRepository;
import com.example.inventoryManagementSystem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        Supplier savedSupplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToSupplierResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(updatedSupplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));


        List<Product> products = productRepository.findBySupplier(supplier);
        if (!products.isEmpty()) {
            throw new IllegalStateException("Cannot delete supplier with associated products");
        }


        List<Purchase> purchases = purchaseRepository.findBySupplier(supplier);
        if (!purchases.isEmpty()) {
            throw new IllegalStateException("Cannot delete supplier with purchase history");
        }

        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
        return productRepository.findBySupplier(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Purchase> getPurchasesBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
        return purchaseRepository.findBySupplier(supplier);
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setContactPerson(supplier.getContactPerson());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setAddress(supplier.getAddress());
        response.setCreatedAt(supplier.getCreatedAt());

        response.setProductCount(productRepository.countBySupplier(supplier));
        response.setPurchaseCount(purchaseRepository.countBySupplier(supplier));

        return response;
    }
}