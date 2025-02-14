package com.asu.EduMentor.controller.rest.paymentProcessor.strategy;

import com.asu.EduMentor.model.PaymentType;

public class DonationRequest {
    private Long amount;
    private String paymentType;
    private int donorId;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (currency.equals("USD") || currency.equals("EGP") || currency.equals("EUR") || currency.equals("GBP") || currency.equals("CAD")) {
            this.currency = currency;
        }else{
            throw new IllegalArgumentException("Currency not supported"
            );
        }
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String  paymentType) {
        if (paymentType.equals("Card") || paymentType.equals("Courier")) {
            this.paymentType = paymentType;
        }else{
            throw new IllegalArgumentException("Payment type must be (Card) or (Courier)" + paymentType
            );
        }
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
