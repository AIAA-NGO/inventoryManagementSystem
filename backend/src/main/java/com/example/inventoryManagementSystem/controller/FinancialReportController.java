package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.response.ProfitLossResponse;
import com.example.inventoryManagementSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/financial")
@RequiredArgsConstructor
public class FinancialReportController {
    private final ReportService reportService;

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossResponse> getProfitLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ProfitLossResponse report = reportService.generateProfitLossReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}