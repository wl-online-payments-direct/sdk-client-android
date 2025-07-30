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

public class ValidationRuleRegexJavaTest {

    private PaymentRequest paymentRequest;
    private final String fieldId = "CompanyName";

    @Before
    public void setup() {
        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testDigitsOnlyRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("\\d+");

        String[] validValues = {"123", "0", "999999", "1234567890"};
        String[] invalidValues = {"abc", "12a", "a123", "12.3", "", "12 34"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Digits-only value '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Non-digits value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testLettersOnlyRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("[a-zA-Z]+");

        String[] validValues = {"abc", "ABC", "AbC", "hello", "WORLD"};
        String[] invalidValues = {"123", "abc123", "a1b", "", "hello world", "hello-world"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Letters-only value '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Non-letters value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testEmailRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "test123@test-domain.org"
        };

        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "test@",
            "test.example.com",
            "test@example"
        };

        for (String email : validEmails) {
            paymentRequest.setValue(fieldId, email);
            assertTrue("Valid email '" + email + "' should match regex",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String email : invalidEmails) {
            paymentRequest.setValue(fieldId, email);
            assertFalse("Invalid email '" + email + "' should not match regex",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testPhoneNumberRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^\\+?[1-9]\\d{4,14}$");

        String[] validPhones = {
            "+1234567890",
            "1234567890",
            "+123456789012345"
        };

        String[] invalidPhones = {
            "0123456789", // starts with 0
            "+0123456789", // starts with +0
            "123", // too short
            "+12345678901234567", // too long
            "123-456-7890", // contains dashes
            "+1 234 567 890" // contains spaces
        };

        for (String phone : validPhones) {
            paymentRequest.setValue(fieldId, phone);
            assertTrue("Valid phone '" + phone + "' should match regex",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String phone : invalidPhones) {
            paymentRequest.setValue(fieldId, phone);
            assertFalse("Invalid phone '" + phone + "' should not match regex",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testExactLengthRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^.{5}$");

        String[] validValues = {"12345", "abcde", "1a2b3", "     ", "!@#$%"};
        String[] invalidValues = {"1234", "123456", "", "12345678901"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("5-character value '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Non-5-character value '" + value + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testOptionalGroupsRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^\\d{4}(-\\d{4})?$");

        String[] validValues = {"1234", "1234-5678"};
        String[] invalidValues = {"123", "12345", "1234-567", "1234-56789", "abcd", "1234-abcd"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Value '" + value + "' should match optional group regex",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Value '" + value + "' should not match optional group regex",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testCaseInsensitiveRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("(?i)^(yes|no)$");

        String[] validValues = {"yes", "YES", "Yes", "YeS", "no", "NO", "No", "nO"};
        String[] invalidValues = {"maybe", "y", "n", "true", "false", ""};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Case-insensitive value '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Invalid value '" + value + "' should not match",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testComplexRegex() {
        // Credit card expiry date MM/YY format
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^(0[1-9]|1[0-2])/([0-9]{2})$");

        String[] validValues = {"01/23", "12/25", "06/30"};
        String[] invalidValues = {"00/23", "13/25", "1/23", "01/2025", "01-23", "01/ab"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Valid expiry date '" + value + "' should match regex",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Invalid expiry date '" + value + "' should not match regex",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testUnicodeRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^[\\p{L}]+$"); // Unicode letters

        String[] validValues = {"café", "naïve", "résumé", "北京", "москва"};
        String[] invalidValues = {"café123", "test@test", "hello world", "123", ""};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Unicode letters value '$value' should be valid", validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Non-letters value '$value' should be invalid", validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testEmptyStringWithEmptyRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("");

        paymentRequest.setValue(fieldId, "");
        assertTrue("Empty string should match empty regex",
            validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "a");
        assertFalse("Non-empty string should not match empty regex",
            validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testMatchAnyRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex(".*");

        String[] anyValues = {"", "abc", "123", "!@#", "hello world", "🎉"};

        for (String value : anyValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Any value '" + value + "' should match .* regex",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testNonExistentField() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("\\d+");

        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testMultilineRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("(?s).*test.*"); // DOTALL flag

        paymentRequest.setValue(fieldId, "line1\ntest\nline3");
        assertTrue("Multiline string with test should match", validationRule.validate(paymentRequest, fieldId));

        paymentRequest.setValue(fieldId, "line1\nother\nline3");
        assertFalse("Multiline string without test should not match", validationRule.validate(paymentRequest, fieldId));
    }

    @Test
    public void testEscapedCharactersRegex() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("^\\$\\d+\\.\\d{2}$"); // Price format $XX.XX

        String[] validValues = {"$0.99", "$12.34", "$123.45"};
        String[] invalidValues = {"0.99", "$12", "$12.3", "$12.345", "12.34"};

        for (String value : validValues) {
            paymentRequest.setValue(fieldId, value);
            assertTrue("Price format '" + value + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }

        for (String value : invalidValues) {
            paymentRequest.setValue(fieldId, value);
            assertFalse("Invalid price format '" + value + "' should not match",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testValidationRuleType() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("\\d+");
        assertSame("ValidationRuleRegex should have REGULAREXPRESSION type", validationRule.getType(), ValidationType.REGULAREXPRESSION);
    }

    @Test
    public void testMessageId() {
        ValidationRuleRegex validationRule = new ValidationRuleRegex("\\d+");
        TestCase.assertEquals("ValidationRuleRegex should have correct messageId", "regularExpression", validationRule.getMessageId());
    }

    @Test
    public void testRegexProperty() {
        String regex = "^[a-zA-Z0-9]+$";
        ValidationRuleRegex validationRule = new ValidationRuleRegex(regex);

        assertEquals("Regex property should return the original regex", regex, validationRule.getPattern());
    }
}