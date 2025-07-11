package com.example.inventoryManagementSystem.config;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.model.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "mpesa.daraja")

public class MpesaConfig{
    // Getters and Setters
    private String consumerKey;
    private String consumerSecret;
    private String passkey;
    private String shortcode;
    private String environment; // sandbox or production
    private String callbackUrl;
    private String confirmationUrl;
    private String validationUrl;

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setConfirmationUrl(String confirmationUrl) {
        this.confirmationUrl = confirmationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public String getBaseUrl() {
        return "sandbox".equals(environment)
                ? "https://sandbox.safaricom.co.ke"
                : "https://api.safaricom.co.ke";
    }
}


