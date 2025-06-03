package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.BrandRequest;
import com.example.inventoryManagementSystem.dto.response.BrandResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);
    Page<BrandResponse> getAllBrands(int page, int size);
    BrandResponse getBrandById(Long id);
    BrandResponse updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
    List<BrandResponse> searchBrands(String query);
}