package com.example.inventoryManagementSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class MpesaCallBack {

    @Data
    public static class MpesaCallbackRequest {
        @JsonProperty("Body")
        private MpesaCallbackBody body;
    }

    @Data
    public static class MpesaCallbackBody {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        @JsonProperty("MerchantRequestID")
        private String merchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestID;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        @JsonProperty("Item")
        private List<Item> item;
    }

    @Data
    public static class Item {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private Object value;
    }
}

