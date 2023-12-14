/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import android.util.Log;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

/**
 * Validation rule for luhn check.
 */
public class ValidationRuleLuhn extends AbstractValidationRule {

	private static final long serialVersionUID = -6609650480352325271L;

	private static final String TAG = ValidationRuleLuhn.class.getName();

	/**
	 * @deprecated This constructor is for internal use only.
	 */
	@Deprecated
	public ValidationRuleLuhn() {
		super("luhn", ValidationType.LUHN);
	}

	/**
	 * @deprecated In a future release, this constructor will be removed.
	 */
	@Deprecated
	public ValidationRuleLuhn(String errorMessage, ValidationType type) {
		super(errorMessage, type);
	}

	/**
	 * Validates that the Credit Card number passes the Luhn test.
	 *
	 * @param text the value for which it should be checked if it passes the Luhn test, as a String
	 *
	 * @return true, if the value passes the Luhn check; false otherwise
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

		text = text.replaceAll(" ", "");
		if (text.length() < 12) {
			return false;
		}

		int sum = 0;
        boolean alternate = false;

        for (int i = text.length() - 1; i >= 0; i--) {

        	int n = Character.digit(text.charAt(i), 10);
        	if (n == -1) {
        		// not a valid number
        		return false;
        	}

        	if (alternate) {
        		n *= 2;

        		if (n > 9) {
        			n = (n % 10) + 1;
        		}
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
	}

	/**
	 * Validates that the value in the field with fieldId passes the Luhn check.
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} that will be used for doing a payment
	 * @param fieldId the ID of the field to which to apply the current validator
	 *
	 * @return true, if the value in the field with fieldId passes the Luhn check; false, if it doesn't or if the fieldId could not be found
	 */
	@Override
	public boolean validate(PaymentRequest paymentRequest, String fieldId) {

		String text = paymentRequest.getValue(fieldId);

		if (text == null) {
			return false;
		}

		text = text.replaceAll(" ", "");
		if (text.length() < 12) {
			return false;
		}

		int sum = 0;
		boolean alternate = false;

		for (int i = text.length() - 1; i >= 0; i--) {

			int n = Character.digit(text.charAt(i), 10);
			if (n == -1) {
				// not a valid number
				return false;
			}

			if (alternate) {
				n *= 2;

				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}

		return (sum % 10 == 0);
	}
}
