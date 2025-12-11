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
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

public class ValidationRuleLengthJavaTest {

    private PaymentRequest paymentRequest;
    private final String fieldId = "PostalCode";

    @Before
    public void setup() {
        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidLengthWithinRange() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);

        String[] validValues = {"abc", "test", "12345", "1234567890"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Value '" + value + "' (length " + value.length() + ") should be valid for range 3-10",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidLengthTooShort() {
        ValidationRuleLength validationRule = new ValidationRuleLength(5, 10);

        String[] shortValues = {"", "a", "ab", "abc", "abcd"};

        for (String value : shortValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' (length " + value.length() + ") should be invalid for min length 5",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidLengthTooLong() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 8);

        String[] longValues = {"123456789", "1234567890", "12345678901"};

        for (String value : longValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' (length " + value.length() + ") should be invalid for max length 8",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testExactBoundaryValues() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 8);

        // Test exact min length
        paymentRequest.setValue(fieldId, "abc");
        assertTrue("Value with exact min length should be valid",
            validationRule.validate(paymentRequest, fieldId));

        // Test exact max length
        paymentRequest.setValue(fieldId, "12345678");
        assertTrue("Value with exact max length should be valid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testZeroMinLength() {
        ValidationRuleLength validationRule = new ValidationRuleLength(0, 5);

        paymentRequest.setValue(fieldId, "");
        assertTrue("Empty string should be valid with min length 0",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "12345");
        assertTrue("Max length value should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "123456");
        assertFalse("Value exceeding max length should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testSameLengthMinMax() {
        ValidationRuleLength validationRule = new ValidationRuleLength(5, 5);

        paymentRequest.setValue(fieldId, "12345");
        assertTrue("Value with exact required length should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "1234");
        assertFalse("Value shorter than required should be invalid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "123456");
        assertFalse("Value longer than required should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testNonExistentField() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);

        assertFalse("Non-existent field should be invalid when min length > 0",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testNonExistentFieldWithZeroMinLength() {
        ValidationRuleLength validationRule = new ValidationRuleLength(0, 10);

        assertTrue("Non-existent field should be valid when min length is 0",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test(expected = InvalidParameterException.class)
    public void testInvalidParameterNegativeMinLength() {
        new ValidationRuleLength(-1, 5);
    }

    @Test(expected = InvalidParameterException.class)
    public void testInvalidParameterMaxLessThanMin() {
        new ValidationRuleLength(5, 3);
    }

    @Test
    public void testValidationRuleType() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);
        assertSame("ValidationRuleLength should have LENGTH type", validationRule.getType(), ValidationType.LENGTH);
    }

    @Test
    public void testMessageId() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);
        TestCase.assertEquals("ValidationRuleLength should have correct messageId", "length", validationRule.getMessageId());
    }

    @Test
    public void testGetterMethods() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);

        assertEquals("Min length getter should return correct value", 3, validationRule.getMinLength());
        assertEquals("Max length getter should return correct value", 10, validationRule.getMaxLength());
    }

    @Test
    public void testUnicodeCharacters() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);

        String[] unicodeValues = {"🎉🎊", "café", "naïve", "Šal", "résumé", "中文测试"};

        for (String value : unicodeValues) {
            paymentRequest.setValue(fieldId, value);
            boolean isValid = validationRule.validate(paymentRequest, fieldId);
            assertTrue("Unicode value '" + value + "' (length " + value.length() + ") validation should match expected", isValid);
        }
    }

    @Test
    public void testWhitespaceCharacters() {
        ValidationRuleLength validationRule = new ValidationRuleLength(3, 10);

        paymentRequest.setValue(fieldId, "   ");
        assertTrue("Spaces should count towards length",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "\t\n\r");
        assertTrue("Whitespace characters should count towards length",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "a b c");
        assertTrue("Mixed content with spaces should be valid",
            validationRule.validate(paymentRequest, fieldId));
    }
}