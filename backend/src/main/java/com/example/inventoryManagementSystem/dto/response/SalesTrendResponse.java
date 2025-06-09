package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;


@Data
@AllArgsConstructor
public class SalesTrendResponse {
    private String period;
    private BigDecimal amount;
    private Date date;
}