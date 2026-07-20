package com.bankone.customer;

import com.bankone.common.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @GetMapping("/customers/{id}")
    public String getCustomer(@PathVariable Long id) {

        throw new ResourceNotFoundException(
                "Customer with ID " + id + " not found."
        );
    }
}