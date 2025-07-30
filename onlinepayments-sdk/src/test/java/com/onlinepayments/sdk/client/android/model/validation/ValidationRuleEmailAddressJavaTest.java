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

public class ValidationRuleEmailAddressJavaTest {

    private ValidationRuleEmailAddress validationRule;
    private PaymentRequest paymentRequest;
    private final String fieldId = "Email";

    @Before
    public void setup() {
        validationRule = new ValidationRuleEmailAddress();

        PaymentProduct paymentProduct = GsonHelperJava.fromResourceJson(
            "paymentProductForValidators.json",
            PaymentProduct.class
        );
        paymentRequest = new PaymentRequest(paymentProduct);
    }

    @Test
    public void testValidEmailAddresses() {
        String[] validEmails = {
            "simple@example.com",
            "very.common@example.com",
            "user.name@domain.co.uk",
            "user-name@domain.co.uk",
            "user_name@example.org",
            "user+tag@example.com",
            "user123@sub.domain.com",
            "user@example.travel",
            "user@example.museum",
            "user@example.co",
            "u@a.io",
            "firstname.o'lastname@example.com",
            "user%example@example.org"
        };

        for (String email : validEmails) {
            paymentRequest.setValue(fieldId, email);
            assertTrue("Email '" + email + "' should be valid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testInvalidEmailAddresses() {
        String[] invalidEmails = {
            "",
            "plainaddress",
            "@no-local-part.com",
            "Outlook Contact <outlook@example.com>",
            "user.name@.com",
            "user.@example.com",
            ".user@example.com",
            "user..name@example.com",
            "user@example..com",
            "user@.example.com",
            "user@example.com.",
            "user@example",
            "user@-example.com",
            "user@example-.com",
            "user@exam_ple.com",
            "test..test@example.com",
            "user@123.123.123.123",
            "user@[300.300.300.300]",
            "test email@example.com",
            "user@exam ple.com",
            "user@.com",
            "user@com.",
            "user@@example.com",
            "\"quoted\"@example.com",
            "user\n@example.com",
        };

        for (String email : invalidEmails) {
            paymentRequest.setValue(fieldId, email);
            assertFalse("Email '" + email + "' should be invalid",
                validationRule.validate(paymentRequest, fieldId));
        }
    }

    @Test
    public void testNonExistentField() {
        assertFalse("Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField"));
    }

    @Test
    public void testValidationRuleType() {
        assertSame("ValidationRuleEmailAddress should have EMAILADDRESS type", validationRule.getType(), ValidationType.EMAILADDRESS);
    }

    @Test
    public void testMessageId() {
        assertEquals("ValidationRuleEmailAddress should have correct messageId", "emailAddress", validationRule.getMessageId());
    }
}