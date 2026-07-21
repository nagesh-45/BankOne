package com.bankone.account.repository;

import com.bankone.account.entity.Account;
import com.bankone.account.enums.AccountStatus;
import com.bankone.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomer(Customer customer);

    List<Account> findByCustomerCustomerId(Long customerId);

    List<Account> findByStatus(AccountStatus status);

    @Query(value = "SELECT nextval('account_ordinal_seq')", nativeQuery = true)
    Long getNextOrdinal();
}