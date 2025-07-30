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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidationRuleFixedListJavaTest {

    private PaymentRequest paymentRequest;
    private final String fieldId = "Country";

    @Before
    public void setup() {
        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidValuesInList() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE", "FR", "NL"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        for (String value : allowedValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Value '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidValuesNotInList() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE", "FR", "NL"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        String[] invalidValues = {"CA", "AU", "JP", "ES", "IT", "XX", "123", ""};

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testEmptyStringInList() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "", "GB"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        paymentRequest.setValue(fieldId, "");
        assertTrue("Empty string should be valid when in allowed list",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testSingleValueList() {
        List<String> allowedValues = new ArrayList<>(List.of("ONLY_VALUE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        paymentRequest.setValue(fieldId, "ONLY_VALUE");
        assertTrue("Single allowed value should be valid",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "OTHER_VALUE");
        assertFalse("Value not in single-item list should be invalid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testCaseSensitivity() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        paymentRequest.setValue(fieldId, "us");
        assertFalse("Lowercase value should be invalid when uppercase in list",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "Us");
        assertFalse("Mixed case value should be invalid when uppercase in list",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testWhitespaceValues() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", " GB ", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        paymentRequest.setValue(fieldId, " GB ");
        assertTrue("Value with spaces should be valid when exactly matching list item",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "GB");
        assertFalse("Trimmed value should be invalid when list contains spaced version",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test(expected = InvalidParameterException.class)
    public void testEmptyListThrowsException() {
        List<String> emptyList = new ArrayList<>();
        new ValidationRuleFixedList(emptyList);
    }

    @Test
    public void testNonExistentField() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testValidationRuleType() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        assertSame("ValidationRuleFixedList should have FIXEDLIST type", validationRule.getType(), ValidationType.FIXEDLIST);
    }

    @Test
    public void testMessageId() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        assertEquals("ValidationRuleFixedList should have correct messageId", "fixedList", validationRule.getMessageId());
    }

    @Test
    public void testListValuesImmutability() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE"));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        // Modify original list
        allowedValues.add("FR");

        // The validation rule should not be affected
        paymentRequest.setValue(fieldId, "FR");
        assertFalse("Added value should not be valid (list should be immutable)",
            validationRule.validate(paymentRequest, fieldId));

        // Original values should still work
        paymentRequest.setValue(fieldId, "US");
        assertTrue("Original values should still be valid",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testListValuesProperty() {
        List<String> allowedValues = new ArrayList<>(Arrays.asList("US", "GB", "DE", null));
        ValidationRuleFixedList validationRule = new ValidationRuleFixedList(allowedValues);

        // Verify the listValues property contains the expected values
        assertTrue("List should contain US", validationRule.getListValues().contains("US"));
        assertTrue("List should contain GB", validationRule.getListValues().contains("GB"));
        assertTrue("List should contain DE", validationRule.getListValues().contains("DE"));
        assertTrue("List should contain null", validationRule.getListValues().contains(null));
        assertFalse("List should not contain FR", validationRule.getListValues().contains("FR"));

        // Verify list size
        assertEquals("List should have 4 items", 4, validationRule.getListValues().size());
    }
}