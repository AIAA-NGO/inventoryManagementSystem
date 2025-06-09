package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class SupplierRequest {
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must be less than 200 characters")
    private String companyName;

    @NotBlank(message = "Contact person is required")
    @Size(max = 100, message = "Contact person must be less than 100 characters")
    private String contactPerson;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,13}$", message = "Phone number must be between 6-20 digits and may include +, - or spaces")
    private String phone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @Size(max = 200, message = "Website must be less than 200 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$",
            message = "Website must be a valid URL")
    private String website;

    @NotEmpty(message = "At least one category is required")
    private List<Long> categoryIds;

}