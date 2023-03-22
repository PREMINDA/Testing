package com.example.testing.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPamentImpl implements CardPaymentCharger{
    @Override
    public CardPaymentCharge chargeCard(String cardSource, BigDecimal amount, Currency currency, String description) {
        return new CardPaymentCharge(true);
    }
}
