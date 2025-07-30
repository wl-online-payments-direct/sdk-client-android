/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct

import com.onlinepayments.sdk.client.android.model.paymentproduct.validation.Validator
import com.onlinepayments.sdk.client.android.model.validation.AbstractValidationRule
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddress
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDate
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedList
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBAN
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhn
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRange
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegex
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleTermsAndConditions
import java.io.Serializable
import java.util.ArrayList

/**
 * POJO that represents an Data restrictions object.
 * The DataRestrictions are used for validating user input.
 */
class DataRestrictions internal constructor() : Serializable {
    private val isRequired: Boolean? = null

    private val validationRules: MutableList<AbstractValidationRule> =
        ArrayList<AbstractValidationRule>()

    private val validators: Validator? = null

    @Suppress("Unused")
    fun addValidationRule(validationRule: AbstractValidationRule) {
        validationRules.add(validationRule)
    }

    fun setValidationRules() {
        validationRules.clear()

        if (validators!!.expirationDate != null) {
            validationRules.add(ValidationRuleExpirationDate())
        }

        if (validators.fixedList != null) {
            if (validators.fixedList.allowedValues != null) {
                val validationRule: AbstractValidationRule = ValidationRuleFixedList(validators.fixedList.allowedValues)
                validationRules.add(validationRule)
            }
        }

        if (validators.iban != null) {
            validationRules.add(ValidationRuleIBAN())
        }

        if (validators.length != null) {
            val length = validators.length
            if (length.minLength != null && length.maxLength != null) {
                val validationRule: AbstractValidationRule = ValidationRuleLength(length.minLength, length.maxLength)
                validationRules.add(validationRule)
            }
        }

        if (validators.luhn != null) {
            validationRules.add(ValidationRuleLuhn())
        }

        if (validators.range != null) {
            val range = validators.range
            if (range.minValue != null && range.maxValue != null) {
                val validationRule: AbstractValidationRule = ValidationRuleRange(range.minValue, range.maxValue)
                validationRules.add(validationRule)
            }
        }

        if (validators.termsAndConditions != null) {
            validationRules.add(ValidationRuleTermsAndConditions())
        }

        if (validators.regularExpression != null) {
            if (validators.regularExpression.regularExpression != null) {
                val validationRule: AbstractValidationRule =
                    ValidationRuleRegex(validators.regularExpression.regularExpression)
                validationRules.add(validationRule)
            }
        }

        if (validators.emailAddress != null) {
            validationRules.add(ValidationRuleEmailAddress())
        }
    }

    fun getValidationRules(): MutableList<AbstractValidationRule> {
        if (validationRules.isEmpty()) {
            setValidationRules()
        }

        return validationRules
    }

    fun isRequired(): Boolean {
        return isRequired == true
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -549503465906936684L
    }
}
