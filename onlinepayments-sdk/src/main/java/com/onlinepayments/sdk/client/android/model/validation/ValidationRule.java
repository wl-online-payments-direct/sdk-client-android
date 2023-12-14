/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

/**
 * Interface for ValidationRule.
 */
public interface ValidationRule {

	/**
	 * Validate method which validates a text.
	 *
	 * @param value the text to be validated
	 *
	 * @return true, if the text is valid; false, if the text is invalid
	 *
	 */
    boolean validate(String value);

	boolean validate(PaymentRequest paymentRequest, String fieldId);

}
