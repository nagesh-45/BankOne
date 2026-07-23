package com.bankone.dashboard.service;

import com.bankone.account.repository.AccountRepository;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.dashboard.dto.DashboardResponse;
import com.bankone.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(CustomerRepository customerRepository,
                                AccountRepository accountRepository,
                                UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardResponse getDashboardSummary() {

        long customerCount = customerRepository.count();
        long accountCount = accountRepository.count();
        long employeeCount = userRepository.countEmployees();

        return new DashboardResponse(
                customerCount,
                accountCount,
                employeeCount,
                0
        );
    }
}