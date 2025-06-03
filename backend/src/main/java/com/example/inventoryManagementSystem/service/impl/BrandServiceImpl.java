package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.BrandRequest;
import com.example.inventoryManagementSystem.dto.response.BrandResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Brand;
import com.example.inventoryManagementSystem.repository.BrandRepository;
import com.example.inventoryManagementSystem.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final ModelMapper modelMapper;

    @Override
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand = modelMapper.map(request, Brand.class);
        Brand savedBrand = brandRepository.save(brand);
        return modelMapper.map(savedBrand, BrandResponse.class);
    }

    @Override
    public Page<BrandResponse> getAllBrands(int page, int size) {
        return brandRepository.findAll(PageRequest.of(page, size))
                .map(brand -> modelMapper.map(brand, BrandResponse.class));
    }

    @Override
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return modelMapper.map(brand, BrandResponse.class);
    }

    @Override
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        modelMapper.map(request, existingBrand);
        Brand updatedBrand = brandRepository.save(existingBrand);
        return modelMapper.map(updatedBrand, BrandResponse.class);
    }

    @Override
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    @Override
    public List<BrandResponse> searchBrands(String query) {
        return brandRepository.findByNameContainingIgnoreCase(query).stream()
                .map(brand -> modelMapper.map(brand, BrandResponse.class))
                .collect(Collectors.toList());
    }
}