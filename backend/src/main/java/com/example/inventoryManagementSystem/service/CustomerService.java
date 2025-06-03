package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.CustomerRequest;
import com.example.inventoryManagementSystem.dto.response.CustomerPurchaseResponse;
import com.example.inventoryManagementSystem.dto.response.CustomerResponse;
import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getAllCustomers();
    CustomerResponse getCustomerById(Long id);
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
    List<CustomerResponse> searchCustomers(String query);
    CustomerPurchaseResponse getCustomerPurchaseHistory(Long customerId);
}