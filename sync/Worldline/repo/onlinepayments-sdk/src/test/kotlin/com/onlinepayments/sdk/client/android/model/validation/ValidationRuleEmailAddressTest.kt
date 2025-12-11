/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation

import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ValidationRuleEmailAddressTest {

    private lateinit var validationRule: ValidationRuleEmailAddress
    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "Email"

    @Before
    fun setup() {
        validationRule = ValidationRuleEmailAddress()

        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductForValidators.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun testValidEmailAddresses() {
        val validEmails = listOf(
            "simple@example.com",
            "very.common@example.com",
            "user.name@domain.co.uk",
            "user-name@domain.co.uk",
            "user_name@example.org",
            "user+tag@example.com",
            "user123@sub.domain.com",
            "user@example.travel",
            "user@example.museum",
            "user@example.co",
            "u@a.io",
            "firstname.o'lastname@example.com",
            "user%example@example.org"
        )

        validEmails.forEach { email ->
            paymentRequest.setValue(fieldId, email)
            assertTrue("Email '$email' should be valid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testInvalidEmailAddresses() {
        val invalidEmails = listOf(
            "",
            "plainaddress",
            "@no-local-part.com",
            "Outlook Contact <outlook@example.com>",
            "user.name@.com",
            "user.@example.com",
            ".user@example.com",
            "user..name@example.com",
            "user@example..com",
            "user@.example.com",
            "user@example.com.",
            "user@example",
            "user@-example.com",
            "user@example-.com",
            "user@exam_ple.com",
            "test..test@example.com",
            "user@123.123.123.123",
            "user@[300.300.300.300]",
            "test email@example.com",
            "user@exam ple.com",
            "user@.com",
            "user@com.",
            "user@@example.com",
            "\"quoted\"@example.com",
            "user\n@example.com",
        )

        invalidEmails.forEach { email ->
            paymentRequest.setValue(fieldId, email)
            assertFalse("Email '$email' should be invalid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testNonExistentField() {
        assertFalse("Non-existent field should be invalid", validationRule.validate(paymentRequest, "nonExistentField"))
    }

    @Test
    fun testValidationRuleType() {
        assertTrue(
            "ValidationRuleEmailAddress should have EMAILADDRESS type",
            validationRule.type == ValidationType.EMAILADDRESS
        )
    }

    @Test
    fun testMessageId() {
        assertTrue(
            "ValidationRuleEmailAddress should have correct messageId",
            validationRule.messageId == "emailAddress"
        )
    }
}