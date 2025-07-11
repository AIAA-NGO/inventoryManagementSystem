package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.SupplierRequest;
import com.example.inventoryManagementSystem.dto.response.SupplierResponse;
import com.example.inventoryManagementSystem.model.Category;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Purchase;

import java.util.List;
import java.util.function.Supplier;

public interface SupplierService {
    SupplierResponse createSupplier(SupplierRequest request);
    List<SupplierResponse> getAllSuppliers();
    SupplierResponse getSupplierById(Long id);
    SupplierResponse updateSupplier(Long id, SupplierRequest request);
    void deleteSupplier(Long id);
    List<Product> getProductsBySupplier(Long supplierId);
    List<Purchase> getPurchasesBySupplier(Long supplierId);
    List<SupplierResponse> searchSuppliers(String companyName, String contactPerson, String category);
    List<Category> getCategoriesBySupplier(Long supplierId);
    void addCategoryToSupplier(Long supplierId, Long categoryId);
    void removeCategoryFromSupplier(Long supplierId, Long categoryId);
}