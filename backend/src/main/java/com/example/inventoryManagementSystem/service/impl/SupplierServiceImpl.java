package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.SupplierRequest;
import com.example.inventoryManagementSystem.dto.response.SupplierResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setCompanyName(request.getCompanyName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setWebsite(request.getWebsite());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            supplier.setSuppliedCategories(categories);
        }

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

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (request.getCompanyName() != null) {
            supplier.setCompanyName(request.getCompanyName());
        }
        if (request.getContactPerson() != null) {
            supplier.setContactPerson(request.getContactPerson());
        }
        if (request.getEmail() != null) {
            supplier.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            supplier.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }
        if (request.getWebsite() != null) {
            supplier.setWebsite(request.getWebsite());
        }

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            supplier.getSuppliedCategories().clear();
            supplier.getSuppliedCategories().addAll(categories);
        }

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

        supplier.getSuppliedCategories().clear();
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

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String companyName, String contactPerson, String category) {
        if (category != null && !category.isEmpty()) {
            return supplierRepository.findBySuppliedCategoriesNameContainingIgnoreCase(category).stream()
                    .filter(supplier -> matchesSearchCriteria(supplier, companyName, contactPerson))
                    .map(this::mapToSupplierResponse)
                    .collect(Collectors.toList());
        } else {
            return supplierRepository.findAll().stream()
                    .filter(supplier -> matchesSearchCriteria(supplier, companyName, contactPerson))
                    .map(this::mapToSupplierResponse)
                    .collect(Collectors.toList());
        }
    }

    private boolean matchesSearchCriteria(Supplier supplier, String companyName, String contactPerson) {
        boolean matches = true;
        if (companyName != null && !companyName.isEmpty()) {
            matches = supplier.getCompanyName().toLowerCase().contains(companyName.toLowerCase());
        }
        if (matches && contactPerson != null && !contactPerson.isEmpty()) {
            matches = supplier.getContactPerson().toLowerCase().contains(contactPerson.toLowerCase());
        }
        return matches;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
        return supplier.getSuppliedCategories();
    }

    @Override
    @Transactional
    public void addCategoryToSupplier(Long supplierId, Long categoryId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!supplier.getSuppliedCategories().contains(category)) {
            supplier.getSuppliedCategories().add(category);
            supplierRepository.save(supplier);
        }
    }

    @Override
    @Transactional
    public void removeCategoryFromSupplier(Long supplierId, Long categoryId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        supplier.getSuppliedCategories().remove(category);
        supplierRepository.save(supplier);
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        SupplierResponse response = SupplierResponse.builder()
                .id(supplier.getId())
                .build();
        response.setId(supplier.getId());
        response.setCompanyName(supplier.getCompanyName());
        response.setContactPerson(supplier.getContactPerson());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setAddress(supplier.getAddress());
        response.setWebsite(supplier.getWebsite());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());
        Double totalPurchasesAmount = purchaseRepository.findBySupplier(supplier).stream()
                .map(Purchase::getTotalAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        response.setTotalPurchasesAmount(totalPurchasesAmount);
        response.setCategories(
                supplier.getSuppliedCategories().stream()
                        .map(category -> {
                            SupplierResponse.SupplierCategoryResponse categoryResponse =
                                    new SupplierResponse.SupplierCategoryResponse();
                            categoryResponse.setId(category.getId());
                            categoryResponse.setName(category.getName());
                            return categoryResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }
}