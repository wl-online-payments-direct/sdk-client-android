/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;

/**
 * POJO that contains PaymentContext information.
 * It contains information about a payment, like its {@link AmountOfMoney} and countryCode.
 */
public class PaymentContext implements Serializable {

    private static final long serialVersionUID = -4845945197600321181L;

    private AmountOfMoney amountOfMoney;
    private String countryCode;
    private boolean isRecurring;

    public PaymentContext() {}

    public PaymentContext(AmountOfMoney amountOfMoney, String countryCode, boolean isRecurring) {
        this.countryCode = countryCode;
        this.isRecurring = isRecurring;
        this.amountOfMoney = amountOfMoney;
    }

    public AmountOfMoney getAmountOfMoney() {
        return amountOfMoney;
    }
    public void setAmountOfMoney(AmountOfMoney amountOfMoney) {
        this.amountOfMoney = amountOfMoney;
    }

    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @deprecated In a future release, this function will be removed. Use {@link #getCountryCode()} instead.
     */
    @Deprecated
    public String getCountryCodeString() {
        return countryCode;
    }

    /**
     * @param countryCode the Country Code of the Country where the transaction will take place. The provided code should match the ISO-3166-alpha-2 standard.
     * @see <a href="https://www.iso.org/iso-3166-country-codes.html">ISO 3166 Country Codes</a>
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean isRecurring() {
        return isRecurring;
    }
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
}
