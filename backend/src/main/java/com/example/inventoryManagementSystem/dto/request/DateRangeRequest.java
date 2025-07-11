package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DateRangeRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}