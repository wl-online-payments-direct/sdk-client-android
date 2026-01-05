/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.models

import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasicPaymentProductsTest {
    private lateinit var basicPaymentProducts: BasicPaymentProducts

    @BeforeTest
    fun setUp() {
        val dto = GsonHelper.fromResourceJson(
            "basicPaymentProducts.json",
            BasicPaymentProductsDto::class.java
        )

        basicPaymentProducts = PaymentProductFactory().createBasicPaymentProducts(dto)
    }

    @Test
    fun `constructor maps paymentProducts and deduplicates accountsOnFile by id`() {
        assertEquals(3, basicPaymentProducts.paymentProducts.size)

        val accounts = basicPaymentProducts.accountsOnFile
        assertEquals(2, accounts.size)

        val ids = accounts.map { it.id }.toSet()
        assertEquals(setOf("1234", "5678"), ids)
    }

    @Test
    fun `constructor handles null paymentProducts`() {
        val dto = BasicPaymentProductsDto(paymentProducts = null)

        val emptyBasicPaymentProduct = PaymentProductFactory().createBasicPaymentProducts(dto)

        assertTrue(emptyBasicPaymentProduct.paymentProducts.isEmpty())
        assertTrue(emptyBasicPaymentProduct.accountsOnFile.isEmpty())
    }
}

