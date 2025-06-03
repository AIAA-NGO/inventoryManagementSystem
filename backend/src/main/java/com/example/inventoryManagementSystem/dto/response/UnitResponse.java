package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnitResponse {
    private Long id;
    private String name;
    private String abbreviation;
    private LocalDateTime createdAt;
}