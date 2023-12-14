/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.onlinepayments.sdk.client.android.model.paymentproduct.validation.Validator;
import com.onlinepayments.sdk.client.android.model.validation.AbstractValidationRule;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddress;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDate;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedList;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBAN;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhn;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRange;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegex;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleTermsAndConditions;
import com.onlinepayments.sdk.client.android.model.validation.ValidationType;


/**
 * POJO that represents an Data restrictions object.
 * The DataRestrictions are used for validating user input.
 */
public class DataRestrictions implements Serializable {

	private static final long serialVersionUID = -549503465906936684L;

	private Boolean isRequired;

	private List<AbstractValidationRule> validationRules = new ArrayList<>();

	private Validator validators;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public DataRestrictions() {}

	/**
	 * @deprecated In a future release, this getter will be removed. Use {@link #getValidationRules()} instead.
	 */
	@Deprecated
	public Validator getValidator(){
		return validators;
	}

	public void addValidationRule(AbstractValidationRule validationRule) {
		validationRules.add(validationRule);
	}

	public void setValidationRules() {
		validationRules.clear();

		if (validators.getExpirationDate() != null) {
			validationRules.add(new ValidationRuleExpirationDate());
		}

		if (validators.getFixedList() != null) {

			if (validators.getFixedList().getAllowedValues() != null) {
				AbstractValidationRule validationRule = new ValidationRuleFixedList(validators.getFixedList().getAllowedValues());
				validationRules.add(validationRule);
			}
		}

		if (validators.getIBAN() != null) {
			validationRules.add(new ValidationRuleIBAN());
		}

		if (validators.getLength() != null) {

			if (validators.getLength().getMinLength() != null && validators.getLength().getMaxLength() != null) {
				AbstractValidationRule validationRule = new ValidationRuleLength(validators.getLength().getMinLength(), validators.getLength().getMaxLength());
				validationRules.add(validationRule);
			}
		}

		if (validators.getLuhn() != null) {
			validationRules.add(new ValidationRuleLuhn());
		}


		if (validators.getRange() != null) {

			if (validators.getRange().getMinValue() != null && validators.getRange().getMaxValue() != null) {
				AbstractValidationRule validationRule = new ValidationRuleRange(validators.getRange().getMinValue(), validators.getRange().getMaxValue());
				validationRules.add(validationRule);
			}
		}

		if (validators.getTermsAndConditions() != null) {
			validationRules.add(new ValidationRuleTermsAndConditions());
		}

		if (validators.getRegularExpression() != null) {

			if (validators.getRegularExpression().getRegularExpression() != null) {
				AbstractValidationRule validationRule = new ValidationRuleRegex(validators.getRegularExpression().getRegularExpression());
				validationRules.add(validationRule);
			}
		}

		if (validators.getEmailAddress() != null) {
			validationRules.add(new ValidationRuleEmailAddress());
		}
	}

	public List<AbstractValidationRule> getValidationRules() {
		if (validationRules.isEmpty()) {
			setValidationRules();
		}

		return validationRules;
	}

	public Boolean isRequired() {
		return isRequired;
	}

}
