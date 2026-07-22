package com.bankone.customer.service;

import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.enums.AccountType;
import com.bankone.account.service.AccountService;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.customer.dto.CreateCustomerRequest;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.customer.specification.CustomerSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountService accountService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AccountService accountService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setStatus(request.getStatus());

        Customer savedCustomer = customerRepository.save(customer);
        if (request.getAccountType() == AccountType.LOAN) {
            throw new IllegalArgumentException(
                    "Loan accounts cannot be created during customer onboarding. Add a loan account after the customer is created.");
        }

        OpenAccountRequest accountRequest = new OpenAccountRequest();
        accountRequest.setCustomerId(savedCustomer.getCustomerId());
        accountRequest.setBranchCode(request.getBranchCode());
        accountRequest.setAccountType(request.getAccountType());
        accountRequest.setCurrencyCode(request.getCurrencyCode());
        accountRequest.setOpeningDeposit(request.getOpeningDeposit());
        accountRequest.setCreatedBy("SYSTEM");

        accountService.openAccount(accountRequest);

        return savedCustomer;
    }

    @Override
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Page<Customer> searchCustomers(String search, Pageable pageable) {
        return customerRepository.findAll(
                CustomerSpecification.containsText(search),
                pageable
        );
    }

    @Override
    public Customer updateCustomer(Long customerId, Customer customer) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setDateOfBirth(customer.getDateOfBirth());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setStatus(customer.getStatus());

        return customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        customerRepository.delete(existingCustomer);
    }
}
