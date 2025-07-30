/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Junit Test class which tests isEditingAllowed boolean of AccountOnFile
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountOnFileJavaTest {
    private final AccountOnFile accountOnFile = GsonHelperJava.fromResourceJson(
        "accountOnFileVisa.json",
        AccountOnFile.class
    );

    @Test
    public void testIsEditingAllowed() {
        AccountOnFileAttribute aofaAlias = accountOnFile.getAttributes().get(0);
        assertFalse(aofaAlias.isEditingAllowed());

        AccountOnFileAttribute aofaFirstName = accountOnFile.getAttributes().get(1);
        assertFalse(aofaFirstName.isEditingAllowed());

        AccountOnFileAttribute aofaSurname = accountOnFile.getAttributes().get(2);
        assertFalse(aofaSurname.isEditingAllowed());

        AccountOnFileAttribute aofaCardholderName = accountOnFile.getAttributes().get(3);
        assertFalse(aofaCardholderName.isEditingAllowed());

        AccountOnFileAttribute aofaCardNumber = accountOnFile.getAttributes().get(4);
        assertFalse(aofaCardNumber.isEditingAllowed());

        AccountOnFileAttribute aofaCvv = accountOnFile.getAttributes().get(5);
        assertTrue(aofaCvv.isEditingAllowed());

        AccountOnFileAttribute aofaExpiryDate = accountOnFile.getAttributes().get(6);
        assertTrue(aofaExpiryDate.isEditingAllowed());
    }

    @Test
    public void testMaskingValue() {
        assertEquals("4111 11XX XXXX 1111 ", accountOnFile.getMaskedValue("alias"));
        assertEquals("4111 11XX XXXX 1111 ", accountOnFile.getLabel());
    }
}

