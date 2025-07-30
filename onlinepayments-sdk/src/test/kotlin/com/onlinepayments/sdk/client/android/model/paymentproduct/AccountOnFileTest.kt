/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct

import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Junit Test class which tests isEditingAllowed boolean of AccountOnFile
 */
@RunWith(MockitoJUnitRunner::class)
class AccountOnFileTest {
    private val accountOnFile: AccountOnFile =
        GsonHelper.fromResourceJson<AccountOnFile>("accountOnFileVisa.json", AccountOnFile::class.java)

    @Test
    fun testIsEditingAllowed() {
        val aofaAlias = accountOnFile.attributes[0]
        Assert.assertFalse(aofaAlias.isEditingAllowed())

        val aofaFirstName = accountOnFile.attributes[1]
        Assert.assertFalse(aofaFirstName.isEditingAllowed())

        val aofaSurname = accountOnFile.attributes[2]
        Assert.assertFalse(aofaSurname.isEditingAllowed())

        val aofaCardholderName = accountOnFile.attributes[3]
        Assert.assertFalse(aofaCardholderName.isEditingAllowed())

        val aofaCardNumber = accountOnFile.attributes[4]
        Assert.assertFalse(aofaCardNumber.isEditingAllowed())

        val aofaCvv = accountOnFile.attributes[5]
        Assert.assertTrue(aofaCvv.isEditingAllowed())

        val aofaExpiryDate = accountOnFile.attributes[6]
        Assert.assertTrue(aofaExpiryDate.isEditingAllowed())
    }

    @Test
    fun testMaskingValue() {
        Assert.assertEquals("4111 11XX XXXX 1111 ", accountOnFile.getMaskedValue("alias"))
        Assert.assertEquals("4111 11XX XXXX 1111 ", accountOnFile.getLabel())
    }
}

