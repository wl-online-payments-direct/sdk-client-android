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

public class ValidationRuleRangeJavaTest {

    private PaymentRequest paymentRequest;
    private final String fieldId = "Range";

    @Before
    public void setup() {
        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidValuesWithinRange() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] validValues = {"11", "50", "99", "25", "75"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Value '" + value + "' should be valid for range 10-100",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidValuesBelowRange() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] belowRangeValues = {"5", "0", "-5", "10"}; // Note: 10 is exclusive minimum

        for (String value : belowRangeValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' should be invalid (below range)",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidValuesAboveRange() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] aboveRangeValues = {"100", "101", "150", "1000"}; // Note: 100 is exclusive maximum

        for (String value : aboveRangeValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' should be invalid (above range)",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testBoundaryValues() {
        ValidationRuleRange validationRule = new ValidationRuleRange(0, 10);

        // Test exclusive boundaries
        paymentRequest.setValue(fieldId, "0");
        assertFalse("Min boundary value should be invalid (exclusive)",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "10");
        assertFalse("Max boundary value should be invalid (exclusive)",
            validationRule.validate(paymentRequest, fieldId));

        // Test values just inside boundaries
        paymentRequest.setValue(fieldId, "1");
        assertTrue("Value just above min should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "9");
        assertTrue("Value just below max should be valid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testNegativeRange() {
        ValidationRuleRange validationRule = new ValidationRuleRange(-100, -10);

        paymentRequest.setValue(fieldId, "-50");
        assertTrue("Negative value within range should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "-100");
        assertFalse("Min boundary should be invalid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "-10");
        assertFalse("Max boundary should be invalid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "0");
        assertFalse("Positive value should be invalid in negative range",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testCrossZeroRange() {
        ValidationRuleRange validationRule = new ValidationRuleRange(-10, 10);

        String[] validValues = {"-5", "0", "5", "1", "-1"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Value '" + value + "' should be valid for range -10 to 10",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testNonNumericValues() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] nonNumericValues = {"abc", "12.5", "1a2", "", "fifty", "12 34", " 50 "};

        for (String value : nonNumericValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Non-numeric value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testDecimalValues() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] decimalValues = {"50.5", "12.34", "99.99", "10.1"};

        for (String value : decimalValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Decimal value '" + value + "' should be invalid (not integer)",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testLeadingZeros() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        paymentRequest.setValue(fieldId, "050");
        assertTrue("Value with leading zeros should be valid if numeric value is in range",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "005");
        assertFalse("Value with leading zeros should be invalid if numeric value is out of range",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testVeryLargeNumbers() {
        ValidationRuleRange validationRule = new ValidationRuleRange(1000000, 2000000);

        paymentRequest.setValue(fieldId, "1500000");
        assertTrue("Large number within range should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "2147483647"); // Max int value
        assertFalse("Max int value should be invalid if outside range",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testIntegerOverflow() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        String[] overflowValues = {"2147483648", "9999999999999999999", "-2147483649"};

        for (String value : overflowValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Integer overflow value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testEmptyString() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        paymentRequest.setValue(fieldId, "");
        assertFalse("Empty string should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testNonExistentField() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);

        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testValidationRuleType() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);
        assertSame("ValidationRuleRange should have RANGE type", validationRule.getType(), ValidationType.RANGE);
    }

    @Test
    public void testMessageId() {
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 100);
        TestCase.assertEquals("ValidationRuleRange should have correct messageId", "range", validationRule.getMessageId());
    }

    @Test
    public void testGetterMethods() {
        ValidationRuleRange validationRule = new ValidationRuleRange(25, 75);

        assertEquals("Min value getter should return correct value", 25, validationRule.getMinValue());
        assertEquals("Max value getter should return correct value", 75, validationRule.getMaxValue());
    }

    @Test
    public void testSingleValueRange() {
        // Edge case: range where min and max are consecutive integers
        ValidationRuleRange validationRule = new ValidationRuleRange(10, 12);

        paymentRequest.setValue(fieldId, "11");
        assertTrue("Only valid value in narrow range should be accepted",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "10");
        assertFalse("Min boundary should be invalid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "12");
        assertFalse("Max boundary should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testImpossibleRange() {
        // Edge case: range where min and max are equal (no valid values)
        ValidationRuleRange validationRule = new ValidationRuleRange(50, 50);

        String[] testValues = {"49", "50", "51"};

        for (String value : testValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("No value should be valid in impossible range",
                validationRule.validate(paymentRequest, fieldId));
        }
    }
}