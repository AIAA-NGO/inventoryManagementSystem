package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendResponse {
    private String period;
    private BigDecimal amount;
    private LocalDate date;

    public SalesTrendResponse(String period, Double amount, java.sql.Date date) {
        this.period = period;
        this.amount = BigDecimal.valueOf(amount);
        this.date = date.toLocalDate();
    }
}