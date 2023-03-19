package com.example.testing.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new CustomerRegistrationService(customerRepository);
    }

    @Test
    void itShouldSaveNewCustomer() {
        //Given a phone number and customer
        UUID id = UUID.randomUUID();
        String phoneNumber = "00009999";
        Customer customer = new Customer(id,"Alina", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... No customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue= customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue.toString())
                .isEqualTo(customer.toString());
    }
}