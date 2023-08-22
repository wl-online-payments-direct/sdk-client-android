/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Contains error message information for a specific field.
 */
public class ValidationErrorMessage implements Serializable {

	private static final long serialVersionUID = 5842038484067693459L;

	private String errorMessage;
	private String paymentProductFieldId;
	private ValidationRule rule;

	public ValidationErrorMessage(String errorMessage, String paymentProductFieldId, ValidationRule rule) {

		if (errorMessage == null) {
			throw new InvalidParameterException("Error creating ValidationErrorMessage, errorMessage may not be null");
		}
		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error creating ValidationErrorMessage, paymentProductFieldId may not be null");
		}

		this.errorMessage = errorMessage;
		this.paymentProductFieldId = paymentProductFieldId;
		this.rule = rule;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public String getPaymentProductFieldId() {
		return paymentProductFieldId;
	}

	public ValidationRule getRule() {
		return rule;
	}
}
