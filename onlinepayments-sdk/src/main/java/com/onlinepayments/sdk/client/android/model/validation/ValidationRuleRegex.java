/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import android.util.Log;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

import java.security.InvalidParameterException;

/**
 * Validation rule for regex.
 */
public class ValidationRuleRegex extends AbstractValidationRule {

	private static final long serialVersionUID = 5054525275294003657L;

	private static final String TAG = ValidationRuleRegex.class.getName();

	private String regex;

	/**
	 * @deprecated This constructor is for internal use only.
	 */
	@Deprecated
	public ValidationRuleRegex(String regex) {
		super("regularExpression", ValidationType.REGULAREXPRESSION);

		if (regex == null) {
			throw new InvalidParameterException("Error initialising ValidationRuleRegex, regex may not be null");
		}

		this.regex = regex;
	}

	/**
	 * @deprecated In a future release, this constructor will be removed.
	 */
	@Deprecated
	public ValidationRuleRegex(String regex, String errorMessage, ValidationType type) {
		super(errorMessage, type);

		if (regex == null) {
			throw new InvalidParameterException("Error initialising ValidationRuleRegex, regex may not be null");
		}

		this.regex = regex;
	}

	/**
	 * Validates if the text matches the regular expression.
	 *
	 * @param text the text which should be checked whether is matches the regular expression
	 *
	 * @return true, if the text matches the regex; false otherwise
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

		return text.matches(regex);
	}

	/**
	 * Validates that the value in the field with fieldId matches the regular expression of this validator.
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} that will be used for doing a payment
	 * @param fieldId the ID of the field to which to apply the current validator
	 *
	 * @return true, if the value in the field with fieldId matches the regex; false, if it doesn't or if the fieldId could not be found
	 */
	@Override
	public boolean validate(PaymentRequest paymentRequest, String fieldId) {

		String text = paymentRequest.getValue(fieldId);

		if (text == null) {
			return false;
		}

		text = paymentRequest.getUnmaskedValue(fieldId, text);

		return text.matches(regex);
	}

}
