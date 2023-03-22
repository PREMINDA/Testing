package com.example.testing.payment;

public class CardPaymentCharge {
    private final boolean isCardDebited;

    public CardPaymentCharge(boolean wasCardDebited) {
        this.isCardDebited = wasCardDebited;
    }

    public boolean isCardDebited() {
        return isCardDebited;
    }

    @Override
    public String toString() {
        return "CardPaymentCharge{" +
                "wasCardDebited=" + isCardDebited +
                '}';
    }
}
