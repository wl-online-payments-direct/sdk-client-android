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

package com.onlinepayments.sdk.client.android.domain.paymentProduct

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFileAttribute
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileAttributeDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileDisplayHintsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountOnFileTest {
    private lateinit var accountOnFile: AccountOnFile

    @BeforeTest
    fun setUp() {
        val dto = GsonHelper.fromResourceJson(
            "accountOnFileVisa.json",
            AccountOnFileDto::class.java
        )

        accountOnFile = PaymentProductFactory().createAccountOnFile(dto)
    }

    @Test
    fun `getLabel should return label alias`() {
        val label = accountOnFile.label
        assertEquals("4111 11XX XXXX 1111", label)
    }

    @Test
    fun `id should be 123`() {
        assertEquals("123", accountOnFile.id)
    }

    @Test
    fun `paymentProductId should be 1`() {
        assertEquals(1, accountOnFile.paymentProductId)
    }

    @Test
    fun `getRequiredAttributes should return attributes with status MUST_WRITE`() {
        val requiredAttributes = accountOnFile.getRequiredAttributes()

        assertEquals(1, requiredAttributes.size)

        val firstAttribute = requiredAttributes[0]
        assertEquals("cvv", firstAttribute.key)
        assertEquals(AccountOnFileAttribute.Status.MUST_WRITE, firstAttribute.status)
        assertEquals("111", firstAttribute.value)
    }

    @Test
    fun `getWritableAttributes should return all attributes that are not 'READ_ONLY'`() {
        val requiredAttributes = accountOnFile.getWritableAttributes()

        assertEquals(2, requiredAttributes.size)
    }

    @Test
    fun `getReadOnlyAttributes should return all attributes that are 'READ_ONLY'`() {
        val requiredAttributes = accountOnFile.getReadOnlyAttributes()

        assertEquals(5, requiredAttributes.size)
    }

    @Test
    fun `isWritable should return false for cardNumber`() {
        val isWritable = accountOnFile.isWritable("cardNumber")
        assertFalse(isWritable)
    }

    @Test
    fun `isWritable should return true for cvv`() {
        val isWritable = accountOnFile.isWritable("cvv")
        assertTrue(isWritable)
    }

    @Test
    fun `getValue should return 411111XXXXXX1111 for cardNumber`() {
        val value = accountOnFile.getValue("cardNumber")
        assertEquals("411111XXXXXX1111", value)
    }

    @Test
    fun `getValue should return empty string for attribute with empty value`() {
        val dto = AccountOnFileDto(
            id = "999",
            paymentProductId = 1,
            displayHints = AccountOnFileDisplayHintsDto(labelTemplate = emptyList()),
            attributes = listOf(
                AccountOnFileAttributeDto("nickname", "", AccountOnFileAttributeDto.Status.CAN_WRITE)
            )
        )

        val aof = PaymentProductFactory().createAccountOnFile(dto)

        assertEquals("", aof.getValue("nickname"))
    }

    @Test
    fun `getAttribute should return attribute for existing key`() {
        val attribute = accountOnFile.getAttribute("cardNumber")

        assertNotNull(attribute)
        assertEquals("cardNumber", attribute.key)
        assertEquals("411111XXXXXX1111", attribute.value)
        assertEquals(AccountOnFileAttribute.Status.READ_ONLY, attribute.status)
    }
}
