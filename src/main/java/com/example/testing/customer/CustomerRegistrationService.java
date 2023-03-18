package com.example.testing.customer;

import org.springframework.beans.factory.annotation.Autowired;

public class CustomerRegistrationService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerRegistrationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
