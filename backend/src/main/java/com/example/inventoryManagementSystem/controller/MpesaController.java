package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.exception.MpesaAuthorization;
import com.example.inventoryManagementSystem.model.MpesaTransactions;
import com.example.inventoryManagementSystem.repository.MpesaTransactionRepository;
import com.example.inventoryManagementSystem.dto.request.Mpesarequest;
import com.example.inventoryManagementSystem.dto.request.StkPushRequest;
import com.example.inventoryManagementSystem.dto.request.StkPushRequest.StkPushInitiateRequest;
import com.example.inventoryManagementSystem.dto.response.MpesaResponse;
import com.example.inventoryManagementSystem.dto.response.MpesaCallBack.Item;
import com.example.inventoryManagementSystem.dto.response.MpesaCallBack.MpesaCallbackRequest;
import com.example.inventoryManagementSystem.dto.response.MpesaCallBack.StkCallback;
//import com.example.inventoryManagementSystem.util.JsonMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/mpesa")
public class MpesaController {

    private final MpesaTransactionRepository mpesaTransactionsRepository;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper

    // --- Configuration from application.properties ---
    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.oauth.url}")
    private String oauthUrl;

    @Value("${mpesa.stkpush.url}")
    private String stkPushUrl;

    @Value("${mpesa.business.shortcode}")
    private String businessShortCode;

    @Value("${mpesa.passkey}")
    private String passkey; // The passkey provided by Safaricom

    @Value("${mpesa.transaction.type}")
    private String transactionType;

    @Value("${mpesa.callback.url}")
    private String callBackURL;

    // Default HttpClient for making external API calls
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(10)) // Set connection timeout
            .build();
    private String cachedAccessToken;
    private Instant tokenExpiryTime; // Stores the exact time the token expires

    // Buffer time before actual expiry to refresh the token (e.g., 5 minutes)
    private static final long EXPIRY_BUFFER_SECONDS = 300;

    public MpesaController(MpesaTransactionRepository mpesaTransactionsRepository, ObjectMapper objectMapper) {
        this.mpesaTransactionsRepository = mpesaTransactionsRepository;
        this.objectMapper = objectMapper;
    }

    // New endpoint or integrated into a payment initiation method
    @PostMapping("/stkpush/initiate")
    public ResponseEntity<String> initiateStkPush(
            @RequestBody StkPushInitiateRequest initiateRequest) {

        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Failed to get Mpesa access token.");
            }

            String timestamp = getCurrentMpesaTimestamp();
            String password = generateMpesaPassword(businessShortCode, passkey, timestamp);

            StkPushRequest stkPushBody = StkPushRequest.builder()
                    .businessShortCode(businessShortCode)
                    .password(password)
                    .timestamp(timestamp)
                    .transactionType(transactionType)
                    .amount(initiateRequest.getAmount())
                    .partyA(initiateRequest.getPhoneNumber())
                    .partyB(businessShortCode)
                    .phoneNumber(initiateRequest.getPhoneNumber())
                    .callBackURL(callBackURL)
                    .accountReference(initiateRequest.getAccountReference())
                    .transactionDesc(initiateRequest.getTransactionDesc())
                    .build();

            String jsonBody = objectMapper.writeValueAsString(stkPushBody);
            System.out.println("STK Push Request Body: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(stkPushUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("STK Push Status Code: " + response.statusCode());
            System.out.println("STK Push Response Body: " + response.body());

            // --- IMPORTANT: Process STK Push Response and Save Initial Transaction ---
            if (response.statusCode() == 200) {
                MpesaResponse stkPushResponse = objectMapper.readValue(response.body(), MpesaResponse.class);

                MpesaTransactions newTransaction = new MpesaTransactions();
                newTransaction.setMerchantRequestId(stkPushResponse.getMerchantRequestID());
                newTransaction.setCheckoutRequestId(stkPushResponse.getCheckoutRequestID());
                newTransaction.setStkResponseCode(stkPushResponse.getResponseCode());
                newTransaction.setStkResponseDescription(stkPushResponse.getResponseDescription());
                newTransaction.setStatus("PENDING"); // Initial status

                // Save initial request details for later comparison/auditing
                newTransaction.setInitialAmount(initiateRequest.getAmount());
                newTransaction.setInitialPhoneNumber(initiateRequest.getPhoneNumber());

                try {
                    mpesaTransactionsRepository.save(newTransaction);
                    System.out.println("Initial STK Push transaction saved: " + newTransaction.getCheckoutRequestId());
                } catch (Exception e) {
                    System.err.println("Error saving initial Mpesa transaction: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to save initial transaction details.");
                }

                return ResponseEntity.ok(response.body()); // Return the original Mpesa response
            } else {
                // Handle non-200 responses from Mpesa STK Push API
                // You might still want to log or save a "FAILED_INITIATION" record
                System.err.println("STK Push initiation failed from Mpesa API.");
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }

        } catch (URISyntaxException e) {
            System.err.println("URI Syntax Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Configuration error: Invalid URI for Mpesa API.");
        } catch (JsonProcessingException e) {
            System.err.println("JSON Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating STK Push request body or parsing Mpesa response.");
        } catch (IOException | InterruptedException e) {
            System.err.println("HTTP Client Error during STK Push: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Network error or interruption during STK Push.");
        } catch (Exception e) {
            System.err.println("Unexpected error initiating STK Push: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    // Existing callback method (mostly unchanged, just for context)
    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody MpesaCallbackRequest mpesaCallbackRequest) {

        try {
            String jsonString = objectMapper.writeValueAsString(mpesaCallbackRequest);
            System.out.println("Mpesa Callback Received: " + jsonString);
        } catch (JsonProcessingException e) {
            System.err.println("Error logging Mpesa callback: " + e.getMessage());
        }

        if (mpesaCallbackRequest == null || mpesaCallbackRequest.getBody() == null ||
                mpesaCallbackRequest.getBody().getStkCallback() == null) {
            System.err.println("Invalid Mpesa callback request received.");
            return ResponseEntity.badRequest().build();
        }

        StkCallback stkCallback = mpesaCallbackRequest.getBody().getStkCallback();

        // --- IMPORTANT: Find existing transaction using CheckoutRequestID ---
        String checkoutRequestId = stkCallback.getCheckoutRequestID();
        Optional<MpesaTransactions> existingTransactionOptional = mpesaTransactionsRepository
                .findByCheckoutRequestId(checkoutRequestId);

        MpesaTransactions transaction;
        if (existingTransactionOptional.isPresent()) {
            transaction = existingTransactionOptional.get();
            System.out.println("Found existing transaction for CheckoutRequestID: " + checkoutRequestId);
        } else {
            // This case should ideally not happen if initial save was successful,
            // but handle it defensively (e.g., log, create new record if necessary, or
            // return error)
            System.err.println("No existing transaction found for CheckoutRequestID: " + checkoutRequestId);
            transaction = new MpesaTransactions(); // Create a new one if not found
            transaction.setCheckoutRequestId(checkoutRequestId); // Set this so we have some link
            transaction.setMerchantRequestId(stkCallback.getMerchantRequestID());
            transaction.setStatus("CALLBACK_NO_MATCH"); // Indicate it's an unmatched callback
        }

        // Update common fields regardless of initial status
        transaction.setStatus(stkCallback.getResultDesc()); // Final status from callback

        // Check if the transaction was successful in Mpesa's eyes (ResultCode 0)
        if (stkCallback.getResultCode() == 0) {
            if (stkCallback.getCallbackMetadata() != null &&
                    stkCallback.getCallbackMetadata().getItem() != null) {

                for (Item item : stkCallback.getCallbackMetadata().getItem()) {
                    switch (item.getName()) {
                        case "Amount":
                            if (item.getValue() instanceof Number) {
                                transaction.setTransactionAmount(((Number) item.getValue()).doubleValue());
                            } else if (item.getValue() instanceof String) {
                                try {
                                    transaction.setTransactionAmount(Double.parseDouble((String) item.getValue()));
                                } catch (NumberFormatException e) {
                                    System.err.println("Error parsing Amount: " + item.getValue());
                                }
                            }
                            break;
                        case "MpesaReceiptNumber":
                            transaction.setMpesaReceiptNumber(String.valueOf(item.getValue())); // Store receipt number
                            transaction.setTransactionCode(String.valueOf(item.getValue())); // Often same as receipt
                            break;
                        case "TransactionDate":
                            if (item.getValue() != null) {
                                String dateString = String.valueOf(item.getValue());
                                transaction.setTransactionDate(dateString);
                            }
                            break;
                        case "PhoneNumber":
                            if (item.getValue() != null) {
                                transaction.setPhoneNumber(String.valueOf(item.getValue()));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            transaction.setStatus("COMPLETED"); // Explicitly set to COMPLETED for successful transactions
        } else {
            // Handle failed transactions based on ResultCode
            System.err.println("Mpesa Transaction Failed. ResultCode: " + stkCallback.getResultCode() +
                    ", ResultDesc: " + stkCallback.getResultDesc());
            // Clear or set default/N/A values for transaction specific details on failure
            transaction.setTransactionAmount(null); // Or 0.0
            transaction.setMpesaReceiptNumber("FAILED_" + stkCallback.getCheckoutRequestID());
            transaction.setTransactionCode("N/A");
            transaction.setPhoneNumber("N/A");
            transaction.setTransactionDate("N/A");
            // Status is already set to stkCallback.getResultDesc() earlier
        }

        try {
            mpesaTransactionsRepository.save(transaction); // This will update the existing record
            System.out.println(
                    "Mpesa transaction updated successfully for CheckoutRequestID: " + transaction.getCheckoutRequestId());
        } catch (Exception e) {
            System.err.println("Error saving/updating Mpesa transaction: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }

    // --- Helper Methods ---

    /**
     * Fetches the Mpesa OAuth access token, with manual caching.
     * Checks if the cached token is still valid before making an API call.
     *
     * @return The access token string, or null if an error occurs.
     */
    private synchronized String getAccessToken() {
        // Check if token is cached and not expired (with a buffer)
        if (cachedAccessToken != null && tokenExpiryTime != null &&
                Instant.now().isBefore(tokenExpiryTime.minusSeconds(EXPIRY_BUFFER_SECONDS))) {
            System.out.println("Returning cached Mpesa access token.");
            return cachedAccessToken;
        }

        System.out.println("Fetching new Mpesa access token...");
        String appKeySecret = consumerKey + ":" + consumerSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(appKeySecret.getBytes(StandardCharsets.ISO_8859_1));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(oauthUrl))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Auth Status Code: " + response.statusCode());
            System.out.println("Auth Response Body: " + response.body());

            if (response.statusCode() == 200) {
                MpesaAuthorization token = objectMapper.readValue(response.body(), MpesaAuthorization.class);
                cachedAccessToken = token.getAccessToken();
                // Calculate expiry time: current time + expires_in seconds
                long expiresInSeconds = Long.parseLong(token.getExpiresIn());
                tokenExpiryTime = Instant.now().plusSeconds(expiresInSeconds);
                System.out.println("New Mpesa access token fetched and cached. Expires at: " + tokenExpiryTime);
                return cachedAccessToken;
            } else {
                System.err.println("Failed to get access token. Response: " + response.body());
                cachedAccessToken = null; // Clear any old token if fetch failed
                tokenExpiryTime = null;
                return null;
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println("Error getting Mpesa access token: " + e.getMessage());
            e.printStackTrace();
            cachedAccessToken = null; // Clear if error occurs
            tokenExpiryTime = null;
            return null;
        }
    }

    /**
     * Generates the Mpesa password (Base64 encoded string of Shortcode + Passkey +
     * Timestamp).
     *
     * @param shortCode Your Mpesa Business Short Code.
     * @param passkey   The passkey provided by Safaricom.
     * @param timestamp The current timestamp in YYYYMMDDHHmmss format.
     * @return Base64 encoded password string.
     */
    private String generateMpesaPassword(String shortCode, String passkey, String timestamp) {
        String rawPassword = shortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the current timestamp in YYYYMMDDHHmmss format required by Mpesa.
     *
     * @return Formatted timestamp string.
     */
    private String getCurrentMpesaTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }
    @GetMapping("/payment-status")
    public ResponseEntity<?> getPaymentStatus(
            @RequestParam("checkout_id") String checkoutRequestId,
            @RequestParam("merchant_id") String merchantRequestId) {

        try {
            Optional<MpesaTransactions> transactionOptional = mpesaTransactionsRepository
                    .findByCheckoutRequestIdAndMerchantRequestId(checkoutRequestId, merchantRequestId);

            if (transactionOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Transaction not found for given checkout_id and merchant_id.");
            }

            MpesaTransactions transaction = transactionOptional.get();
            return ResponseEntity.ok(transaction);

        } catch (Exception e) {
            System.err.println("Error querying transaction status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching transaction status.");
        }
    }

}
