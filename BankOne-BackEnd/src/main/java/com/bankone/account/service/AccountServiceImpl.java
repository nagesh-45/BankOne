package com.bankone.account.service;

import com.bankone.account.dto.DepositRequest;
import com.bankone.account.entity.Account;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.UpdateAccountStatusRequest;
import com.bankone.account.repository.AccountRepository;
import com.bankone.account.repository.AccountPolicyRepository;
import com.bankone.account.util.AccountNumberGenerator;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.account.enums.AccountStatus;
import com.bankone.account.entity.AccountPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bankone.account.specification.AccountSpecification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import com.bankone.transaction.enums.TransactionType;
import com.bankone.transaction.service.TransactionService;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountPolicyRepository accountPolicyRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final TransactionService transactionService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              AccountPolicyRepository accountPolicyRepository,
                              AccountNumberGenerator accountNumberGenerator,
                              TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.accountPolicyRepository = accountPolicyRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.transactionService = transactionService;
    }

    @Override
    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {
        // Validate customer
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Customer not found");
        }
        Customer customer = customerOpt.get();

        // Validate account policy
        AccountPolicy policy = accountPolicyRepository
                .findByAccountTypeAndCurrencyCodeAndActiveTrue(
                        request.getAccountType(),
                        request.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active account policy found for "
                                + request.getAccountType()
                                + " / "
                                + request.getCurrencyCode()));

        LocalDateTime now = LocalDateTime.now();

        if (policy.getEffectiveFrom().isAfter(now)) {
            throw new IllegalArgumentException("Account policy is not yet effective");
        }

        if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(now)) {
            throw new IllegalArgumentException("Account policy has expired");
        }

        if (Boolean.TRUE.equals(policy.getOpeningDepositRequired())) {

            if (request.getOpeningDeposit() == null) {
                throw new IllegalArgumentException("Opening deposit is required");
            }

            if (request.getOpeningDeposit().compareTo(policy.getRequiredOpeningDeposit()) < 0) {
                throw new IllegalArgumentException(
                        "Minimum opening deposit is " + policy.getRequiredOpeningDeposit());
            }
        }

        // Get next ordinal for account
        Long ordinal = accountRepository.getNextOrdinal();

        // Generate account number
        String accountNumber = accountNumberGenerator.generate(
                request.getBranchCode(),
                request.getAccountType(),
                ordinal,
                request.getCurrencyCode()
        );

        BigDecimal openingDeposit = request.getOpeningDeposit() == null
                ? BigDecimal.ZERO
                : request.getOpeningDeposit();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBranchCode(request.getBranchCode());
        account.setAccountType(request.getAccountType().name());
        account.setOrdinal(ordinal.intValue());
        account.setCurrencyCode(request.getCurrencyCode().name());
        String checkDigitStr = accountNumber.substring(accountNumber.length() - 1);
        try {
            account.setCheckDigit(Integer.parseInt(checkDigitStr));
        } catch (NumberFormatException e) {
            account.setCheckDigit(0);
        }
        account.setAvailableBalance(openingDeposit);
        account.setLedgerBalance(openingDeposit);
        account.setDebitCount(0);
        account.setCreditCount(openingDeposit.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        if (openingDeposit.compareTo(BigDecimal.ZERO) > 0) {
            account.setLastCreditAt(now);
            account.setLastTransactionAt(now);
        }
        account.setStatus(AccountStatus.ACTIVE.name());
        account.setCreatedAt(now);
        account.setActivatedAt(now);
        account.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM");
        account.setCustomer(customer);

        Account savedAccount = accountRepository.save(account);

        if (openingDeposit.compareTo(BigDecimal.ZERO) > 0) {
            transactionService.record(
                    savedAccount,
                    TransactionType.CREDIT,
                    openingDeposit,
                    savedAccount.getLedgerBalance(),
                    "Opening deposit",
                    savedAccount.getCreatedBy()
            );
        }

        return toResponse(savedAccount);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        return accountRepository.findByCustomerCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<AccountResponse> getAccountsByCustomerId(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        return accountRepository.findByCustomerCustomerId(customerId, pageable)
                .map(this::toResponse);
    }


    @Override
    public AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Account status is required");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        AccountStatus newStatus = request.getStatus();
        account.setStatus(newStatus.name());

        if (newStatus == AccountStatus.CLOSED) {
            account.setClosedAt(LocalDateTime.now());
            account.setClosedBy("SYSTEM");
        } else {
            account.setClosedAt(null);
            account.setClosedBy(null);
        }

        if (newStatus == AccountStatus.ACTIVE && account.getActivatedAt() == null) {
            account.setActivatedAt(LocalDateTime.now());
        }

        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse deposit(Long accountId, DepositRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Deposit amount is required");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!AccountStatus.ACTIVE.name().equals(account.getStatus())) {
            throw new IllegalArgumentException("Deposits are allowed only on ACTIVE accounts");
        }

        BigDecimal amount = request.getAmount();
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setLedgerBalance(account.getLedgerBalance().add(amount));

        int creditCount = account.getCreditCount() == null ? 0 : account.getCreditCount();
        account.setCreditCount(creditCount + 1);
        account.setLastCreditAt(LocalDateTime.now());
        account.setLastTransactionAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);

        transactionService.record(
                saved,
                TransactionType.CREDIT,
                amount,
                saved.getLedgerBalance(),
                "Deposit",
                "SYSTEM"
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> searchAccounts(String search, Pageable pageable) {
        return accountRepository
                .findAll(AccountSpecification.matching(search), pageable)
                .map(this::toResponse);
    }

    private AccountResponse toResponse(Account savedAccount) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(savedAccount.getAccountId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setBranchCode(savedAccount.getBranchCode());
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
        if (savedAccount.getCustomer() != null) {
            response.setCustomerId(savedAccount.getCustomer().getCustomerId());
        }
        return response;
    }
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return toResponse(account);
    }

}