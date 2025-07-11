package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.CategoryRequest;
import com.example.inventoryManagementSystem.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    Page<CategoryResponse> getAllCategories(int page, int size);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> searchCategories(String query);
}