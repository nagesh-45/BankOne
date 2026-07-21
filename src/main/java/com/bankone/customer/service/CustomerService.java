package com.bankone.customer.service;

import com.bankone.customer.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer getCustomerById(Long customerId);

    List<Customer> getAllCustomers();

    Customer updateCustomer(Long customerId, Customer customer);

    void deleteCustomer(Long customerId);
}