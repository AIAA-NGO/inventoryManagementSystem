package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashFlowResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal cashInflows;
    private BigDecimal cashOutflows;
    private BigDecimal netCashFlow;
}