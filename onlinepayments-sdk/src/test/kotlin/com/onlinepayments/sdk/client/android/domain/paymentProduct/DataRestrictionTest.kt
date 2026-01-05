/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleLength
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleRegex
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.DataRestrictionsDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataRestrictionTest {
    @Test
    fun `isRequired returns true when isRequired is true in JSON`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        assertTrue(dataRestrictions.isRequired())
    }

    @Test
    fun `isRequired returns false when isRequired is false in JSON`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsNotRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        assertFalse(dataRestrictions.isRequired())
    }

    @Test
    fun `validationRules includes ValidationRuleLength when length validator is present`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        val rules = dataRestrictions.validationRules

        assertEquals(1, rules.size)
        assertTrue(rules[0] is ValidationRuleLength)
    }

    @Test
    fun `validationRules includes multiple validators when both length and regex are present`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsNotRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        val rules = dataRestrictions.validationRules

        assertEquals(2, rules.size)
        assertTrue(rules.any { it is ValidationRuleLength })
        assertTrue(rules.any { it is ValidationRuleRegex })
    }

    @Test
    fun `validationRules are lazily initialized and cached`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        val rules1 = dataRestrictions.validationRules
        val rules2 = dataRestrictions.validationRules

        assertEquals(rules1, rules2)
    }

    @Test
    fun `isRequired and validators work together correctly for required field with length`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        assertTrue(dataRestrictions.isRequired())
        assertEquals(1, dataRestrictions.validationRules.size)
        assertTrue(dataRestrictions.validationRules[0] is ValidationRuleLength)
    }

    @Test
    fun `isRequired and validators work together correctly for optional field with multiple validators`() {
        val response = GsonHelper.fromResourceJson(
            "dataRestrictionsNotRequired.json",
            DataRestrictionsDto::class.java
        )

        val dataRestrictions = PaymentProductFactory().createDataRestrictions(response)

        assertFalse(dataRestrictions.isRequired())
        assertEquals(2, dataRestrictions.validationRules.size)
    }
}

