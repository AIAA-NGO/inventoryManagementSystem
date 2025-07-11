package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.CustomerRequest;
import com.example.inventoryManagementSystem.dto.response.CustomerPurchaseResponse;
import com.example.inventoryManagementSystem.dto.response.CustomerResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Customer;
import com.example.inventoryManagementSystem.repository.CustomerRepository;
import com.example.inventoryManagementSystem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customer -> modelMapper.map(customer, CustomerResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return modelMapper.map(customer, CustomerResponse.class);
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = modelMapper.map(request, Customer.class);
        Customer savedCustomer = customerRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerResponse.class);
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        modelMapper.map(request, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return modelMapper.map(updatedCustomer, CustomerResponse.class);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .build();
    }

    @Override
    public List<CustomerResponse> searchCustomers(String query) {
        List<Customer> customers;
        if (query == null || query.trim().isEmpty()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository.searchCustomers(query);
        }

        return customers.stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerPurchaseResponse getCustomerPurchaseHistory(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        CustomerPurchaseResponse response = new CustomerPurchaseResponse();
        response.setCustomerId(customer.getId());
        response.setCustomerName(customer.getName());
        response.setPurchases(List.of());

        return response;
    }
}