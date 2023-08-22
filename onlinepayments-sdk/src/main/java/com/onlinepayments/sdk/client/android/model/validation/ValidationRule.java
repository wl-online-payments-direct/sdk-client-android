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
	 * @param text the text to be validated
	 *
	 * @return true, if the text is valid; false, if the text is invalid
	 *
	 * @deprecated use {@link #validate(PaymentRequest, String)} instead.
	 */
	@Deprecated
    boolean validate(String text);

	boolean validate(PaymentRequest paymentRequest, String fieldId);

}
