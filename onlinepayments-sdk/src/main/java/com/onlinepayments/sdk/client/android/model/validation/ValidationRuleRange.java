/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import android.util.Log;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

import java.security.InvalidParameterException;

/**
 * Validation rule for range.
 */
public class ValidationRuleRange extends AbstractValidationRule {

	private static final long serialVersionUID = 1199939638104378041L;

	private static final String TAG = ValidationRuleRange.class.getName();

	private Integer minValue;
	private Integer maxValue;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public ValidationRuleRange(Integer minValue, Integer maxValue, String errorMessage, ValidationType type) {
		super(errorMessage, type);

		if (minValue == null) {
			throw new InvalidParameterException("Error initialising ValidationRuleRange, minValue may not be null");
		}

		if (maxValue == null) {
			throw new InvalidParameterException("Error initialising ValidationRuleRange, maxValue may not be null");
		}

		this.minValue = minValue;
		this.maxValue = maxValue;
	}


	/**
	 * Validates that the text value is of the correct amount.
	 *
	 * @param text the value for which it should be checked if it is the correct amount, as a String
	 *
	 * @return true, if the value has the correct amount; false otherwise
	 *
	 * @deprecated use {@link #validate(PaymentRequest, String)} instead.
	 */
	@Override
	@Deprecated
	public boolean validate(String text) {
		Log.w(TAG, "This method is deprecated and should not be used! Use <validate(PaymentRequest paymentRequest, String)> instead.");

		if (text == null) {
			return false;
		}

		try {
			Integer enteredValue = Integer.parseInt(text);
			return enteredValue > minValue && enteredValue < maxValue;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Validates that the value in the field with fieldId has a value within the set bounds.
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} that will be used for doing a payment
	 * @param fieldId the ID of the field to which to apply the current validator
	 *
	 * @return true, if the value in the field with fieldId is in the correct range; false, if it is out of bounds or if the fieldId could not be found
	 */
	@Override
	public boolean validate(PaymentRequest paymentRequest, String fieldId) {

		String text = paymentRequest.getValue(fieldId);

		if (text == null) {
			return false;
		}

		text = paymentRequest.getUnmaskedValue(fieldId, text);

		try {
			Integer enteredValue = Integer.parseInt(text);
			return enteredValue > minValue && enteredValue < maxValue;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public Integer getMinValue() {
		return minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

}
