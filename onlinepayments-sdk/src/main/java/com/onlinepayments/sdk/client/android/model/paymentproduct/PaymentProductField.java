/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.onlinepayments.sdk.client.android.formatter.StringFormatter;
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields;
import com.onlinepayments.sdk.client.android.model.FormatResult;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.validation.AbstractValidationRule;
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a PaymentProductField object.
 */
public class PaymentProductField implements Serializable {

	private static final long serialVersionUID = 7731107337899853223L;

	public enum Type {

		@SerializedName("string")
		STRING,

		@SerializedName("integer")
		INTEGER,

		@SerializedName("numericstring")
		NUMERICSTRING,

		@SerializedName("expirydate")
		EXPIRYDATE,

		@SerializedName("boolean")
		BOOLEAN,

		@SerializedName("date")
		DATE,


	}


	// Id of this field
	private String id;

	// Type of this field for Online Payments gateway
	private Type type;

	// Contains hints for rendering this field
	private DisplayHintsProductFields displayHints = new DisplayHintsProductFields();

	// Contains contraints for this field
	private DataRestrictions dataRestrictions = new DataRestrictions();

	// Used for masking fields
	private StringFormatter formatter = new StringFormatter();

	// List of all invalid field errormessages
	private List<ValidationErrorMessage> errorMessageIds = new ArrayList<>();

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public PaymentProductField() {}

	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public DisplayHintsProductFields getDisplayHints() {
		return displayHints;
	}

	public DataRestrictions getDataRestrictions() {
		return dataRestrictions;
	}

	private Boolean valueNullOrEmpty(String value){
		if (value == null){
			return true;
		}
		return value.isEmpty();
	}


	/**
	 * Gets all error message codes for the supplied value.
	 * This list is filled after doing isValid() on this field.
	 *
	 * @param value the value that should be validated
	 *
	 * @return a list of error messages that apply to this field. If the list is empty you can assume that the field value is a valid value
	 *
	 * @deprecated use {@link #validateValue(PaymentRequest)} instead
	 */
	@Deprecated
	public List<ValidationErrorMessage> validateValue(String value) {

		// Remove possible existing errors first
		errorMessageIds.clear();

		// check required first
		if (dataRestrictions.isRequired() && valueNullOrEmpty(value)) {

			// If field is required, but has no value, add to the the errormessage list
			errorMessageIds.add(new ValidationErrorMessage("required", id, null));
		} else {

			if (!valueNullOrEmpty(value)) {
				for (AbstractValidationRule rule : dataRestrictions.getValidationRules()) {
						if (!rule.validate(value)) {

						// If an invalid fieldvalue is found, add to the the errormessage list
						errorMessageIds.add(new ValidationErrorMessage(rule.getMessageId(), id, rule));
					}
				}
			}
		}

		return errorMessageIds;
	}

	/**
	 * Gets all error message codes for the {@link PaymentRequest}'s value.
	 * This list is filled after doing isValid() on this field.
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} which holds all the values that the payment will be made with
	 *
	 * @return a list of error messages that apply to this field. If the list is empty you can assume that the field value is a valid value
	 */
	public List<ValidationErrorMessage> validateValue(PaymentRequest paymentRequest) {

		// Get the value from the paymentRequest
		String value = paymentRequest.getValue(id);

		// Remove possible existing errors
		errorMessageIds.clear();

		// check required first
		if (dataRestrictions.isRequired() && valueNullOrEmpty(value)) {

			// If field is required, but has no value, add to the the errormessage list
			errorMessageIds.add(new ValidationErrorMessage("required", id, null));
		} else {
			for (AbstractValidationRule rule : dataRestrictions.getValidationRules()) {
				if (!rule.validate(paymentRequest, id)) {

					// If an invalid fieldvalue is found, add to the errormessage list
					errorMessageIds.add(new ValidationErrorMessage(rule.getMessageId(), id, rule));
				}
			}
		}

		return errorMessageIds;
	}

	/**
	 * Applies a mask to a String, based on the previous value and splice information.
	 * The result is a FormatResult object, that holds the masked String and the new cursor index.
	 * This masker is meant for user input fields, where users are busy entering their information.
	 *
	 * @param newValue the value that the mask will be applied to
	 * @param oldValue the value that was in the edit text, before characters were removed or added
	 * @param start the index of the start of the change
	 * @param count the number of characters that were removed
	 * @param after the number of characters that were added
	 *
	 * @return {@link FormatResult} that contains the masked String and the new cursor index
	 */
	public FormatResult applyMask(String newValue, String oldValue, int start, int count, int after) {
		String mask = displayHints.getMask();
		if (mask == null) {
			return new FormatResult(newValue, (start - count) + after);
		}
		return formatter.applyMask(mask, newValue, oldValue, start, count, after);
	}

	/**
	 * Applies mask on a given String and calculates the new cursor index for the given newValue and oldValue.
	 *
	 * @param newValue the value that the mask will be applied to
	 * @param oldValue the value that was in the edit text, before characters were removed or added
	 * @param cursorIndex the current cursor index
	 *
	 * @return {@link FormatResult} that contains the masked String and the new cursor index
	 */
	public FormatResult applyMask(String newValue, String oldValue, Integer cursorIndex) {
		String mask = displayHints.getMask();
		if (mask == null) {
			return new FormatResult(newValue, cursorIndex);
		}
		return formatter.applyMask(mask, newValue, oldValue, cursorIndex);
	}

	/**
	 * Applies mask on a given String.
	 *
	 * @param value the String that the mask will be applied to
	 *
	 * @return the masked String value
	 */
	public String applyMask(String value){
		String mask = displayHints.getMask();
		if (mask == null) {
			return value;
		}
		return formatter.applyMask(mask, value);
	}

	/**
	 * Removes mask on a given String.
	 *
	 * @param value the value of which the mask will be removed
	 *
	 * @return the unmasked String value
	 */
	public String removeMask(String value){
		String mask = displayHints.getMask();
		if (mask == null) {
			return value;
		}
		return formatter.removeMask(mask, value);
	}

}
