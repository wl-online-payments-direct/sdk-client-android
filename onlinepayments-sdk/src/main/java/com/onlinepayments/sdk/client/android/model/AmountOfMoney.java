/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;

/**
 * POJO that contains money information for a payment.
 */
public class AmountOfMoney implements Serializable{

    private static final long serialVersionUID = 3077405745639575054L;

    private Long amount = 0L;
    private String currencyCode;

    public AmountOfMoney() {}

    /**
     * @param amount the amount in the smallest possible denominator of the provided currency
     * @param currencyCode the ISO-4217 Currency Code as a String
     * @see <a href="https://www.iso.org/iso-4217-currency-codes.html">ISO 4217 Currency Codes</a>
     */
    public AmountOfMoney(Long amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * @deprecated In a future release, this function will be removed. Use {@link #getCurrencyCode()} instead.
     */
    @Deprecated
    public String getCurrencyCodeString() {
        return currencyCode;
    }
}