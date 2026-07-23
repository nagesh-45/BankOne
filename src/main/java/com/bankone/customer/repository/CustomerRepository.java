package com.bankone.customer.repository;

import com.bankone.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Long customerId);

    boolean existsByPhoneNumberAndCustomerIdNot(String phoneNumber, Long customerId);
}