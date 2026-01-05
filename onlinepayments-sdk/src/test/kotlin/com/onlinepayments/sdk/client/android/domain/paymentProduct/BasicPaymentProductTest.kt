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

import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BasicPaymentProductTest {
    private lateinit var basicPaymentProduct: BasicPaymentProduct

    @BeforeTest
    fun setUp() {
        val dto = GsonHelper.fromResourceJson(
            "basicPaymentProduct.json",
            BasicPaymentProductDto::class.java
        )

        basicPaymentProduct = PaymentProductFactory().createBasicPaymentProduct(dto)
    }

    @Test
    fun `return label, logo and displayOrder`() {
        val logo = basicPaymentProduct.logo
        val label = basicPaymentProduct.label
        val displayOrder = basicPaymentProduct.displayOrder

        assertNotNull(logo)
        assertNotNull(label)
        assertNotNull(displayOrder)
    }

    @Test
    fun `label should return Test label`() {
        val label = basicPaymentProduct.label
        assertEquals("Test label", label)
    }

    @Test
    fun `logoUrl should return test-logo`() {
        val logo = basicPaymentProduct.logo
        assertEquals("test-logo", logo)
    }

    @Test
    fun `should return displayOrder`() {
        val displayOrder = basicPaymentProduct.displayOrder
        assertEquals(0, displayOrder)
    }

    @Test
    fun `paymentProduct302SpecificData should not be undefined`() {
        val paymentProduct302SpecificData = basicPaymentProduct.paymentProduct302SpecificData

        assertNotNull(paymentProduct302SpecificData)
    }

    @Test
    fun `paymentProduct320SpecificData should not be undefined`() {
        val paymentProduct320SpecificData = basicPaymentProduct.paymentProduct320SpecificData

        assertNotNull(paymentProduct320SpecificData)
        assertContentEquals(
            listOf("test network 1", "test network 2", "test network 3"),
            paymentProduct320SpecificData.networks
        )
        assertEquals("test gateway", paymentProduct320SpecificData.gateway)
    }

    @Test
    fun `accounts should return list of accounts with length 2`() {
        val accountOnFiles = basicPaymentProduct.accountsOnFile

        assertEquals(2, accountOnFiles.size)

        assertEquals("1234", accountOnFiles[0].id)
        assertEquals(1, accountOnFiles[0].paymentProductId)

        assertEquals("5678", accountOnFiles[1].id)
        assertEquals(2, accountOnFiles[1].paymentProductId)
    }

    @Test
    fun `accountOnFile should return accountOnFile for existing id`() {
        val accountOnFile = basicPaymentProduct.getAccountOnFile("5678")

        assertNotNull(accountOnFile)
        assertEquals("5678", accountOnFile.id)
        assertEquals(2, accountOnFile.paymentProductId)
    }

    @Test
    fun `accountOnFile should return null for nonexisting id`() {
        val accountOnFile = basicPaymentProduct.getAccountOnFile("0")

        assertNull(accountOnFile)
    }
}