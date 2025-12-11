/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Before;
import org.junit.Test;

public class ValidationRuleIBANJavaTest {

    private ValidationRuleIBAN validationRule;
    private PaymentRequest paymentRequest;
    private final String fieldId = "Iban";

    @Before
    public void setup() {
        validationRule = new ValidationRuleIBAN();

        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidIBANNumbers() {
        String[] validIbans = {
            "BE68539007547034", // Belgian IBAN
            "NL91ABNA0417164300", // Dutch IBAN
            "AT611904300234573201", // Austrian IBAN
            "CH9300762011623852957", // Swiss IBAN
            "GB82WEST12345698765432", // UK IBAN
            "DE89370400440532013000", // German IBAN
            "AD1200012030200359100100", // Andorra IBAN
            "ES9121000418450200051332", // Spanish IBAN
            "GR1601101250000000012300695", // Greek IBAN
            "FR1420041010050500013M02606", // French IBAN
            "IT60X0542811101000000123456", // Italian IBAN
            "MT84MALT011000012345MTLCAST001S" // Malta IBAN
        };

        for (String iban : validIbans) {
            paymentRequest.setValue(fieldId, iban);
            assertTrue("IBAN '" + iban + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidIBANNumbers() {
        String[] invalidIbans = {
            "GB82WEST12345698765433", // Invalid checksum
            "DE89370400440532013001", // Invalid checksum
            "FR1420041010050500013M02607", // Invalid checksum
            "IT60X0542811101000000123457", // Invalid checksum
            "ES9121000418450200051333", // Invalid checksum
            "NL91ABNA0417164301", // Invalid checksum
            "BE68539007547035", // Invalid checksum
            "AT611904300234573202", // Invalid checksum
            "CH9300762011623852958", // Invalid checksum
            "GR1601101250000000012300696" // Invalid checksum
        };

        for (String iban : invalidIbans) {
            paymentRequest.setValue(fieldId, iban);
            assertFalse("IBAN '" + iban + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidIBANFormat() {
        String[] invalidFormats = {
            "gb82west12345698765432", // Lowercase letters
            "GB82WEST123456987654321", // Too long
            "GB82WEST1234569876543", // Too short
            "G82WEST12345698765432", // Missing country letter
            "GB2WEST12345698765432", // Missing check digit
            "GBWEST12345698765432", // Missing check digits
            "GB82WE5T12345698765432", // Invalid bank code format
            "1234567890123456789012", // All numbers
            "ABCDEFGHIJKLMNOPQRSTUV", // All letters
            "GB82 WEST 1234 5698 7654 32", // Spaces
            "GB82-WEST-1234-5698-7654-32" // Dashes
        };

        for (String iban : invalidFormats) {
            paymentRequest.setValue(fieldId, iban);
            assertFalse("IBAN '" + iban + "' should be invalid format",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testIBANWithWhitespace() {
        // Test that whitespace is properly trimmed
        String ibanWithSpaces = " GB82WEST12345698765432 ";
        paymentRequest.setValue(fieldId, ibanWithSpaces);
        assertTrue("IBAN with whitespace should be valid after trimming",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testEmptyValue() {
        paymentRequest.setValue(fieldId, "");
        assertFalse("Empty IBAN should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testNonExistentField() {
        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testValidationRuleType() {
        assertSame("ValidationRuleIBAN should have IBAN type", validationRule.getType(), ValidationType.IBAN);
    }

    @Test
    public void testMessageId() {
        assertEquals("ValidationRuleIBAN should have correct messageId", "iban", validationRule.getMessageId());
    }

    @Test
    public void testMaximumLengthIBAN() {
        // Test maximum length IBAN (34 characters)
        String longValidIban = "MT84MALT011000012345MTLCAST001S"; // Malta IBAN
        paymentRequest.setValue(fieldId, longValidIban);
        assertTrue("Long valid IBAN should be accepted", validationRule.validate(paymentRequest, fieldId));
    }
}