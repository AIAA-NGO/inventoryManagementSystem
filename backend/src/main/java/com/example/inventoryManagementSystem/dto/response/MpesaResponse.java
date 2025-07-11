package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import lombok.NonNull;

@Data
public class MpesaResponse {
    @NonNull
    private String MerchantRequestID;
    @NonNull
    private String CheckoutRequestID;
    @NonNull
    private String ResponseCode;
    @NonNull
    private String ResponseDescription;
    @NonNull
    private String CustomerMessage;
}
