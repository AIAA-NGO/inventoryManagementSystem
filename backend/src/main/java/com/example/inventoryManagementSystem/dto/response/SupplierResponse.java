package com.example.inventoryManagementSystem.dto.response;

import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor // Add this annotation
@NoArgsConstructor
public class SupplierResponse {
    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String website;
    private Double rating;
    private Double totalPurchasesAmount;
    private List<SupplierCategoryResponse> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupplierCategoryResponse {
        private Long id;
        private String name;

    }
}