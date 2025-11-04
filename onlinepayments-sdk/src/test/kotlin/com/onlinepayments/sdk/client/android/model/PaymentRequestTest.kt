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

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileDisplay
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.model.validation.AbstractValidationRule
import com.onlinepayments.sdk.client.android.model.validation.ValidationType
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.HashMap
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Junit Test class which tests PaymentRequest
 */
@RunWith(MockitoJUnitRunner::class)
class PaymentRequestTest {
    private val paymentProductVisa: PaymentProduct =
        GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductVisa.json",
            PaymentProduct::class.java
        )
    private val paymentProductAmEx: PaymentProduct? =
        GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductAmEx.json",
            PaymentProduct::class.java
        )
    private val paymentProductInvoice: PaymentProduct? =
        GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductInVoice.json",
            PaymentProduct::class.java
        )
    private val paymentProductPayPal: PaymentProduct? =
        GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductPayPal.json",
            PaymentProduct::class.java
        )

    private val accountOnFileVisa: AccountOnFile =
        GsonHelper.fromResourceJson<AccountOnFile>(
            "accountOnFileVisa.json",
            AccountOnFile::class.java
        )

    private val parsedPaymentRequest: PaymentRequest =
        GsonHelper.fromResourceJson<PaymentRequest>(
            "paymentRequest.json",
            PaymentRequest::class.java
        )

    @Test
    fun testDeserialization() {
        val actual: PaymentRequest? = parsedPaymentRequest

        Assert.assertTrue(
            "Mismatch in tokenize for paymentRequest: ",
            parsedPaymentRequest.tokenize
        )
        testAccountOnFileEquality(actual!!.accountOnFile!!, accountOnFileVisa)

        testPaymentProductEquality(actual.paymentProduct!!, paymentProductVisa)

        Assert.assertEquals(
            "Mismatch in fieldsValues for paymentRequest",
            allValidValuesVisa,
            actual.getValues()
        )
    }

    @Test
    fun testConstructors() {
        var paymentRequest = PaymentRequest(paymentProductVisa)

        Assert.assertFalse(paymentRequest.tokenize)
        Assert.assertNull(paymentRequest.accountOnFile)
        Assert.assertEquals(paymentProductVisa, paymentRequest.paymentProduct)

        paymentRequest = PaymentRequest(paymentProductVisa, true)

        Assert.assertTrue(paymentRequest.tokenize)
        Assert.assertNull(paymentRequest.accountOnFile)
        Assert.assertEquals(paymentProductVisa, paymentRequest.paymentProduct)

        paymentRequest = PaymentRequest(paymentProductVisa, accountOnFileVisa)

        Assert.assertFalse(paymentRequest.tokenize)
        Assert.assertEquals(accountOnFileVisa, paymentRequest.accountOnFile)
        Assert.assertEquals(paymentProductVisa, paymentRequest.paymentProduct)

        paymentRequest = PaymentRequest(paymentProductVisa, accountOnFileVisa, true)

        Assert.assertTrue(paymentRequest.tokenize)
        Assert.assertEquals(accountOnFileVisa, paymentRequest.accountOnFile)
        Assert.assertEquals(paymentProductVisa, paymentRequest.paymentProduct)

        paymentRequest = PaymentRequest()

        Assert.assertFalse(paymentRequest.tokenize)
        Assert.assertNull(paymentRequest.accountOnFile)
        Assert.assertNull(paymentRequest.paymentProduct)
    }

    @Test
    fun testValidateSucceedsForValidValuesVisa() {
        val validVisaValuesRequest = PaymentRequest(paymentProductVisa)
        setValuesInRequest(allValidValuesVisa, validVisaValuesRequest)
        Assert.assertTrue(validVisaValuesRequest.validate().isEmpty())
    }

    @Test
    fun testValidateSucceedsForValidValuesAmEx() {
        val validValuesRequest = PaymentRequest(paymentProductAmEx)
        setValuesInRequest(allValidValuesAmEx, validValuesRequest)
        Assert.assertTrue(validValuesRequest.validate().isEmpty())
    }

    @Test
    fun testValidateSucceedsForValidValuesInVoice() {
        val validInVoiceValuesRequest = PaymentRequest(paymentProductInvoice)
        setValuesInRequest(allValidValuesInVoice, validInVoiceValuesRequest)
        Assert.assertTrue(validInVoiceValuesRequest.validate().isEmpty())
    }

    @Test
    fun testValidateSucceedsForNoValuesPayPal() {
        val validPayPalValuesRequest = PaymentRequest(paymentProductPayPal)
        Assert.assertTrue(validPayPalValuesRequest.validate().isEmpty())
    }

    @Test
    fun testValidateFailsForInvalidCCNVisa() {
        val invalidVisaCCNRequest = PaymentRequest(paymentProductVisa)
        setValuesInRequest(invalidCCNVisa, invalidVisaCCNRequest)
        Assert.assertFalse(invalidVisaCCNRequest.validate().isEmpty())
    }

    @Test
    fun testValidateFailsForInValidStateInVoice() {
        val invalidStateInVoiceRequest = PaymentRequest(paymentProductInvoice)
        setValuesInRequest(invalidStateInVoice, invalidStateInVoiceRequest)
        Assert.assertFalse(invalidStateInVoiceRequest.validate().isEmpty())
    }

    @Test
    fun testValidateFailsForMissingRequiredValuesVisa() {
        val missingCCNVisaRequest = PaymentRequest(paymentProductVisa)
        setValuesInRequest(missingCCNVisa, missingCCNVisaRequest)
        Assert.assertFalse(missingCCNVisaRequest.validate().isEmpty())
    }

    @Test
    fun testValidateFailsForInvalidCVVVisa() {
        val invalidCVVVisaRequest = PaymentRequest(paymentProductVisa)
        setValuesInRequest(invalidCVVVisa, invalidCVVVisaRequest)
        Assert.assertFalse(invalidCVVVisaRequest.validate().isEmpty())
    }

    @Test
    fun testValidateFailsForMissingRequiredValuesInVoice() {
        val missingCityInVoiceRequest = PaymentRequest(paymentProductInvoice)
        setValuesInRequest(missingCityInVoice, missingCityInVoiceRequest)
        Assert.assertFalse(missingCityInVoiceRequest.validate().isEmpty())
    }

    @Test
    fun testValidateSucceedsForAccountOnFileVisa() {
        val accountOnFileVisaRequest = PaymentRequest(paymentProductVisa, accountOnFileVisa)
        Assert.assertTrue(accountOnFileVisaRequest.validate().isEmpty())
    }

    @Test
    fun testValidateSucceedsForAccountOnFileVisaWithChangedFields() {
        val accountOnFileVisaChangedValuesRequest =
            PaymentRequest(paymentProductVisa, accountOnFileVisa)
        Assert.assertTrue(accountOnFileVisaChangedValuesRequest.validate().isEmpty())
    }

    private fun testPaymentProductFieldsEquality(
        actualFields: List<PaymentProductField>,
        expectedFields: List<PaymentProductField>
    ) {
        if (actualFields.size != expectedFields.size) {
            Assert.fail("Expected fields and actual fields are not the same size!")
        }
        // Compare fields
        var index = 0
        for (actualField in actualFields) {
            if (index < paymentProductVisa.getPaymentProductFields().size) {
                val expectedField = expectedFields[index]
                println("expectedField = " + expectedField.id)

                Assert.assertEquals(
                    "Mismatch in ID for field: " + actualField.id,
                    expectedField.id,
                    actualField.id
                )
                Assert.assertEquals(
                    "Mismatch in Type for field: " + actualField.id,
                    expectedField.type,
                    actualField.type
                )
                Assert.assertEquals("Mismatch in ValidationRules for field: " + actualField.id,
                    expectedField.dataRestrictions.getValidationRules().stream()
                        .map<ValidationType?>((Function { obj: AbstractValidationRule? -> obj!!.type }))
                        .collect(Collectors.toList()),
                    actualField.dataRestrictions.getValidationRules().stream()
                        .map<ValidationType?> { obj: AbstractValidationRule? -> obj!!.type }
                        .collect(Collectors.toList())
                )

                Assert.assertEquals(
                    "Mismatch in DisplayOrder for field: " + actualField.id,
                    expectedField.displayHints.displayOrder,
                    actualField.displayHints.displayOrder
                )
                Assert.assertEquals(
                    "Mismatch in AlwaysShow for field: " + actualField.id,
                    expectedField.displayHints.alwaysShow,
                    actualField.displayHints.alwaysShow
                )
                Assert.assertEquals(
                    "Mismatch in Mask for field: " + actualField.id,
                    expectedField.displayHints.mask,
                    actualField.displayHints.mask
                )
                Assert.assertEquals(
                    "Mismatch in Label for field: " + actualField.id,
                    expectedField.displayHints.label,
                    actualField.displayHints.label
                )
                Assert.assertEquals(
                    "Mismatch in FormElementType for field: " + actualField.id,
                    expectedField.displayHints.formElement!!.type,
                    actualField.displayHints.formElement!!.type
                )
                Assert.assertEquals(
                    "Mismatch in PlaceholderLabel for field: " + actualField.id,
                    expectedField.displayHints.placeholderLabel,
                    actualField.displayHints.placeholderLabel
                )
                Assert.assertEquals(
                    "Mismatch in PreferredInputType for field: " + actualField.id,
                    expectedField.displayHints.preferredInputType,
                    actualField.displayHints.preferredInputType
                )
                val expectedTooltip = expectedField.displayHints.tooltip
                val actualTooltip = actualField.displayHints.tooltip
                if (expectedTooltip != null && actualTooltip != null) {
                    Assert.assertEquals(
                        "Mismatch in Tooltip.Label for field: " + actualField.id,
                        expectedTooltip.label,
                        actualTooltip.label
                    )
                } else if (expectedTooltip != null
                    || actualTooltip != null
                ) {
                    Assert.fail("One of the tooltips is null for field: " + actualField.id + "\nactualTooltip: " + actualTooltip + "\nexpectedTooltip: " + expectedTooltip)
                }
                index++
            } else {
                Assert.fail("Parsed paymentRequest has more fields than paymentProductVisa")
            }
        }
    }

    private fun testPaymentProductEquality(actual: PaymentProduct, expected: PaymentProduct) {
        Assert.assertEquals(
            "Mismatch in ID for product: " + actual.getId(),
            expected.getId(),
            actual.getId()
        )
        Assert.assertEquals(
            "Mismatch in PaymentMethod for product: " + actual.getId(),
            expected.paymentMethod,
            actual.paymentMethod
        )
        Assert.assertEquals(
            "Mismatch in DisplayHintsList for product: " + actual.getId(),
            expected.getDisplayHintsList(),
            actual.getDisplayHintsList()
        )
        Assert.assertEquals(
            "Mismatch in PaymentProductGroup for product: " + actual.getId(),
            expected.paymentProductGroup,
            actual.paymentProductGroup
        )
        Assert.assertEquals(
            "Mismatch in Product302SpecificData for product: " + actual.getId(),
            expected.paymentProduct302SpecificData,
            actual.paymentProduct302SpecificData
        )
        Assert.assertEquals(
            "Mismatch in Product320SpecificData for product: " + actual.getId(),
            expected.paymentProduct320SpecificData,
            actual.paymentProduct320SpecificData
        )
        testPaymentProductFieldsEquality(
            actual.getPaymentProductFields(),
            expected.getPaymentProductFields()
        )
    }

    private fun testAccountOnFileEquality(
        actualAccountOnFile: AccountOnFile,
        expectedAccountOnFile: AccountOnFile
    ) {
        Assert.assertEquals(
            "Mismatch in AccountOnFile for accountOnFile: " + actualAccountOnFile.id,
            actualAccountOnFile.id.toLong(),
            expectedAccountOnFile.id.toLong()
        )
        Assert.assertEquals(
            "Mismatch in ID for product: " + actualAccountOnFile.id,
            actualAccountOnFile.paymentProductId,
            expectedAccountOnFile.paymentProductId
        )
        Assert.assertEquals(
            "Mismatch in ID for product: " + actualAccountOnFile.id,
            actualAccountOnFile.getLabel(),
            expectedAccountOnFile.getLabel()
        )
        Assert.assertEquals(
            "Mismatch in ID for product: " + actualAccountOnFile.id,
            actualAccountOnFile.attributes,
            expectedAccountOnFile.attributes
        )
        Assert.assertEquals("Mismatch in ID for product: " + actualAccountOnFile.id,
            actualAccountOnFile.displayHints?.labelTemplate?.stream()
                ?.map<String?>((Function { accountOnFileDisplay: AccountOnFileDisplay? -> accountOnFileDisplay!!.getKey() + ":" + accountOnFileDisplay.mask }
                    ))?.collect(Collectors.toList()),
            expectedAccountOnFile.displayHints?.labelTemplate?.stream()
                ?.map<String?>((Function { accountOnFileDisplay: AccountOnFileDisplay? -> accountOnFileDisplay!!.getKey() + ":" + accountOnFileDisplay.mask }
                    ))?.collect(Collectors.toList())
        )
    }

    companion object {
        var allValidValuesVisa: MutableMap<String, String> = HashMap<String, String>()

        init {
            allValidValuesVisa.put("cardNumber", "4012000033330026")
            allValidValuesVisa.put("expiryDate", "122030")
            allValidValuesVisa.put("cvv", "123")
            allValidValuesVisa.put("cardholderName", "Test")
        }

        var allValidValuesAmEx: MutableMap<String, String> = HashMap<String, String>()

        init {
            allValidValuesAmEx.put("cardNumber", "375418081197346")
            allValidValuesAmEx.put("expiryDate", "1234")
            allValidValuesAmEx.put("cvv", "1234")
            allValidValuesAmEx.put("cardholderName", "Test")
        }

        var allValidValuesInVoice: MutableMap<String, String> = HashMap<String, String>()

        init {
            allValidValuesInVoice.put("stateCode", "abcdefgh")
            allValidValuesInVoice.put("city", "Amsterdam")
            allValidValuesInVoice.put("street", "De Dam")
        }

        var invalidCCNVisa: MutableMap<String, String> = HashMap<String, String>()

        init {
            invalidCCNVisa.put("cardNumber", "401200")
            invalidCCNVisa.put("expiryDate", "1230")
            invalidCCNVisa.put("cvv", "123")
        }

        var invalidStateInVoice: MutableMap<String, String> = HashMap<String, String>()

        init {
            invalidStateInVoice.put("stateCode", "abcdefghijklmnopqrstuvwxyz")
            invalidStateInVoice.put("city", "Amsterdam")
            invalidStateInVoice.put("street", "De dam")
        }

        var missingCCNVisa: MutableMap<String, String> = HashMap<String, String>()

        init {
            missingCCNVisa.put("expiryDate", "1230")
            missingCCNVisa.put("cvv", "123")
        }

        var invalidCVVVisa: MutableMap<String, String> = HashMap<String, String>()

        init {
            invalidCVVVisa.put("cardNumber", "4012000033330026")
            invalidCVVVisa.put("expiryDate", "1230")
            invalidCVVVisa.put("cvv", "12345")
            invalidCVVVisa.put("cardholderName", "John")
        }

        var missingCityInVoice: MutableMap<String, String> = HashMap<String, String>()

        init {
            missingCityInVoice.put("stateCode", "abcdefgh")
            missingCityInVoice.put("street", "De Dam")
        }

        private fun setValuesInRequest(
            values: MutableMap<String, String>,
            request: PaymentRequest
        ) {
            for (entry in values.entries) {
                request.setValue(entry.key, entry.value)
            }
        }
    }
}
