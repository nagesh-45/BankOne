package com.bankone.customer.service;

import com.bankone.customer.dto.CreateCustomerRequest;
import com.bankone.customer.dto.UpdateCustomerRequest;
import com.bankone.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(CreateCustomerRequest request);

    Customer getCustomerById(Long customerId);

    List<Customer> getAllCustomers();

    Page<Customer> searchCustomers(String search, Pageable pageable);

    Customer updateCustomer(Long customerId, UpdateCustomerRequest request);

    void deleteCustomer(Long customerId);
}
