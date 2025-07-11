package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.model.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        modelMapper.addMappings(new PropertyMap<ProductRequest, Product>() {
            @Override
            protected void configure() {
                skip().setId(null);
                skip().setCategory(null);
                skip().setBrand(null);
                skip().setUnit(null);
                skip().setCreatedAt(null);
                skip().setUpdatedAt(null);
            }
        });
        modelMapper.addMappings(new PropertyMap<Product, ProductResponse>() {
            @Override
            protected void configure() {
                map().setCategoryId(source.getCategory() != null ? source.getCategory().getId() : null);
                map().setCategoryName(source.getCategory() != null ? source.getCategory().getName() : null);
                map().setBrandId(source.getBrand() != null ? source.getBrand().getId() : null);
                map().setBrandName(source.getBrand() != null ? source.getBrand().getName() : null);
                map().setUnitId(source.getUnit() != null ? source.getUnit().getId() : null);
                map().setUnitName(source.getUnit() != null ? source.getUnit().getName() : null);
            }
        });
        return modelMapper;
    }
}