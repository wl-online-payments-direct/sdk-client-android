/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddress;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDate;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedList;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBAN;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhn;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRange;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegex;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleTermsAndConditions;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

/**
 * Junit Test class which tests validation functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestValidationJavaTest {
    private final PaymentProduct paymentProductVisa = GsonHelperJava.fromResourceJson(
        "paymentProductVisa.json",
        PaymentProduct.class
    );

    private final ArrayList<String> listEntries = new ArrayList<>();

    private final Integer minLength = 0;
    private final Integer maxLength = 10;

    private final String regex = "\\d{2}[a-z]{2}[A-Z]{3}";

    public PaymentRequestValidationJavaTest() {

        // Fill the test listEntries
        listEntries.add("1");
        listEntries.add("2");
        listEntries.add("3");
    }

    // Test emailaddress validator
    @Test
    public void testValidEmailAddress() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String emailAddressValid = "aa@bb.com";
        paymentRequest.setValue("emailAddress", emailAddressValid);
        ValidationRuleEmailAddress rule = new ValidationRuleEmailAddress();
        assertTrue(rule.validate(paymentRequest, "emailAddress"));
    }

    @Test
    public void testInvalidEmailAddress() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String emailAddressInvalid = "aa2bb.com";
        paymentRequest.setValue("emailAddress", emailAddressInvalid);
        ValidationRuleEmailAddress rule = new ValidationRuleEmailAddress();
        assertFalse(rule.validate(paymentRequest, "emailAddress"));
    }

    // Test expirationdate validator
    @Test
    public void testValidExpirationDate() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String expirationDateValid = "112028";
        paymentRequest.setValue("expirationDate", expirationDateValid);
        ValidationRuleExpirationDate rule = new ValidationRuleExpirationDate();
        assertTrue(rule.validate(paymentRequest, "expirationDate"));
    }

    @Test
    public void testInvalidExpirationDate() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String expirationDateInvalid = "0000";
        paymentRequest.setValue("expirationDate", expirationDateInvalid);
        ValidationRuleExpirationDate rule = new ValidationRuleExpirationDate();
        assertFalse(rule.validate(paymentRequest, "expirationDate"));
    }

    // Test fixed list validator
    @Test
    public void testValidFixedList() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validListOption = "1";
        paymentRequest.setValue("fixedList", validListOption);
        ValidationRuleFixedList rule = new ValidationRuleFixedList(listEntries);
        assertTrue(rule.validate(paymentRequest, "fixedList"));
    }

    @Test
    public void testInvalidFixedList() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidListOption = "a";
        paymentRequest.setValue("fixedList", invalidListOption);
        ValidationRuleFixedList rule = new ValidationRuleFixedList(listEntries);
        assertFalse(rule.validate(paymentRequest, "fixedList"));
    }

    // Test IBAN validator
    @Test
    public void testValidIBAN() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validIBAN = "GB33BUKB20201555555555";
        paymentRequest.setValue("IBAN", validIBAN);
        ValidationRuleIBAN rule = new ValidationRuleIBAN();
        assertTrue(rule.validate(paymentRequest, "IBAN"));
    }

    @Test
    public void testInvalidIBAN() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidIBAN = "GB94BARC20201530093459";
        paymentRequest.setValue("IBAN", invalidIBAN);
        ValidationRuleIBAN rule = new ValidationRuleIBAN();
        assertFalse(rule.validate(paymentRequest, "IBAN"));
    }

    // Test length validator
    @Test
    public void testValidLength() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validLength = "abc";
        paymentRequest.setValue("length", validLength);
        ValidationRuleLength rule = new ValidationRuleLength(minLength, maxLength);
        assertTrue(rule.validate(paymentRequest, "length"));
    }

    @Test
    public void testValidZeroLength() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        paymentRequest.setValue("length", "");
        ValidationRuleLength rule = new ValidationRuleLength(minLength, maxLength);
        assertTrue(rule.validate(paymentRequest, "length"));
    }

    @Test
    public void testValidFieldNullLength() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        ValidationRuleLength rule = new ValidationRuleLength(minLength, maxLength);
        assertTrue(rule.validate(paymentRequest, "length"));
    }

    @Test
    public void testInvalidLength() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidLength = "abcabcabcabcabc";
        paymentRequest.setValue("length", invalidLength);
        ValidationRuleLength rule = new ValidationRuleLength(minLength, maxLength);
        assertFalse(rule.validate(paymentRequest, "length"));
    }

    // Test luhn validator
    @Test
    public void testValidLuhn() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validLuhnCheck = "4242424242424242";
        paymentRequest.setValue("luhn", validLuhnCheck);
        ValidationRuleLuhn rule = new ValidationRuleLuhn();
        assertTrue(rule.validate(paymentRequest, "luhn"));
    }

    @Test
    public void testInvalidLuhn() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidLuhnCheck = "1142424242424242";
        paymentRequest.setValue("luhn", invalidLuhnCheck);
        ValidationRuleLuhn rule = new ValidationRuleLuhn();
        assertFalse(rule.validate(paymentRequest, "luhn"));
    }

    // Test range validator
    @Test
    public void testValidRange() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validRange = "1";
        paymentRequest.setValue("range", validRange);
        ValidationRuleRange rule = new ValidationRuleRange(minLength, maxLength);
        assertTrue(rule.validate(paymentRequest, "range"));
    }

    @Test
    public void testInvalidRange() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidRange = "150";
        paymentRequest.setValue("range", invalidRange);
        ValidationRuleRange rule = new ValidationRuleRange(minLength, maxLength);
        assertFalse(rule.validate(paymentRequest, "range"));
    }

    // Test regex validator
    @Test
    public void testValidRegex() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String validRegex = "11atAAB";
        paymentRequest.setValue("regex", validRegex);
        ValidationRuleRegex rule = new ValidationRuleRegex(regex);
        assertTrue(rule.validate(paymentRequest, "regex"));
    }

    @Test
    public void testInValidRegex() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        String invalidRegex = "abcabcabc";
        paymentRequest.setValue("regex", invalidRegex);
        ValidationRuleRegex rule = new ValidationRuleRegex(regex);
        assertFalse(rule.validate(paymentRequest, "regex"));
    }

    // Test terms and conditions validator
    @Test
    public void testValidTermsAndConditions() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        paymentRequest.setValue("termsAndConditions", Boolean.TRUE.toString());
        ValidationRuleTermsAndConditions rule = new ValidationRuleTermsAndConditions();
        assertTrue(rule.validate(paymentRequest, "termsAndConditions"));
    }

    @Test
    public void testInValidTermsAndConditions() {
        PaymentRequest paymentRequest = new TestPaymentRequest(paymentProductVisa);
        paymentRequest.setValue("termsAndConditions", "test");
        ValidationRuleTermsAndConditions rule = new ValidationRuleTermsAndConditions();
        assertFalse(rule.validate(paymentRequest, "termsAndConditions"));
    }

    private static final class TestPaymentRequest extends PaymentRequest {

        public TestPaymentRequest(PaymentProduct paymentProduct) {
            super(paymentProduct);
        }

        @NonNull
        @Override
        public String getUnmaskedValue(
            @NonNull String paymentProductFieldId,
            @NonNull String value
        ) {
            // no actual payment product fields are available
            return value;
        }
    }
}
