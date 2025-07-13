package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ProductService {
    Page<ProductResponse> getAllProducts(int page, int size);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse createProductWithImage(ProductRequest request, MultipartFile imageFile);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    List<ProductResponse> searchProducts(String query);
    List<ProductResponse> getLowStockProducts();
    void importProducts(MultipartFile file);
    byte[] exportProducts();
    List<ProductResponse> getProductsBySupplier(Long supplierId);
    List<ProductResponse> getProductsByCategory(Long categoryId);
    List<ProductResponse> getExpiringProducts(LocalDate thresholdDate);
    ProductResponse deleteProductImage(Long productId);
    ProductResponse updateProductWithImage(Long id, ProductRequest request, MultipartFile imageFile);
    List<ProductResponse> getAllProductsList();
}