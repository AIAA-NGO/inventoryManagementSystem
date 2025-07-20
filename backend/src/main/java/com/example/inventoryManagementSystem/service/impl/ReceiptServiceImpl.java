package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.response.ReceiptItemResponse;
import com.example.inventoryManagementSystem.dto.response.ReceiptResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Sale;
import com.example.inventoryManagementSystem.model.SaleItem;
import com.example.inventoryManagementSystem.repository.SaleRepository;
import com.example.inventoryManagementSystem.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final SaleRepository saleRepository;
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

    @Override
    public ReceiptResponse generateReceipt(Long saleId) throws ResourceNotFoundException {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        List<ReceiptItemResponse> items = sale.getItems().stream()
                .map(this::mapToReceiptItemResponse)
                .collect(Collectors.toList());

        return ReceiptResponse.builder()
                .receiptNumber("RCPT-" + sale.getId())
                .date(sale.getSaleDate())
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : "Guest")
                .items(items)
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .total(sale.getTotal())
                .paymentMethod(sale.getPaymentMethod())
                .appliedDiscountCode(sale.getAppliedDiscountCode())
                .build();
    }

    @Override
    public ReceiptResponse generateSimpleReceipt(Long saleId) throws ResourceNotFoundException {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        return ReceiptResponse.builder()
                .receiptNumber("RCPT-" + sale.getId())
                .date(sale.getSaleDate())
                .total(sale.getTotal())
                .paymentMethod(sale.getPaymentMethod())
                .build();
    }

    @Override
    public String generateHtmlReceipt(Long saleId) throws ResourceNotFoundException {
        ReceiptResponse receipt = generateReceipt(saleId);

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 300px; margin: 0 auto;'>");
        html.append("<h2 style='text-align: center;'>INVENTORY STORE</h2>");
        html.append("<p style='text-align: center;'>123 Business Street, KISII<br>Tel: +254 700 000000</p>");
        html.append("<hr style='border-top: 1px dashed #ccc;'>");

        html.append("<p><strong>Date:</strong> ").append(receipt.getDate().format(dateFormatter)).append("</p>");
        html.append("<p><strong>Receipt #:</strong> ").append(receipt.getReceiptNumber().replace("RCPT-", "")).append("</p>");

        html.append("<table style='width: 100%; border-collapse: collapse; margin: 10px 0;'>");
        html.append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<th style='text-align: left;'>ITEM</th>")
                .append("<th style='text-align: right;'>QTY</th>")
                .append("<th style='text-align: right;'>PRICE</th>")
                .append("<th style='text-align: right;'>TOTAL</th>")
                .append("</tr>");

        for (ReceiptItemResponse item : receipt.getItems()) {
            html.append("<tr style='border-bottom: 1px solid #eee;'>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td style='text-align: right;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='text-align: right;'>").append(formatCurrency(item.getUnitPrice())).append("</td>")
                    .append("<td style='text-align: right;'>").append(formatCurrency(item.getTotalPrice())).append("</td>")
                    .append("</tr>");

            if (item.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                html.append("<tr style='color: green;'>")
                        .append("<td colspan='3' style='text-align: right;'>Discount:</td>")
                        .append("<td style='text-align: right;'>-").append(formatCurrency(item.getDiscountAmount())).append("</td>")
                        .append("</tr>");
            }
        }

        html.append("</table>");
        html.append("<hr style='border-top: 1px dashed #ccc;'>");

        html.append("<p style='text-align: right;'><strong>Subtotal:</strong> ").append(formatCurrency(receipt.getSubtotal())).append("</p>");
        if (receipt.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<p style='text-align: right; color: green;'><strong>Discount:</strong> -").append(formatCurrency(receipt.getDiscountAmount())).append("</p>");
        }
        html.append("<p style='text-align: right;'><strong>Tax (16%):</strong> ").append(formatCurrency(receipt.getTaxAmount())).append("</p>");
        html.append("<p style='text-align: right; font-weight: bold;'><strong>TOTAL:</strong> ").append(formatCurrency(receipt.getTotal())).append("</p>");

        html.append("<hr style='border-top: 1px dashed #ccc;'>");
        html.append("<p><strong>Payment Method:</strong> ").append(receipt.getPaymentMethod()).append("</p>");
        if (receipt.getAppliedDiscountCode() != null) {
            html.append("<p><strong>Discount Code:</strong> ").append(receipt.getAppliedDiscountCode()).append("</p>");
        }
        html.append("<p><strong>Customer:</strong> ").append(receipt.getCustomerName()).append("</p>");

        html.append("<p style='text-align: center; margin-top: 20px;'>Thank you for your business!</p>");
        html.append("<p style='text-align: center; font-size: 0.8em;'>* Items cannot be returned/exchanged *</p>");
        html.append("<p style='text-align: center; font-size: 0.8em;'>Powered by Inventory Management System</p>");
        html.append("</div>");

        return html.toString();
    }

    private ReceiptItemResponse mapToReceiptItemResponse(SaleItem item) {
        return ReceiptItemResponse.builder()
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private String formatCurrency(BigDecimal amount) {
        return "Ksh " + currencyFormat.format(amount);
    }

    @Override
    public String generateTextReceipt(Long saleId) throws ResourceNotFoundException {
        ReceiptResponse receipt = generateReceipt(saleId);

        StringBuilder text = new StringBuilder();
        text.append("INVENTORY STORE\n");
        text.append("123 Business Street, KISII\n");
        text.append("Tel: +254 700 000000\n\n");

        text.append("Date: ").append(receipt.getDate().format(dateFormatter)).append("\n");
        text.append("Receipt #: ").append(receipt.getReceiptNumber().replace("RCPT-", "")).append("\n\n");

        text.append(String.format("%-30s %4s %8s %8s%n", "ITEM", "QTY", "PRICE", "TOTAL"));
        text.append("------------------------------------------------\n");

        for (ReceiptItemResponse item : receipt.getItems()) {
            text.append(String.format("%-30s %4d %8s %8s%n",
                    item.getProductName(),
                    item.getQuantity(),
                    formatCurrency(item.getUnitPrice()),
                    formatCurrency(item.getTotalPrice())));

            if (item.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                text.append(String.format("%42s%n", "Discount: -" + formatCurrency(item.getDiscountAmount())));
            }
        }

        text.append("\n");
        text.append(String.format("%42s%n", "Subtotal: " + formatCurrency(receipt.getSubtotal())));
        if (receipt.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            text.append(String.format("%42s%n", "Discount: -" + formatCurrency(receipt.getDiscountAmount())));
        }
        text.append(String.format("%42s%n", "Tax (16%): " + formatCurrency(receipt.getTaxAmount())));
        text.append(String.format("%42s%n", "TOTAL: " + formatCurrency(receipt.getTotal())));

        text.append("\n");
        text.append("Payment Method: ").append(receipt.getPaymentMethod()).append("\n");
        if (receipt.getAppliedDiscountCode() != null) {
            text.append("Discount Code: ").append(receipt.getAppliedDiscountCode()).append("\n");
        }
        text.append("Customer: ").append(receipt.getCustomerName()).append("\n\n");

        text.append("Thank you for your business!\n");
        text.append("* Items cannot be returned/exchanged *\n");
        text.append("Powered by Inventory Management System\n");

        return text.toString();
    }
}