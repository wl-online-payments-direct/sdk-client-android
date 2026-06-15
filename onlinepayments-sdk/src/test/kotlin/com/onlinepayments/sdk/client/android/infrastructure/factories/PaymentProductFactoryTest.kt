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

import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.DataRestrictionsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.displayHints.ProductFieldDisplayHintsDto
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentProductFactoryTest {
    private val factory = PaymentProductFactory()

    private val basePaymentProductDto = GsonHelper.fromResourceJson(
        "basicPaymentProduct.json",
        BasicPaymentProductDto::class.java
    )

    private val basePaymentProductDto2 = GsonHelper.fromResourceJson(
        "basicPaymentProduct2.json",
        BasicPaymentProductDto::class.java
    )

    @Test
    fun `createBasicPaymentProduct should return BasicPaymentProduct instance`() {
        val product = factory.createBasicPaymentProduct(basePaymentProductDto)

        assertIs<BasicPaymentProduct>(product)
        assertEquals(0, product.id)
        assertEquals(2, product.accountsOnFile.size)
    }

    @Test
    fun `createBasicPaymentProducts should return a list of BasicPaymentProduct instances`() {
        val dto = BasicPaymentProductsDto(
            paymentProducts = mutableListOf(basePaymentProductDto, basePaymentProductDto2)
        )

        val result = factory.createBasicPaymentProducts(dto)

        assertIs<BasicPaymentProduct>(result.paymentProducts[0])
        assertEquals(0, result.paymentProducts[0].id)

        assertIs<BasicPaymentProduct>(result.paymentProducts[1])
        assertEquals(1, result.paymentProducts[1].id)
    }

    @Test
    fun `createBasicPaymentProducts deduplicates accounts on file that share the same id`() {
        val dto = BasicPaymentProductsDto(
            paymentProducts = mutableListOf(basePaymentProductDto, basePaymentProductDto2)
        )

        val result = factory.createBasicPaymentProducts(dto)

        assertEquals(2, result.accountsOnFile.size)
        assertEquals(result.accountsOnFile.size, result.accountsOnFile.map { it.id }.distinct().size)
    }

    @Test
    fun `createBasicPaymentProduct maps paymentProduct320SpecificData from dto`() {
        val product = factory.createBasicPaymentProduct(basePaymentProductDto)

        assertNotNull(product.paymentProduct320SpecificData)
        assertEquals("test gateway", product.paymentProduct320SpecificData.gateway)
    }

    @Test
    fun `createDisplayHintsForField uses Int MAX_VALUE as display order when dto value is absent`() {
        val dto = ProductFieldDisplayHintsDto()

        val result = factory.createDisplayHintsForField(dto)

        assertEquals(Int.MAX_VALUE, result.displayOrder)
    }

    @Test
    fun `createDataRestrictions defaults required to false when not defined in dto`() {
        val dto = DataRestrictionsDto()

        val result = factory.createDataRestrictions(dto)

        assertEquals(false, result.isRequired())
        assertTrue(result.validationRules.isEmpty())
    }
}