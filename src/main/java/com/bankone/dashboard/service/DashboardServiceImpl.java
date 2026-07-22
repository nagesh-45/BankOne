package com.bankone.dashboard.service;

import com.bankone.account.repository.AccountRepository;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.dashboard.dto.DashboardResponse;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public DashboardServiceImpl(CustomerRepository customerRepository,
                                AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public DashboardResponse getDashboardSummary() {

        long customerCount = customerRepository.count();
        long accountCount = accountRepository.count();

        return new DashboardResponse(
                customerCount,
                accountCount,
                0,
                0
        );
    }
}