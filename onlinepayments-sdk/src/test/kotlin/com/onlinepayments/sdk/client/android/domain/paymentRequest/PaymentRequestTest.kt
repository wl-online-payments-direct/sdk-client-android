/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentRequest

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaymentRequestTest {
    private lateinit var paymentProduct: PaymentProduct
    private lateinit var paymentRequest: PaymentRequest
    private lateinit var accountOnFile: AccountOnFile

    @BeforeTest
    fun setUp() {
        val paymentProductResponse = GsonHelper.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto::class.java
        )

        paymentProduct = PaymentProductFactory().createPaymentProduct(paymentProductResponse)

        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileVisa.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun `should return field for nonexistent fieldId expiryDate`() {
        val field = paymentRequest.getField("expiryDate")

        assertEquals("expirydate", field.getType().toString().lowercase())
        assertEquals("expiryDate", field.getId())
    }

    @Test
    fun `should return correct length`() {
        val fieldExpiryDate = paymentRequest.getField("expiryDate")
        val fieldCvv = paymentRequest.getField("cvv")
        val fieldCardNumber = paymentRequest.getField("cardNumber")

        assertNull(fieldExpiryDate.getValue())
        assertNull(fieldCvv.getValue())
        assertNull(fieldCardNumber.getValue())

        fieldExpiryDate.setValue("12-35")
        fieldCvv.setValue("123")
        fieldCardNumber.setValue("1234567890123456789")

        val fields = paymentRequest.getValues()

        assertEquals(3, fields.size)

        assertEquals("1235", paymentRequest.getValue("expiryDate"))
        assertEquals("123", paymentRequest.getValue("cvv"))
        assertEquals("1234567890123456789", paymentRequest.getValue("cardNumber"))

        assertEquals("1235", fieldExpiryDate.getValue())
        assertEquals("123", fieldCvv.getValue())
        assertEquals("1234567890123456789", fieldCardNumber.getValue())
    }

    @Test
    fun `should get value 123 for cvv fieldId`() {
        val field = paymentRequest.getField("cvv")
        field.setValue("123")

        val value = paymentRequest.getValue("cvv")

        assertEquals("123", value)
    }

    @Test
    fun `should set value 123 for cvv fieldId`() {
        val field = paymentRequest.getField("cvv")

        assertNull(field.getValue())

        paymentRequest.setValue("cvv", "123")
        val value = paymentRequest.getValue("cvv")

        assertEquals("123", value)
    }

    @Test
    fun `should set and get accountOnFile`() {
        assertNull(paymentRequest.getAccountOnFile())

        paymentRequest.setAccountOnFile(accountOnFile)

        assertNotNull(paymentRequest.getAccountOnFile())
        assertEquals("123", paymentRequest.getAccountOnFile()?.id)
    }

    @Test
    fun `should set and get tokenize`() {
        assertFalse(paymentRequest.getTokenize())

        paymentRequest.setTokenize(true)

        assertTrue(paymentRequest.getTokenize())
    }

    @Test
    fun `should return undefined when getValue called for READ_ONLY fields`() {
        paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

        assertNull(paymentRequest.getValue("cardNumber"))
    }

    @Test
    fun `should remove field values after setting accountOnFile and return undefined if READ_ONLY`() {
        val paymentRequest = PaymentRequest(paymentProduct)
        assertNull(paymentRequest.getAccountOnFile())

        val cardNumberField = paymentRequest.getField("cardNumber")
        cardNumberField.setValue("1111111111")

        paymentRequest.setAccountOnFile(accountOnFile)

        assertFailsWith<Exception> {
            paymentRequest.getField("cardNumber").setValue("2222222222")
        }

        assertNull(paymentRequest.getField("cardNumber").getValue())
        assertNotNull(paymentRequest.getAccountOnFile())
        assertEquals("123", paymentRequest.getAccountOnFile()?.id)
    }

    @Test
    fun `should return errors for cvv cardNumber expiryDate Field required if no accountOnFile provided`() {
        paymentRequest = PaymentRequest(paymentProduct)

        val validationResult = paymentRequest.validate()

        assertFalse(validationResult.isValid)
        assertEquals(3, validationResult.errors.size)
    }

    @Test
    fun `should return errors for cvv Field required if accountOnFile provided`() {
        paymentRequest.setAccountOnFile(accountOnFile)

        val validationResult = paymentRequest.validate()

        assertFalse(validationResult.isValid)
        assertEquals(1, validationResult.errors.size)
    }

    @Test
    fun `should return empty errors and isValid when all fields set correctly`() {
        paymentRequest.setValue("cardNumber", "7822551678890142249")
        paymentRequest.setValue("expiryDate", "11/2026")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("cardholderName", "test")

        val validationResult = paymentRequest.validate()

        assertTrue(validationResult.isValid)
        assertEquals(0, validationResult.errors.size)
    }

    @Test
    fun `should return error for 'cvv' when it is 'MUST_WRITE' in accountOnFile and user provides no value with AOF`(){
        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileWithMustWriteCvv.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

        val validationResult = paymentRequest.validate()

        assertFalse(validationResult.isValid)
        assertEquals(1, validationResult.errors.size)
    }

    @Test
    fun `should return error for 'cvv' when it is 'MUST_WRITE' and user provides invalid value with AOF`(){
        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileWithMustWriteCvv.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

       paymentRequest.getField("cvv").setValue("1")

        val validationResult = paymentRequest.validate()

        assertFalse(validationResult.isValid)
        assertEquals(1, validationResult.errors.size)
    }

    @Test
    fun `should not return error for 'cvv' when it is 'MUST_WRITE' and user provides valid value with AOF`(){
        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileWithMustWriteCvv.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

        paymentRequest.getField("cvv").setValue("123")

        val validationResult = paymentRequest.validate()

        assertTrue(validationResult.isValid)
        assertEquals(0, validationResult.errors.size)
    }

    @Test
    fun `should pass validation when 'cardholderName' is 'CAN_WRITE' and user does not provide value it should not validate it`(){
        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileWithMustWriteCvv.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

        paymentRequest.setValue("expiryDate", "11/2026")
        paymentRequest.setValue("cvv", "123")


        val validationResult = paymentRequest.validate()

        assertTrue(validationResult.isValid)
        assertEquals(0, validationResult.errors.size)
    }

    @Test
    fun `should not pass validation when 'cvv' is 'MUST_WRITE' and user does not provide value`(){
        val accountOnFileResponse = GsonHelper.fromResourceJson(
            "accountOnFileWithMustWriteCvv.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(accountOnFileResponse)

        val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)

        paymentRequest.setValue("expiryDate", "11/2026")

        val validationResult = paymentRequest.validate()


        assertFalse(validationResult.isValid)
        assertEquals(1, validationResult.errors.size)
    }
}
