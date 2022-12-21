package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;


/**
 * Pojo that contains money information for a payment.
 *
 * Copyright 2020 Global Collect Services B.V
 */
public class AmountOfMoney implements Serializable{

    private static final long serialVersionUID = 3077405745639575054L;

    private Long amount = 0L;
    private String currencyCode;

    public AmountOfMoney() {}

    /**
     * @deprecated use {@link #AmountOfMoney(Long, String)} instead.
     */
    @Deprecated
    public AmountOfMoney(Long amount, CurrencyCode currencyCode) {
        this(amount, currencyCode.toString());
    }

    /**
     * @param amount The amount, in the smallest possible denominator of the provided currency.
     * @param currencyCode The ISO-4217 Currency Code.
     *     @see <a href="https://www.iso.org/iso-4217-currency-codes.html">ISO 4217 Currency Codes</a>
     */
    public AmountOfMoney(Long amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public Long getAmount() {
        return amount;
    }

    /**
     * @deprecated In the next major release, the type of currencyCode will change to String.
     * Note that 'null' will be returned when an unknown String value was set.
     * Use {@link #getCurrencyCodeString()} instead.
     */
    @Deprecated
    public CurrencyCode getCurrencyCode() {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getCurrencyCodeString() {
        return currencyCode;
    }
}
