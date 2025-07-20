package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.response.ReceiptResponse;
import com.example.inventoryManagementSystem.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/{saleId}")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long saleId) {
        return ResponseEntity.ok(receiptService.generateReceipt(saleId));
    }

    @GetMapping("/{saleId}/html")
    public ResponseEntity<String> getHtmlReceipt(@PathVariable Long saleId) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(receiptService.generateHtmlReceipt(saleId));
    }
}
