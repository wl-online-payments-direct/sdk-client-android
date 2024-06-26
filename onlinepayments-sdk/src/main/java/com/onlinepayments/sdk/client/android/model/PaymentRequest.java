/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile;
import com.onlinepayments.sdk.client.android.model.paymentproduct.KeyValuePair;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage;

/**
 * Contains all payment request data needed for doing a payment.
 */
public class PaymentRequest implements Serializable {

	private static final long serialVersionUID = 1553481971640554760L;

	// Paymentproduct which the customer is using
	private PaymentProduct paymentProduct;

	// Account On File which the customer has selected
	private AccountOnFile accountOnFile;

	// All field values which the customer has entered
	private Map<String, String> fieldValues = new HashMap<>();

	// All validation errormessages for the entered values
	private List<ValidationErrorMessage> errorMessageIds = new ArrayList<>();

	// Used for storing account on file (true is storing)
	private Boolean tokenize;

	/**
	 * Get the value of tokenize.
	 *
	 * @return a Boolean indicating whether account on file should be stored
	 */
	public Boolean getTokenize(){
		return tokenize;
	}

	/**
	 * Set the value of tokenize.
	 *
	 * @param tokenize the new value of tokenize
	 */
	public void setTokenize(Boolean tokenize){
		this.tokenize = tokenize;
	}

	public PaymentRequest() {
		this(null, null, false);
	}

	public PaymentRequest(PaymentProduct paymentProduct) {
		this(paymentProduct, null, false);
	}

	public PaymentRequest(PaymentProduct paymentProduct, AccountOnFile accountOnFile) {
		this(paymentProduct, accountOnFile, false);
	}

	public PaymentRequest(PaymentProduct paymentProduct, boolean tokenize) {
		this(paymentProduct, null, tokenize);
	}

	public PaymentRequest(PaymentProduct paymentProduct, AccountOnFile accountOnFile, boolean tokenize) {
		this.paymentProduct = paymentProduct;
		this.accountOnFile = accountOnFile;
		this.tokenize = tokenize;
	}


	/**
	 * Validates all fields based on their value and their validation rules.
	 * If a field is prefilled from the {@link AccountOnFile}, but it has been altered, it will be validated.
	 *
	 * @return list of errorMessageIds
	 */
	public List<ValidationErrorMessage> validate() {

		errorMessageIds.clear();

		if (paymentProduct == null) {
			throw new NullPointerException("Error validating PaymentRequest, please set a paymentProduct first.");
		}

		// Loop trough all validationrules of all fields on the paymentProduct
		for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {

			// Validate the field with its value
			if (!isFieldInAccountOnFileAndNotAltered(field)) {
				errorMessageIds.addAll(field.validateValue(this));
			}
		}
		return errorMessageIds;
	}

	private boolean isFieldInAccountOnFileAndNotAltered(PaymentProductField field) {
		if (accountOnFile != null && paymentProductHasAccountOnFile()) {
			for (KeyValuePair pair : accountOnFile.getAttributes()) {
				if (pair.getKey().equals(field.getId()) &&               // Field is in account on file
						(!pair.isEditingAllowed() ||                     // Not altered
								(getValue(field.getId()) == null))) {    // Not altered; Unaltered values should not be in the request
					return true;
				}
			}
		}
		return false;
	}

	private boolean paymentProductHasAccountOnFile() {
		if (paymentProduct != null) {
			for (AccountOnFile ppAccountOnFile : paymentProduct.getAccountsOnFile()) {
				if (accountOnFile.getId().equals(ppAccountOnFile.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Add value to the paymentProductFields map.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} for which the value will be set
	 * @param value the value to set for the corresponding {@link PaymentProductField}
	 */
	public void setValue(String paymentProductFieldId, String value) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error setting value on PaymentRequest, paymentProductFieldId may not be null");
		}
		if (value == null) {
			throw new InvalidParameterException("Error setting value on PaymentRequest, value may not be null");
		}

		fieldValues.remove(paymentProductFieldId);
		fieldValues.put(paymentProductFieldId, value);
	}


	/**
	 * Gets the value of given paymentProductFieldId.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} the value should be retrieved from
	 *
	 * @return the value of the {@link PaymentProductField}
	 */
	public String getValue(String paymentProductFieldId) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error getting value from PaymentRequest, paymentProductFieldId may not be null");
		}
		return fieldValues.get(paymentProductFieldId);
	}

	/**
	 * Removes the value of given paymentProductFieldId
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} the value should be removed from
     */
	public void removeValue(String paymentProductFieldId) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error removing value from PaymentRequest, paymentProductFieldId may not be null");
		}
		fieldValues.remove(paymentProductFieldId);
	}

	private PaymentProductField getPaymentProductField(String paymentProductFieldId) {
		// Loop through all fields to get the corresponding payment product field
		for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {
			if (field.getId().equals(paymentProductFieldId)) {
				return field;
			}
		}

		return null;
	}

	/**
	 * Gets the map with all field values.
	 *
	 * @return a Map of the {@link PaymentProductField} id and their corresponding values
	 */
	public Map<String, String> getValues() {
		return fieldValues;
	}

	/**
	 * Gets masked value for the given newValue and oldValue with the mask of the {@link PaymentProductField} with the corresponding paymentProductFieldId.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} whose mask is used
	 * @param newValue the value which is masked
	 * @param oldValue the previous value
	 * @param cursorIndex the current index of the cursor
	 *
	 * @return FormatResult which contains maskedValue and cursorIndex, or null if there is no {@link PaymentProductField} found
	 */
	public FormatResult getMaskedValue(String paymentProductFieldId, String newValue, String oldValue, Integer cursorIndex) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error getting masked value from PaymentRequest, paymentProductFieldId may not be null");
		}
		if (newValue == null) {
			throw new InvalidParameterException("Error getting masked value from PaymentRequest, newValue may not be null");
		}
		if (oldValue == null) {
			throw new InvalidParameterException("Error getting masked value from PaymentRequest, oldValue may not be null");
		}

		PaymentProductField field = getPaymentProductField(paymentProductFieldId);
		if (field != null) {
			return field.applyMask(newValue, oldValue, cursorIndex);
		}

		return null;
	}

	/**
	 * Returns the masked value for the given payment product field id.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} whose masked value should be returned
	 *
	 * @return String which is the masked value of the provided payment product field.
	 */
	public String getMaskedValue(String paymentProductFieldId) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error getting masked value from PaymentRequest, paymentProductFieldId may not be null");
		}


		PaymentProductField field = getPaymentProductField(paymentProductFieldId);
		if (field != null) {
			String value = getValue(paymentProductFieldId);
			return field.applyMask(value);
		}

		return null;
	}

	/**
	 * Removes the mask of a given value.
	 *
	 * @param paymentProductFieldId the ID of the {@link PaymentProductField} that the value belongs to
	 * @param value the value that will be unmasked
	 *
	 * @return String with unmaskedValue, or null if there is no {@link PaymentProductField} found
	 */
	public String getUnmaskedValue(String paymentProductFieldId, String value) {
		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error getting unmasked value from PaymentRequest, paymentProductFieldId may not be null");
		}
		if (value == null) {
			throw new InvalidParameterException("Error getting unmasked value from PaymentRequest, value may not be null");
		}

		PaymentProductField field = getPaymentProductField(paymentProductFieldId);
		if (field != null) {
			return field.removeMask(value);
		}

		return null;
	}

	/**
	 * Returns the unmasked value for the given payment product field id.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} whose unmasked value should be returned
	 *
	 * @return String which is the unmasked value of the provided payment product field.
	 */
	public String getUnmaskedValue(String paymentProductFieldId) {

		if (paymentProductFieldId == null) {
			throw new InvalidParameterException("Error getting unmasked value from PaymentRequest, paymentProductFieldId may not be null");
		}

		PaymentProductField field = getPaymentProductField(paymentProductFieldId);
		if (field != null) {
			String value = getValue(paymentProductFieldId);
			return field.removeMask(value);
		}

		return null;
	}

	/**
	 * Gets the map with all unmasked field values.
	 *
	 * @return a Map of the {@link PaymentProductField} id and the corresponding unmasked value
	 */
	public Map<String, String> getUnmaskedValues(){
		Map<String, String> unMaskedFieldValues = new HashMap<>();

		// Loop through all the fieldValues
		for (Entry<String, String> entry : fieldValues.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();

			// Loop through all fields and format the matching field value.
			for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {
				if (field.getId().equals(key)) {
					// retrieve value from fieldFormatter
					value = field.removeMask(value);

					// put the value in the new hashmap
					unMaskedFieldValues.put(key, value);

					break;
				}
			}
		}
		return unMaskedFieldValues;
	}

	/**
	 * Gets the map with all masked field values.
	 *
	 * @return a Map of the {@link PaymentProductField} id and the corresponding masked value
	 */
	public Map<String, String> getMaskedValues(){
		Map<String, String> maskedFieldValues = new HashMap<>();
		// Loop through all the fieldValues
		for (Entry<String, String> entry : fieldValues.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// Loop through all fields and format the matching field value.
			for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {
				if (field.getId().equals(key)) {
					// retrieve value from fieldFormatter
					value = field.applyMask(value);

					// put the value in the new hashmap
					maskedFieldValues.put(key, value);
					break;
				}
			}
		}
		return maskedFieldValues;
	}

	/**
	 * Merges existing field values map with the current {@link PaymentProduct} values.
	 *
	 * @param paymentProduct the {@link PaymentProduct} for which the field values should be merged with fieldValues
	 * @deprecated This method will be removed from the SDK in a later release.
	 */
	@Deprecated
	public void mergePaymentRequest(PaymentProduct paymentProduct) {

		if (paymentProduct == null) {
			throw new InvalidParameterException("Error merging PaymentRequest, paymentProduct may not be null");
		}

		// Create new map which contains all values for fields who are also present in the new paymentproduct
		Map<String, String> newFieldValues = new HashMap<>();

		// Loop trough all new fields and see of they match the fieldvalues id
		if (paymentProduct.getPaymentProductFields() != null) {
			for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {

				for (Entry<String, String> valueEntry : fieldValues.entrySet()) {

					if (field.getId().equals(valueEntry.getKey())) {
						newFieldValues.put(valueEntry.getKey(), valueEntry.getValue());
					}
				}
			}
		}

		fieldValues = newFieldValues;
	}

	/**
	 * Sets the {@link PaymentProduct} for which the customer is going to do a payment.
	 *
	 * @param paymentProduct the {@link PaymentProduct} for which the customer is going to do a payment
	 */
	public void setPaymentProduct(PaymentProduct paymentProduct) {

		if (paymentProduct == null) {
			throw new InvalidParameterException("Error setting paymentproduct, paymentProduct may not be null");
		}
		this.paymentProduct = paymentProduct;
	}

	/**
	 * Sets which {@link AccountOnFile} is selected on the {@link PaymentProduct} selection page.
	 *
	 * @param accountOnFile the selected {@link AccountOnFile}
	 */
	public void setAccountOnFile(AccountOnFile accountOnFile) {

		if (accountOnFile == null) {
			throw new InvalidParameterException("Error setting accountOnFile, accountOnFile may not be null");
		}
		this.accountOnFile = accountOnFile;
	}

	public void removeAccountOnFile(){
		this.accountOnFile = null;
	}

	public PaymentProduct getPaymentProduct() {
		return paymentProduct;
	}

	public AccountOnFile getAccountOnFile() {
		return accountOnFile;
	}
}
