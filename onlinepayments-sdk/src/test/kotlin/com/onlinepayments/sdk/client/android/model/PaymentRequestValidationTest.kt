/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import com.onlinepayments.sdk.client.android.model.PaymentRequestValidationTest.TestPaymentRequest
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddress
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDate
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedList
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBAN
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhn
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRange
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegex
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleTermsAndConditions
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.Boolean
import java.util.ArrayList

/**
 * Junit Test class which tests validation functionality
 */
@RunWith(MockitoJUnitRunner::class)
class PaymentRequestValidationTest {
    private val paymentProductVisa: PaymentProduct? =
        GsonHelper.fromResourceJson<PaymentProduct>("paymentProductVisa.json", PaymentProduct::class.java)

    private val emailAddressValid = "aa@bb.com"
    private val emailAddressInvalid = "aa2bb.com"

    private val expirationDateValid = "1125"
    private val expirationDateInvalid = "0000"

    private val listEntries = ArrayList<String?>()
    private val validListOption = "1"
    private val invalidListOption = "a"

    private val minLength = 0
    private val maxLength = 10
    private val validLength = "abc"
    private val invalidLength = "abcabcabcabcabc"

    private val validIBAN = "GB33BUKB20201555555555"
    private val invalidIBAN = "GB94BARC20201530093459"

    private val validLuhnCheck = "4242424242424242"
    private val invalidLuhnCheck = "1142424242424242"

    private val validRange = "1"
    private val invalidRange = "150"

    private val regex = "\\d{2}[a-z]{2}[A-Z]{3}"
    private val validRegex = "11atAAB"
    private val invalidRegex = "abcabcabc"

    init {
        // Fill the test listEntries

        listEntries.add("1")
        listEntries.add("2")
        listEntries.add("3")
    }

    // Test email address validator
    @Test
    fun testValidEmailAddress() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("emailAddress", emailAddressValid)
        val rule = ValidationRuleEmailAddress()
        Assert.assertTrue(rule.validate(paymentRequest, "emailAddress"))
    }

    @Test
    fun testInvalidEmailAddress() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("emailAddress", emailAddressInvalid)
        val rule = ValidationRuleEmailAddress()
        Assert.assertFalse(rule.validate(paymentRequest, "emailAddress"))
    }

    // Test expiration date validator
    @Test
    fun testValidExpirationDate() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("expirationDate", expirationDateValid)
        val rule = ValidationRuleExpirationDate()
        Assert.assertTrue(rule.validate(paymentRequest, "expirationDate"))
    }

    @Test
    fun testInvalidExpirationDate() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("expirationDate", expirationDateInvalid)
        val rule = ValidationRuleExpirationDate()
        Assert.assertFalse(rule.validate(paymentRequest, "expirationDate"))
    }

    // Test fixed list validator
    @Test
    fun testValidFixedList() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("fixedList", validListOption)
        val rule = ValidationRuleFixedList(listEntries)
        Assert.assertTrue(rule.validate(paymentRequest, "fixedList"))
    }

    @Test
    fun testInvalidFixedList() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("fixedList", invalidListOption)
        val rule = ValidationRuleFixedList(listEntries)
        Assert.assertFalse(rule.validate(paymentRequest, "fixedList"))
    }

    // Test IBAN validator
    @Test
    fun testValidIBAN() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("IBAN", validIBAN)
        val rule = ValidationRuleIBAN()
        Assert.assertTrue(rule.validate(paymentRequest, "IBAN"))
    }

    @Test
    fun testInvalidIBAN() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("IBAN", invalidIBAN)
        val rule = ValidationRuleIBAN()
        Assert.assertFalse(rule.validate(paymentRequest, "IBAN"))
    }

    // Test length validator
    @Test
    fun testValidLength() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("length", validLength)
        val rule = ValidationRuleLength(minLength, maxLength)
        Assert.assertTrue(rule.validate(paymentRequest, "length"))
    }

    @Test
    fun testValidZeroLength() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("length", "")
        val rule = ValidationRuleLength(minLength, maxLength)
        Assert.assertTrue(rule.validate(paymentRequest, "length"))
    }

    @Test
    fun testValidFieldNullLength() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        val rule = ValidationRuleLength(minLength, maxLength)
        Assert.assertTrue(rule.validate(paymentRequest, "length"))
    }

    @Test
    fun testInvalidLength() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("length", invalidLength)
        val rule = ValidationRuleLength(minLength, maxLength)
        Assert.assertFalse(rule.validate(paymentRequest, "length"))
    }

    // Test luhn validator
    @Test
    fun testValidLuhn() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("luhn", validLuhnCheck)
        val rule = ValidationRuleLuhn()
        Assert.assertTrue(rule.validate(paymentRequest, "luhn"))
    }

    @Test
    fun testInvalidLuhn() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("luhn", invalidLuhnCheck)
        val rule = ValidationRuleLuhn()
        Assert.assertFalse(rule.validate(paymentRequest, "luhn"))
    }

    // Test range validator
    @Test
    fun testValidRange() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("range", validRange)
        val rule = ValidationRuleRange(minLength, maxLength)
        Assert.assertTrue(rule.validate(paymentRequest, "range"))
    }

    @Test
    fun testInvalidRange() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("range", invalidRange)
        val rule = ValidationRuleRange(minLength, maxLength)
        Assert.assertFalse(rule.validate(paymentRequest, "range"))
    }

    // Test regex validator
    @Test
    fun testValidRegex() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("regex", validRegex)
        val rule = ValidationRuleRegex(regex)
        Assert.assertTrue(rule.validate(paymentRequest, "regex"))
    }

    @Test
    fun testInValidRegex() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("regex", invalidRegex)
        val rule = ValidationRuleRegex(regex)
        Assert.assertFalse(rule.validate(paymentRequest, "regex"))
    }

    // Test terms and conditions validator
    @Test
    fun testValidTermsAndConditions() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("termsAndConditions", Boolean.TRUE.toString())
        val rule = ValidationRuleTermsAndConditions()
        Assert.assertTrue(rule.validate(paymentRequest, "termsAndConditions"))
    }

    @Test
    fun testInValidTermsAndConditions() {
        val paymentRequest: PaymentRequest = TestPaymentRequest(paymentProductVisa)
        paymentRequest.setValue("termsAndConditions", "test")
        val rule = ValidationRuleTermsAndConditions()
        Assert.assertFalse(rule.validate(paymentRequest, "termsAndConditions"))
    }

    private class TestPaymentRequest(paymentProduct: PaymentProduct?) : PaymentRequest(paymentProduct) {
        override fun getUnmaskedValue(paymentProductFieldId: String, value: String): String {
            // no actual payment product fields are available
            return value
        }
    }
}
