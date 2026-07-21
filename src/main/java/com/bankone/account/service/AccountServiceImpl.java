package com.bankone.account.service;

import com.bankone.account.entity.Account;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.AccountResponse;
import com.bankone.account.repository.AccountRepository;
import com.bankone.account.repository.AccountPolicyRepository;
import com.bankone.account.util.AccountNumberGenerator;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.account.enums.AccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountPolicyRepository accountPolicyRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              AccountPolicyRepository accountPolicyRepository,
                              AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.accountPolicyRepository = accountPolicyRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Override
    public AccountResponse openAccount(OpenAccountRequest request) {
        // Validate customer
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Customer not found");
        }
        Customer customer = customerOpt.get();

        // Get next ordinal for account
        Long ordinal = accountRepository.getNextOrdinal();

        // Generate account number
        String accountNumber = accountNumberGenerator.generate(
                request.getBranchCode(),
                request.getAccountType(),
                ordinal,
                request.getCurrencyCode()
        );

        // Create Account entity
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBranchCode(request.getBranchCode());
        account.setAccountType(request.getAccountType().name());
        account.setOrdinal(ordinal.intValue());
        account.setCurrencyCode(request.getCurrencyCode().name());
        // Assume check digit is always the last char(s) of account number
        String checkDigitStr = accountNumber.substring(accountNumber.length() - 1);
        try {
            account.setCheckDigit(Integer.parseInt(checkDigitStr));
        } catch (NumberFormatException e) {
            account.setCheckDigit(0);
        }
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setLedgerBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE.name());
        account.setCreatedAt(LocalDateTime.now());
        account.setCreatedBy(request.getCreatedBy());
        account.setCustomer(customer);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setLedgerBalance(BigDecimal.ZERO);

        account.setDebitCount(0);
        account.setCreditCount(0);

        account.setStatus(AccountStatus.ACTIVE.name());
        account.setCreatedAt(LocalDateTime.now());
        account.setActivatedAt(LocalDateTime.now());
        account.setCreatedBy(request.getCreatedBy());

        account.setCustomer(customer);
        Account savedAccount = accountRepository.save(account);

        // Build AccountResponse
        AccountResponse response = new AccountResponse();
        response.setAccountId(savedAccount.getAccountId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setAccountType(savedAccount.getAccountType());
        response.setOrdinal(savedAccount.getOrdinal());
        response.setCurrencyCode(savedAccount.getCurrencyCode());
        response.setCheckDigit(savedAccount.getCheckDigit());
        response.setAvailableBalance(savedAccount.getAvailableBalance());
        response.setLedgerBalance(savedAccount.getLedgerBalance());
        response.setStatus(AccountStatus.valueOf(savedAccount.getStatus()));
        response.setCreatedAt(savedAccount.getCreatedAt());
        response.setActivatedAt(savedAccount.getActivatedAt());
        response.setCreatedBy(savedAccount.getCreatedBy());
        response.setCustomerId(savedAccount.getCustomer().getCustomerId());
        return response;
    }
}