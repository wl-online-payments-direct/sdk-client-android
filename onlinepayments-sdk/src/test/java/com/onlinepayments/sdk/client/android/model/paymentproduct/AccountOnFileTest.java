package com.onlinepayments.sdk.client.android.model.paymentproduct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.onlinepayments.sdk.client.android.testUtil.GsonHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Junit Testclass which tests isEditingAllowed boolean of AccountOnFile
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountOnFileTest {
    private final AccountOnFile accountOnFile = GsonHelper.fromResourceJson("accountOnFileVisa.json", AccountOnFile.class);

    @Test
    public void testIsEditingAllowed() {
        AccountOnFileAttribute aofaAlias = accountOnFile.getAccountOnFileAttributes().get(0);
        assertFalse(aofaAlias.isEditingAllowed());

        AccountOnFileAttribute aofaFirstName = accountOnFile.getAccountOnFileAttributes().get(1);
        assertFalse(aofaFirstName.isEditingAllowed());

        AccountOnFileAttribute aofaSurname = accountOnFile.getAccountOnFileAttributes().get(2);
        assertFalse(aofaSurname.isEditingAllowed());

        AccountOnFileAttribute aofaCardholderName = accountOnFile.getAccountOnFileAttributes().get(3);
        assertFalse(aofaCardholderName.isEditingAllowed());

        AccountOnFileAttribute aofaCardNumber = accountOnFile.getAccountOnFileAttributes().get(4);
        assertFalse(aofaCardNumber.isEditingAllowed());

        AccountOnFileAttribute aofaCvv = accountOnFile.getAccountOnFileAttributes().get(5);
        assertTrue(aofaCvv.isEditingAllowed());

        AccountOnFileAttribute aofaExpiryDate = accountOnFile.getAccountOnFileAttributes().get(6);
        assertTrue(aofaExpiryDate.isEditingAllowed());
    }

    @Test
    public void testMaskingValue() {
        assertEquals("4111 11XX XXXX 1111 ", accountOnFile.getMaskedValue("alias"));
    }
}

