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

public class ValidationRuleLuhnJavaTest {

    private ValidationRuleLuhn validationRule;
    private PaymentRequest paymentRequest;
    private final String fieldId = "cardNumber";

    @Before
    public void setup() {
        validationRule = new ValidationRuleLuhn();

        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductVisa.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidCreditCardNumbers() {
        String[] validCardNumbers = {
            "4111111111111111", // Visa test card
            "4000000000000002", // Visa test card
            "5555555555554444", // MasterCard test card
            "5105105105105100", // MasterCard test card
            "378282246310005",  // American Express test card
            "371449635398431",  // American Express test card
            "30569309025904",   // Diners Club test card
            "38520000023237",   // Diners Club test card
            "6011111111111117", // Discover test card
            "6011000990139424"  // Discover test card
        };

        for (String cardNumber : validCardNumbers) {
            paymentRequest.setValue(fieldId, cardNumber);
            assertTrue("Card number '" + cardNumber + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testValidCreditCardNumbersWithSpaces() {
        String[] validCardNumbersWithSpaces = {
            "4111 1111 1111 1111", // Visa test card with spaces
            "5555 5555 5555 4444", // MasterCard test card with spaces
            "3782 822463 10005",   // American Express test card with spaces
            "6011 1111 1111 1117"  // Discover test card with spaces
        };

        for (String cardNumber : validCardNumbersWithSpaces) {
            paymentRequest.setValue(fieldId, cardNumber);
            assertTrue("Card number '" + cardNumber + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidCreditCardNumbers() {
        String[] invalidCardNumbers = {
            "4111111111111112",
            "4000000000000001",
            "5555555555554445",
            "378282246310006",
            "1234567890123456",
            "4444444444444444",
            "9999999999999999"
        };

        for (String cardNumber : invalidCardNumbers) {
            paymentRequest.setValue(fieldId, cardNumber);
            assertFalse("Card number '" + cardNumber + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testTooShortNumbers() {
        String[] shortNumbers = {
            "411111111",      // 9 digits
            "41111111111",    // 11 digits
            "123456789012"    // 12 digits but invalid
        };

        for (String cardNumber : shortNumbers) {
            paymentRequest.setValue(fieldId, cardNumber);
            assertFalse("Card number '" + cardNumber + "' should be invalid (too short or invalid)",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testNonNumericCharacters() {
        String[] invalidInputs = {
            "411111111111111a",    // Contains letter
            "4111-1111-1111-1111", // Contains dashes
            "4111.1111.1111.1111", // Contains dots
            "abc123def456ghi7",    // Mixed letters and numbers
            "4111 1111 1111 111a"  // Space and letter
        };

        for (String input : invalidInputs) {
            paymentRequest.setValue(fieldId, input);
            assertFalse("Input '" + input + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testEmptyValue() {
        paymentRequest.setValue(fieldId, "");
        assertFalse("Empty card number should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testNonExistentField() {
        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testValidationRuleType() {
        assertSame("ValidationRuleLuhn should have LUHN type", validationRule.getType(), ValidationType.LUHN);
    }

    @Test
    public void testMessageId() {
        assertEquals("ValidationRuleLuhn should have correct messageId", "luhn", validationRule.getMessageId());
    }
}