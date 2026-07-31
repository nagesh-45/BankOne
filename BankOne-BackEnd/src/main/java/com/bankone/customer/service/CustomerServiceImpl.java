package com.bankone.customer.service;

import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.enums.AccountType;
import com.bankone.account.service.AccountService;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ConflictException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.customer.dto.CreateCustomerRequest;
import com.bankone.customer.dto.UpdateCustomerRequest;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.customer.specification.CustomerSpecification;
import com.bankone.user.dto.CreateUserRequest;
import com.bankone.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountService accountService;
    private final UserService userService;
    private final AuditEventService auditEventService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AccountService accountService,
                               UserService userService,
                               AuditEventService auditEventService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
        this.userService = userService;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String phone = request.getPhoneNumber() == null ? null : request.getPhoneNumber().trim();

        if (email != null && customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(email, -1L)) {
            throw new ConflictException("Email already exists");
        }
        if (phone != null && customerRepository.existsByPhoneNumberAndCustomerIdNot(phone, -1L)) {
            throw new ConflictException("Phone number already exists");
        }

        boolean wantsPortal = StringUtils.hasText(request.getPortalUsername())
                || StringUtils.hasText(request.getPortalPassword());
        if (wantsPortal) {
            if (!StringUtils.hasText(request.getPortalUsername())
                    || !StringUtils.hasText(request.getPortalPassword())) {
                throw new BadRequestException(
                        "Both portal username and password are required to create a portal login");
            }
            if (request.getPortalPassword().length() < 6) {
                throw new BadRequestException("Portal password must be at least 6 characters");
            }
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
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

        if (wantsPortal) {
            CreateUserRequest portalUser = new CreateUserRequest();
            portalUser.setUserType(CreateUserRequest.UserType.CUSTOMER);
            portalUser.setCustomerId(savedCustomer.getCustomerId());
            portalUser.setUsername(request.getPortalUsername().trim());
            portalUser.setPassword(request.getPortalPassword());
            portalUser.setFirstName(savedCustomer.getFirstName());
            portalUser.setLastName(savedCustomer.getLastName());
            portalUser.setEmail(savedCustomer.getEmail());
            userService.createUser(portalUser);
        }

        auditEventService.record(
                AuditCategory.CUSTOMER,
                AuditAction.CUSTOMER_CREATE,
                "CUSTOMER",
                String.valueOf(savedCustomer.getCustomerId()),
                "Customer created: " + savedCustomer.getFirstName() + " " + savedCustomer.getLastName(),
                "accountType=" + request.getAccountType() + ", portalLogin=" + wantsPortal,
                true
        );

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
    @Transactional
    public Customer updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        String email = request.getEmail().trim();
        String phoneNumber = request.getPhoneNumber().trim();

        if (customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(email, customerId)) {
            throw new ConflictException("Email already exists");
        }

        if (customerRepository.existsByPhoneNumberAndCustomerIdNot(phoneNumber, customerId)) {
            throw new ConflictException("Phone number already exists");
        }

        existingCustomer.setFirstName(request.getFirstName().trim());
        existingCustomer.setLastName(request.getLastName().trim());
        existingCustomer.setEmail(email);
        existingCustomer.setPhoneNumber(phoneNumber);
        existingCustomer.setDateOfBirth(request.getDateOfBirth());
        existingCustomer.setAddress(request.getAddress().trim());
        existingCustomer.setStatus(request.getStatus().trim().toUpperCase());
        existingCustomer.setTransferApprovalThreshold(request.getTransferApprovalThreshold());

        Customer saved = customerRepository.save(existingCustomer);
        auditEventService.record(
                AuditCategory.CUSTOMER,
                AuditAction.CUSTOMER_UPDATE,
                "CUSTOMER",
                String.valueOf(saved.getCustomerId()),
                "Customer updated: " + saved.getFirstName() + " " + saved.getLastName(),
                "status=" + saved.getStatus(),
                true
        );
        return saved;
    }

    @Override
    public void deleteCustomer(Long customerId) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        customerRepository.delete(existingCustomer);
    }
}
