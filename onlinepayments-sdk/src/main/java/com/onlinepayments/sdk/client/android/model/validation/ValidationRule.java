package com.onlinepayments.sdk.client.android.model.validation;


import com.onlinepayments.sdk.client.android.model.PaymentRequest;

/**
 * Interface for ValidationRule
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public interface ValidationRule {

	/**
	 * Validate method which validates a text
	 * @param text, the text to be validated
	 * @return true when the text is valid, false it's invalid
	 * @deprecated use {@link #validate(PaymentRequest, String)} instead
	 */
	@Deprecated
    boolean validate(String text);

	boolean validate(PaymentRequest paymentRequest, String fieldId);

}
