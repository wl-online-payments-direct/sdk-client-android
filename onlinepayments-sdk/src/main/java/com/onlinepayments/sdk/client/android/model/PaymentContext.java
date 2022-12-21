package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;


/**
 * Pojo that contains PaymentContext information
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class PaymentContext implements Serializable {

    private static final long serialVersionUID = -4845945197600321181L;

    private AmountOfMoney amountOfMoney;
    private String countryCode;
    private boolean isRecurring;
    private Boolean forceBasicFlow;

    public PaymentContext() {}

    /**
     * @deprecated use {@link #PaymentContext(AmountOfMoney, String, boolean)} instead.
     */
    @Deprecated
    public PaymentContext(AmountOfMoney amountOfMoney, CountryCode countryCode, boolean isRecurring) {
        this(amountOfMoney, countryCode.toString(), isRecurring);
    }

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

    /**
     * @deprecated In the next major release, the type of countryCode will change to String.
     * Note that 'null' will be returned when an unknown String value was set.
     * Use {@link #getCountryCodeString()} instead.
     */
    @Deprecated
    public CountryCode getCountryCode() {
        try {
            return CountryCode.valueOf(countryCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @deprecated use {@link #setCountryCode(String)} instead.
     */
    @Deprecated
    public void setCountryCode(CountryCode countryCode) {
        this.countryCode = countryCode.name();
    }

    public String getCountryCodeString() {
        return countryCode;
    }

    /**
     * @param countryCode The Country Code of the Country where the transaction will take place. The
     *     provided code should match the ISO-3166-alpha-2 standard.
     *     @see <a href="https://www.iso.org/iso-3166-country-codes.html">ISO 3166 Country Codes</a>
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

    public Boolean isForceBasicFlow() {
        return forceBasicFlow;
    }
    public void setForceBasicFlow(Boolean forceBasicFlow) {
        this.forceBasicFlow = forceBasicFlow;
    }
}
