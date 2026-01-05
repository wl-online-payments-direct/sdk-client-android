/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRule
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleEmailAddress
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleExpirationDate
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleFixedList
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleIBAN
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleLength
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleLuhn
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleRange
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleRegex
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleTermsAndConditions
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ValidatorsDto

internal class ValidationRuleFactory {
    fun createRules(validator: ValidatorsDto?): List<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        val validators = validator ?: return rules

        if (validators.luhn != null) {
            rules.add(ValidationRuleLuhn())
        }

        if (validators.iban != null) {
            rules.add(ValidationRuleIBAN())
        }

        if (validators.termsAndConditions != null) {
            rules.add(ValidationRuleTermsAndConditions())
        }

        if (validators.regularExpression?.regularExpression != null) {
            rules.add(ValidationRuleRegex(validators.regularExpression.regularExpression))
        }

        if (validators.emailAddress != null) {
            rules.add(ValidationRuleEmailAddress())
        }

        if (validators.expirationDate != null) {
            rules.add(ValidationRuleExpirationDate())
        }

        if (validators.fixedList?.allowedValues != null) {
            rules.add(ValidationRuleFixedList(validators.fixedList.allowedValues))
        }

        if (validators.length != null) {
            val length = validators.length
            if (length.minLength != null && length.maxLength != null) {
                rules.add(ValidationRuleLength(length.minLength, length.maxLength))
            }
        }

        if (validators.range != null) {
            val range = validators.range
            if (range.minValue != null && range.maxValue != null) {
                rules.add(ValidationRuleRange(range.minValue, range.maxValue))
            }
        }

        return rules
    }
}
