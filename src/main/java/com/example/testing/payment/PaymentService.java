package com.example.testing.payment;

import com.example.testing.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;
    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.GBP);

    @Autowired
    public PaymentService(CustomerRepository customerRepository, PaymentRepository paymentRepository, CardPaymentCharger cardPaymentCharger) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    void chargeCard(UUID customerId, PaymentRequest request){

        boolean isCustomerFound = customerRepository.findById(customerId).isPresent();
        if(!isCustomerFound){
            throw new IllegalStateException(String.format("Customer with id [%s] not found",customerId));
        }

        boolean isCurrencySupport = ACCEPTED_CURRENCIES.contains(request.getPayment().getCurrency());
        if (!isCurrencySupport){
            String message = String.format(
                    "Currency [%s] not supported",
                    request.getPayment().getCurrency());
            throw new IllegalStateException(message);
        }

        CardPaymentCharge cardPaymentCharge = cardPaymentCharger.chargeCard(
                request.getPayment().getSource(),
                request.getPayment().getAmount(),
                request.getPayment().getCurrency(),
                request.getPayment().getDescription()
        );

        if (!cardPaymentCharge.isCardDebited()) {
            throw new IllegalStateException(String.format("Card not debited for customer %s", customerId));
        }

        request.getPayment().setCustomerId(customerId);

        paymentRepository.save(request.getPayment());

    }
}
