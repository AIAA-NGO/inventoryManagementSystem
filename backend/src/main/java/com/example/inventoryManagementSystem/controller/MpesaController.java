package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.exception.MpesaAuthorization;
import com.example.inventoryManagementSystem.dto.request.Mpesarequest;
import com.example.inventoryManagementSystem.dto.response.MpesaResponse;
import com.example.inventoryManagementSystem.util.JsonMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@RestController
@RequestMapping("/try/mpesa")
public class MpesaController {

    @PostMapping("/response")
    public ResponseEntity<Void> mpesaresponse(@RequestBody MpesaResponse request) {
        System.out.println(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pay")
    public ResponseEntity<Void> mpesaauthorization() throws IOException, InterruptedException, URISyntaxException {
        HttpClient httpclient = HttpClient.newHttpClient();
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"))
                    .header("Authorization",
                            "Basic R1RhRjRjc090TUpDZkJaNzdaMTlLSDg5Y1p2RkpzbHd6bVM5QXNjYlFHTGprZlBPOkdkVkpFbHJYYkdhUHl0Y3pwVFlWRTk2VXNzNkJFSUpvWlFBc01CTUxjN24ySldzVmhnSzNtdW5selNsTlhST0E=")
                    .version(HttpClient.Version.HTTP_2)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpResponse<String> response = httpclient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/process-request")
    public ResponseEntity<Void> MpesaRequest(@RequestBody Mpesarequest request) throws IOException, InterruptedException {

        //Authorization
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httprequest = null;
        try {
            httprequest = HttpRequest.newBuilder()
                    .uri(new URI("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"))
                    .header("Authorization",
                            "Basic R1RhRjRjc090TUpDZkJaNzdaMTlLSDg5Y1p2RkpzbHd6bVM5QXNjYlFHTGprZlBPOkdkVkpFbHJYYkdhUHl0Y3pwVFlWRTk2VXNzNkJFSUpvWlFBc01CTUxjN24ySldzVmhnSzNtdW5selNsTlhST0E=")
                    .version(HttpClient.Version.HTTP_2)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpResponse<String> response = client.send(httprequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());

        MpesaAuthorization token= JsonMapper.toMpesaAuthorization(response.body());
        System.out.println(token);
        try {
            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(new URI("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"))
                    .header("Authorization",
                            "Bearer "+  token.getAccess_token())
                    .header("Content-Type", "application/json")
                    .version(HttpClient.Version.HTTP_2)
                    .POST(HttpRequest.BodyPublishers.ofString("{\n" +
                            "    \"BusinessShortCode\": 174379,\n" +
                            "    \"Password\": \"MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkxOTIwMjUwNjExMTIwMTA4\",\n" +
                            "    \"Timestamp\": \"20250611120108\",\n" +
                            "    \"TransactionType\": \"CustomerPayBillOnline\",\n" +
                            "    \"Amount\": 2,\n" +
                            "    \"PartyA\": 254741989277,\n" +
                            "    \"PartyB\": 174379,\n" +
                            "    \"PhoneNumber\": 254741989277,\n" +
                            "    \"CallBackURL\": \"https://a30b-197-248-195-25.ngrok-free.app/try/mpesa/response\",\n" +
                            "    \"AccountReference\": \"CompanyXLTD\",\n" +
                            "    \"TransactionDesc\": \"Payment of X\" \n" +
                            "  }"))
                    .build();
            System.out.println(request2);


            try {
                HttpResponse<String> response2  = client.send(request2,HttpResponse.BodyHandlers.ofString());
                System.out.println(response);

                System.out.println("Status Code: " + response2.statusCode());
                System.out.println("Response Body: " + response2.body());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }



        return ResponseEntity.ok().build();
    }
}
