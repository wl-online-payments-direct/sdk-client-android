/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * POJO which holds the PaymentProductCachkey data.
 * It's used to determine if a PaymentProduct should be retrieved from the Online Payments platform, or retrieved from the memory cache.
 *
 * @deprecated In a future release, this class and its functions will become internal to the SDK.
 */
@Deprecated
public class PaymentItemCacheKey implements Serializable {

	private static final long serialVersionUID = 930873231953051398L;

	Long amount;
	String countryCode;
	String currencyCode;
	boolean isRecurring;
	String paymentProductId;

	/**
	 * @deprecated use {@link #PaymentItemCacheKey(Long, String, String, boolean, String)} instead.
	 */
	@Deprecated
	public PaymentItemCacheKey(Long amount, CountryCode countryCode, CurrencyCode currencyCode, boolean isRecurring, String paymentProductId) {
		this(amount, countryCode.toString(), currencyCode.toString(), isRecurring, paymentProductId);
	}

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public PaymentItemCacheKey(Long amount, String countryCode, String currencyCode, boolean isRecurring, String paymentProductId) {

		if (amount == null) {
			throw new InvalidParameterException("Error creating PaymentItemCacheKey, amount may not be null");
		}
		if (countryCode == null) {
			throw new InvalidParameterException("Error creating PaymentItemCacheKey, countryCode may not be null");
		}
		if (currencyCode == null) {
			throw new InvalidParameterException("Error creating PaymentItemCacheKey, currencyCode may not be null");
		}
		if (paymentProductId == null) {
			throw new InvalidParameterException("Error creating PaymentItemCacheKey, paymentProductId may not be null");
		}

		this.amount = amount;
		this.countryCode = countryCode;
		this.currencyCode = currencyCode;
		this.isRecurring = isRecurring;
		this.paymentProductId = paymentProductId;
	}


	public Long getAmount() {
		return amount;
	}

	/**
	 * Note that 'null' will be returned when an unknown String value was set.
	 *
	 * @deprecated In the next major release, the type of countryCode will change to String. Use {@link #getCountryCodeString()} instead.
	 */
	@Deprecated
	public CountryCode getCountryCode() {
		try {
			return CountryCode.valueOf(countryCode);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getCountryCodeString() {
		return countryCode;
	}

	/**
	 * Note that 'null' will be returned when an unknown String value was set.
	 *
	 * @deprecated In the next major release, the type of currencyCode will change to String. Use {@link #getCurrencyCodeString()} instead.
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

	public String getPaymentProductId() {
		return paymentProductId;
	}

	public boolean getIsRecurring() {
		return isRecurring;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (o == null || o.getClass() != getClass()) {
			return false;
		}

		PaymentItemCacheKey otherKey = (PaymentItemCacheKey)o;
		return otherKey.getAmount().equals(amount) &&
			   otherKey.getCountryCodeString().equals(countryCode) &&
			   otherKey.getCurrencyCodeString().equals(currencyCode) &&
			   otherKey.getPaymentProductId().equals(paymentProductId) &&
			   otherKey.getIsRecurring() == isRecurring;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + amount.hashCode();
		hash = 31 * hash + countryCode.hashCode();
		hash = 31 * hash + currencyCode.hashCode();
		hash = 31 * hash + paymentProductId.hashCode();
		hash = 31 * hash + Boolean.valueOf(isRecurring).hashCode();
		return hash;
	}
}
