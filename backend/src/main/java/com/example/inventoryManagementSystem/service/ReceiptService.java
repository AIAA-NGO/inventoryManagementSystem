package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.response.ReceiptResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;

public interface ReceiptService {
    /**
     * Generates a detailed receipt for a completed sale
     * @param saleId The ID of the sale to generate receipt for
     * @return ReceiptResponse containing all receipt details
     * @throws ResourceNotFoundException if sale is not found
     */
    ReceiptResponse generateReceipt(Long saleId) throws ResourceNotFoundException;

    /**
     * Generates a simplified receipt for display purposes
     * @param saleId The ID of the sale
     * @return Simplified receipt data
     * @throws ResourceNotFoundException if sale is not found
     */
    ReceiptResponse generateSimpleReceipt(Long saleId) throws ResourceNotFoundException;

    /**
     * Generates a receipt in HTML format for printing/emailing
     * @param saleId The ID of the sale
     * @return HTML formatted receipt
     * @throws ResourceNotFoundException if sale is not found
     */
    String generateHtmlReceipt(Long saleId) throws ResourceNotFoundException;

    /**
     * Generates a receipt in plain text format
     * @param saleId The ID of the sale
     * @return Plain text receipt
     * @throws ResourceNotFoundException if sale is not found
     */
    String generateTextReceipt(Long saleId) throws ResourceNotFoundException;
}