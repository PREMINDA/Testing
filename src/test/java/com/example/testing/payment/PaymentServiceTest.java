package com.example.testing.payment;

import com.example.testing.customer.Customer;
import com.example.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private  CardPaymentCharger cardPaymentCharger;
    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.GBP);

    private PaymentService underTest;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        underTest = new PaymentService(customerRepository,paymentRepository,cardPaymentCharger);
    }

    @Test
    void itShouldCardChargeSuccessfully() {
        //Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... Card is charged successfully
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(true));

        // When
        underTest.chargeCard(customerId, paymentRequest);

        // Then
        ArgumentCaptor<Payment> paymentArgumentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();
        assertThat(paymentArgumentCaptorValue)
                .usingRecursiveComparison()
                .ignoringFields("customerId")
                .isEqualTo(paymentRequest.getPayment());

        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void itShouldNotMakePaymentIfCustomerNotExists() {

        //Given
        UUID customerId = UUID.randomUUID();

        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.chargeCard(customerId,new PaymentRequest(new Payment())))
                .hasMessageContaining(String.format("Customer with id [%s] not found",customerId))
                .isInstanceOf(IllegalStateException.class);
        // When
        // Then

        // ... No interactions with PaymentCharger not PaymentRepository
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotMakePaymentIfCurrencyNotSupport() {

        // Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Euros
        Currency currency = Currency.EUR;

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        currency,
                        "card123xx",
                        "Donation"
                )
        );

        //When
        assertThatThrownBy(() -> underTest.chargeCard(customerId,paymentRequest))
                .hasMessageContaining("Currency [" + currency + "] not supported")
                .isInstanceOf(IllegalStateException.class);
        //Then
        // ... No interactions with PaymentCharger not PaymentRepository
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Euros
        Currency currency = Currency.USD;

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        currency,
                        "card123xx",
                        "Donation"
                )
        );

        // ... Card is not charged successfully
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(false));
        //When
        assertThatThrownBy(()->underTest.chargeCard(customerId,paymentRequest))
                .hasMessageContaining(String.format("Card not debited for customer %s", customerId))
                .isInstanceOf(IllegalStateException.class);
        //Then
        then(paymentRepository).shouldHaveNoInteractions();
    }
}