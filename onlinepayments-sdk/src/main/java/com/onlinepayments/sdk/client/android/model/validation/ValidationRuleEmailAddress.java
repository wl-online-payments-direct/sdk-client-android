/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import android.util.Log;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

import java.security.InvalidParameterException;

/**
 * Validation rule for email address.
 */
public class ValidationRuleEmailAddress extends AbstractValidationRule {

	private static final long serialVersionUID = -2476401279131525956L;

	private static final String TAG = ValidationRuleEmailAddress.class.getName();

	private static final String EMAIL_REGEX = "[^@\\.]+(\\.[^@\\.]+)*@([^@\\.]+\\.)*[^@\\.]+\\.[^@\\.][^@\\.]+";


	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public ValidationRuleEmailAddress(String errorMessage, ValidationType type) {
		super(errorMessage, type);
	}

	/**
	 * Validates an email address.
	 *
	 * @param text the email address to be validated, as a String
	 *
	 * @return whether the email address is valid or not
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

		// Check whether text matches the regex for email addresses
		return text.matches(EMAIL_REGEX);
	}

	/**
	 * Validates an email address
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} that will be used for doing a payment
	 * @param fieldId the ID of the field to which to apply the current validator
	 *
	 * @return true, if the value in the field with fieldId is a valid e-mail address; false, if it is not a valid email address or if the fieldId could not be found
     */
	@Override
	public boolean validate(PaymentRequest paymentRequest, String fieldId) {
		if (paymentRequest == null) {
			throw new InvalidParameterException("Error validating, paymentRequest may not be null");
		}
		if (fieldId == null) {
			throw new InvalidParameterException("Error validating, fieldId may not be null");
		}

		String text = paymentRequest.getValue(fieldId);

		if (text == null) {
			return false;
		}

		text = paymentRequest.getUnmaskedValue(fieldId, text);

		// Check whether text matches the regex for email addresses
		return text.matches(EMAIL_REGEX);
	}
}
