package com.example.testing.payment;

import java.math.BigDecimal;

public class PaymentRequest {
    private final Payment payment;

    public PaymentRequest(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "payment=" + payment +
                '}';
    }

}
