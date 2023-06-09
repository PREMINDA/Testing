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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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

    @Test
    void itShouldNotSaveCustomerWhenCustomerExists() {
        //Given a phone number and customer
        UUID id = UUID.randomUUID();
        String phoneNumber = "00009999";
        Customer customer = new Customer(id,"Alina", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... ... an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customer));

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        //Given
        String phoneNumber = "00009999";
        Customer customer = new Customer(null,"Alina", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... No customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        //When
        underTest.registerNewCustomer(request);
        //Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue= customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customer);
        assertThat(customerArgumentCaptorValue.getId()).isNotNull();

    }

    @Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        //Given
        UUID id = UUID.randomUUID();
        String phoneNumber = "00009999";
        Customer customerOne = new Customer(id,"Alina", phoneNumber);
        Customer customerTwo = new Customer(id,"mari", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customerTwo);

        // ... ... an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customerOne));

        //When
        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .hasMessageContaining(String.format("phone number [%s] is taken", phoneNumber))
                .isInstanceOf(IllegalStateException.class);



        then(customerRepository).should(never()).save(any(Customer.class));
    }
}