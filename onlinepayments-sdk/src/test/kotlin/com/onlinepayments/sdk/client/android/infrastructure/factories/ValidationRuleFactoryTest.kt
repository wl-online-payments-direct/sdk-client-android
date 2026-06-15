/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

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
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.EmailAddressDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.ExpirationDateDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.FixedListDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.IBANDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.LengthDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.LuhnDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.RangeDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.RegularExpressionDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.TermsAndConditionsDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleFactoryTest {
    private val factory = ValidationRuleFactory()

    @Test
    fun `createRules creates validation rules when validators are present`() {
        val validators = ValidatorsDto(
            expirationDate = ExpirationDateDto,
            emailAddress = EmailAddressDto,
            iban = IBANDto,
            fixedList = FixedListDto(allowedValues = mutableListOf("A", "B")),
            length = LengthDto(minLength = 5, maxLength = 10),
            luhn = LuhnDto,
            range = RangeDto(minValue = 1, maxValue = 100),
            regularExpression = RegularExpressionDto(regularExpression = "[0-9]+"),
            termsAndConditions = TermsAndConditionsDto
        )

        val rules = factory.createRules(validators)

        assertEquals(9, rules.size)
        assertTrue(rules.any { it is ValidationRuleExpirationDate })
        assertTrue(rules.any { it is ValidationRuleEmailAddress })
        assertTrue(rules.any { it is ValidationRuleIBAN })
        assertTrue(rules.any { it is ValidationRuleFixedList })
        assertTrue(rules.any { it is ValidationRuleLength })
        assertTrue(rules.any { it is ValidationRuleLuhn })
        assertTrue(rules.any { it is ValidationRuleRange })
        assertTrue(rules.any { it is ValidationRuleRegex })
        assertTrue(rules.any { it is ValidationRuleTermsAndConditions })
    }

    @Test
    fun `createRules returns empty list when validators is null`() {
        val rules = factory.createRules(null)

        assertTrue(rules.isEmpty())
    }

    @Test
    fun `createRules skips length rule when minLength or maxLength is absent`() {
        val validators = ValidatorsDto(length = LengthDto(minLength = 5, maxLength = null))

        val rules = factory.createRules(validators)

        assertFalse(rules.any { it is ValidationRuleLength })
    }

    @Test
    fun `createRules skips range rule when minValue or maxValue is absent`() {
        val validators = ValidatorsDto(range = RangeDto(minValue = null, maxValue = 100))

        val rules = factory.createRules(validators)

        assertFalse(rules.any { it is ValidationRuleRange })
    }
}
