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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.model.paymentproduct.Tooltip;
import com.onlinepayments.sdk.client.android.model.validation.AbstractValidationRule;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Junit Test class which tests PaymentRequest
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestJavaTest {

    private final PaymentProduct paymentProductVisa = GsonHelperJava.fromResourceJson(
            "paymentProductVisa.json",
            PaymentProduct.class
    );
    private final PaymentProduct paymentProductInvoice = GsonHelperJava.fromResourceJson(
            "paymentProductInVoice.json",
            PaymentProduct.class
    );
    private final PaymentProduct paymentProductPayPal = GsonHelperJava.fromResourceJson(
            "paymentProductPayPal.json",
            PaymentProduct.class
    );

    private final AccountOnFile accountOnFileVisa = GsonHelperJava.fromResourceJson(
            "accountOnFileVisa.json",
            AccountOnFile.class
    );

    private final PaymentRequest parsedPaymentRequest = GsonHelperJava.fromResourceJson(
            "paymentRequest.json",
            PaymentRequest.class
    );

    public static Map<String, String> allValidValuesVisa = new HashMap<>();

    static {
        allValidValuesVisa.put("cardNumber", "4012000033330026");
        allValidValuesVisa.put("expiryDate", "122030");
        allValidValuesVisa.put("cvv", "123");
        allValidValuesVisa.put("cardholderName", "Test");
    }

    public static Map<String, String> allValidValuesInVoice = new HashMap<>();

    static {
        allValidValuesInVoice.put("stateCode", "abcdefgh");
        allValidValuesInVoice.put("city", "Amsterdam");
        allValidValuesInVoice.put("street", "De Dam");
    }

    public static Map<String, String> invalidCCNVisa = new HashMap<>();

    static {
        invalidCCNVisa.put("cardNumber", "401200");
        invalidCCNVisa.put("expiryDate", "1230");
        invalidCCNVisa.put("cvv", "123");
    }

    public static Map<String, String> invalidStateInVoice = new HashMap<>();

    static {
        invalidStateInVoice.put("stateCode", "abcdefghijklmnopqrstuvwxyz");
        invalidStateInVoice.put("city", "Amsterdam");
        invalidStateInVoice.put("street", "De dam");
    }

    public static Map<String, String> missingCCNVisa = new HashMap<>();

    static {
        missingCCNVisa.put("expiryDate", "1230");
        missingCCNVisa.put("cvv", "123");
    }

    public static Map<String, String> missingCityInVoice = new HashMap<>();

    static {
        missingCityInVoice.put("stateCode", "abcdefgh");
        missingCityInVoice.put("street", "De Dam");
    }

    @Test
    public void testDeserialization() {
        PaymentRequest actual = parsedPaymentRequest;

        assertTrue("Mismatch in tokenize for paymentRequest: ", parsedPaymentRequest.getTokenize());
        testAccountOnFileEquality(
                Objects.requireNonNull(actual.getAccountOnFile()),
                accountOnFileVisa
        );

        testPaymentProductEquality(
                Objects.requireNonNull(actual.getPaymentProduct()),
                paymentProductVisa
        );

        assertEquals(
                "Mismatch in fieldsValues for paymentRequest",
                allValidValuesVisa,
                actual.getValues()
        );
    }

    @Test
    public void testConstructors() {
        PaymentRequest paymentRequest = new PaymentRequest(paymentProductVisa);

        assertFalse(paymentRequest.getTokenize());
        assertNull(paymentRequest.getAccountOnFile());
        assertEquals(paymentProductVisa, paymentRequest.getPaymentProduct());

        paymentRequest = new PaymentRequest(paymentProductVisa, true);

        assertTrue(paymentRequest.getTokenize());
        assertNull(paymentRequest.getAccountOnFile());
        assertEquals(paymentProductVisa, paymentRequest.getPaymentProduct());

        paymentRequest = new PaymentRequest(paymentProductVisa, accountOnFileVisa);

        assertFalse(paymentRequest.getTokenize());
        assertEquals(accountOnFileVisa, paymentRequest.getAccountOnFile());
        assertEquals(paymentProductVisa, paymentRequest.getPaymentProduct());

        paymentRequest = new PaymentRequest(paymentProductVisa, accountOnFileVisa, true);

        assertTrue(paymentRequest.getTokenize());
        assertEquals(accountOnFileVisa, paymentRequest.getAccountOnFile());
        assertEquals(paymentProductVisa, paymentRequest.getPaymentProduct());

        paymentRequest = new PaymentRequest();

        assertFalse(paymentRequest.getTokenize());
        assertNull(paymentRequest.getAccountOnFile());
        assertNull(paymentRequest.getPaymentProduct());
    }

    @Test
    public void testValidateSucceedsForValidValuesVisa() {
        PaymentRequest validVisaValuesRequest = new PaymentRequest(paymentProductVisa);
        setValuesInRequest(allValidValuesVisa, validVisaValuesRequest);
        assertTrue(validVisaValuesRequest.validate().isEmpty());
    }

    @Test
    public void testValidateSucceedsForValidValuesInVoice() {
        PaymentRequest validInVoiceValuesRequest = new PaymentRequest(paymentProductInvoice);
        setValuesInRequest(allValidValuesInVoice, validInVoiceValuesRequest);
        assertTrue(validInVoiceValuesRequest.validate().isEmpty());
    }

    @Test
    public void testValidateSucceedsForNoValuesPayPal() {
        PaymentRequest validPayPalValuesRequest = new PaymentRequest(paymentProductPayPal);
        assertTrue(validPayPalValuesRequest.validate().isEmpty());
    }

    @Test
    public void testValidateFailsForInvalidCCNVisa() {
        PaymentRequest invalidVisaCCNRequest = new PaymentRequest(paymentProductVisa);
        setValuesInRequest(invalidCCNVisa, invalidVisaCCNRequest);
        assertFalse(invalidVisaCCNRequest.validate().isEmpty());
    }

    @Test
    public void testValidateFailsForInValidStateInVoice() {
        PaymentRequest invalidStateInVoiceRequest = new PaymentRequest(paymentProductInvoice);
        setValuesInRequest(invalidStateInVoice, invalidStateInVoiceRequest);
        assertFalse(invalidStateInVoiceRequest.validate().isEmpty());
    }

    @Test
    public void testValidateFailsForMissingRequiredValuesVisa() {
        PaymentRequest missingCCNVisaRequest = new PaymentRequest(paymentProductVisa);
        setValuesInRequest(missingCCNVisa, missingCCNVisaRequest);
        assertFalse(missingCCNVisaRequest.validate().isEmpty());
    }

    @Test
    public void testValidateFailsForMissingRequiredValuesInVoice() {
        PaymentRequest missingCityInVoiceRequest = new PaymentRequest(paymentProductInvoice);
        setValuesInRequest(missingCityInVoice, missingCityInVoiceRequest);
        assertFalse(missingCityInVoiceRequest.validate().isEmpty());
    }

    @Test
    public void testValidateSucceedsForAccountOnFileVisa() {
        PaymentRequest accountOnFileVisaRequest = new PaymentRequest(
                paymentProductVisa,
                accountOnFileVisa
        );
        assertTrue(accountOnFileVisaRequest.validate().isEmpty());
    }

    @Test
    public void testValidateSucceedsForAccountOnFileVisaWithChangedFields() {
        PaymentRequest accountOnFileVisaChangedValuesRequest = new PaymentRequest(
                paymentProductVisa,
                accountOnFileVisa
        );
        assertTrue(accountOnFileVisaChangedValuesRequest.validate().isEmpty());
    }

    private static void setValuesInRequest(Map<String, String> values, PaymentRequest request) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            request.setValue(entry.getKey(), entry.getValue());
        }
    }

    private void testPaymentProductFieldsEquality(
            List<PaymentProductField> actualFields,
            List<PaymentProductField> expectedFields
    ) {
        if (actualFields.size() != expectedFields.size()) {
            fail("Expected fields and actual fields are not the same size!");
        }
        // Compare fields
        int index = 0;
        for (PaymentProductField actualField : actualFields) {
            if (index < paymentProductVisa.getPaymentProductFields().size()) {
                PaymentProductField expectedField = expectedFields.get(index);
                System.out.println("expectedField = " + expectedField.getId());

                assertEquals(
                        "Mismatch in ID for field: " + actualField.getId(),
                        expectedField.getId(),
                        actualField.getId()
                );
                assertEquals(
                        "Mismatch in Type for field: " + actualField.getId(),
                        expectedField.getType(),
                        actualField.getType()
                );
                assertEquals(
                        "Mismatch in ValidationRules for field: " + actualField.getId(),
                        expectedField.getDataRestrictions()
                                .getValidationRules()
                                .stream()
                                .map((AbstractValidationRule::getType))
                                .collect(Collectors.toList()),
                        actualField.getDataRestrictions()
                                .getValidationRules()
                                .stream()
                                .map(AbstractValidationRule::getType)
                                .collect(Collectors.toList())
                );

                assertEquals(
                        "Mismatch in DisplayOrder for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getDisplayOrder(),
                        actualField.getDisplayHints().getDisplayOrder()
                );
                assertEquals(
                        "Mismatch in AlwaysShow for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getAlwaysShow(),
                        actualField.getDisplayHints().getAlwaysShow()
                );
                assertEquals(
                        "Mismatch in Mask for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getMask(),
                        actualField.getDisplayHints().getMask()
                );
                assertEquals(
                        "Mismatch in Label for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getLabel(),
                        actualField.getDisplayHints().getLabel()
                );
                assertEquals(
                        "Mismatch in FormElementType for field: " + actualField.getId(),
                        Objects.requireNonNull(
                                expectedField.getDisplayHints().getFormElement()).getType(),
                        Objects.requireNonNull(
                                actualField.getDisplayHints().getFormElement()).getType()
                );
                assertEquals(
                        "Mismatch in PlaceholderLabel for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getPlaceholderLabel(),
                        actualField.getDisplayHints().getPlaceholderLabel()
                );
                assertEquals(
                        "Mismatch in PreferredInputType for field: " + actualField.getId(),
                        expectedField.getDisplayHints().getPreferredInputType(),
                        actualField.getDisplayHints().getPreferredInputType()
                );
                Tooltip expectedTooltip = expectedField.getDisplayHints().getTooltip();
                Tooltip actualTooltip = actualField.getDisplayHints().getTooltip();
                if (expectedTooltip != null && actualTooltip != null) {
                    assertEquals(
                            "Mismatch in Tooltip.Label for field: " + actualField.getId(),
                            expectedTooltip.getLabel(),
                            actualTooltip.getLabel()
                    );
                } else if (expectedTooltip != null
                        || actualTooltip != null) {
                    fail("One of the tooltips is null for field: " + actualField.getId() + "\nactualTooltip: " + actualTooltip + "\nexpectedTooltip: " + expectedTooltip);
                }
                index++;
            } else {
                fail("Parsed paymentRequest has more fields than paymentProductVisa");
            }
        }
    }

    private void testPaymentProductEquality(PaymentProduct actual, PaymentProduct expected) {
        assertEquals(
                "Mismatch in ID for product: " + actual.getId(),
                expected.getId(),
                actual.getId()
        );
        assertEquals(
                "Mismatch in PaymentMethod for product: " + actual.getId(),
                expected.getPaymentMethod(),
                actual.getPaymentMethod()
        );
        assertEquals(
                "Mismatch in DisplayHintsList for product: " + actual.getId(),
                expected.getDisplayHintsList(),
                actual.getDisplayHintsList()
        );
        assertEquals(
                "Mismatch in PaymentProductGroup for product: " + actual.getId(),
                expected.getPaymentProductGroup(),
                actual.getPaymentProductGroup()
        );
        assertEquals(
                "Mismatch in Product302SpecificData for product: " + actual.getId(),
                expected.getPaymentProduct302SpecificData(),
                actual.getPaymentProduct302SpecificData()
        );
        assertEquals(
                "Mismatch in Product320SpecificData for product: " + actual.getId(),
                expected.getPaymentProduct320SpecificData(),
                actual.getPaymentProduct320SpecificData()
        );
        testPaymentProductFieldsEquality(
                actual.getPaymentProductFields(),
                expected.getPaymentProductFields()
        );
    }

    private void testAccountOnFileEquality(
            AccountOnFile actualAccountOnFile,
            AccountOnFile expectedAccountOnFile
    ) {

        assertEquals(
                "Mismatch in AccountOnFile for accountOnFile: " + actualAccountOnFile.getId(),
                actualAccountOnFile.getId(),
                expectedAccountOnFile.getId()
        );
        assertEquals(
                "Mismatch in ID for product: " + actualAccountOnFile.getId(),
                actualAccountOnFile.getPaymentProductId(),
                expectedAccountOnFile.getPaymentProductId()
        );
        assertEquals(
                "Mismatch in ID for product: " + actualAccountOnFile.getId(),
                actualAccountOnFile.getLabel(),
                expectedAccountOnFile.getLabel()
        );
        assertEquals(
                "Mismatch in ID for product: " + actualAccountOnFile.getId(),
                actualAccountOnFile.getAttributes(),
                expectedAccountOnFile.getAttributes()
        );
        assertEquals(
                "Mismatch in ID for product: " + actualAccountOnFile.getId(),
                Objects.requireNonNull(actualAccountOnFile.getDisplayHints())
                        .getLabelTemplate().stream().map((
                                accountOnFileDisplay -> {
                                    assert accountOnFileDisplay != null;
                                    return accountOnFileDisplay.getKey() + ":" + accountOnFileDisplay.getMask();
                                }
                        )).collect(Collectors.toList()),
                Objects.requireNonNull(expectedAccountOnFile.getDisplayHints())
                        .getLabelTemplate().stream().map((
                                accountOnFileDisplay -> {
                                    assert accountOnFileDisplay != null;
                                    return accountOnFileDisplay.getKey() + ":" + accountOnFileDisplay.getMask();
                                }
                        )).collect(Collectors.toList())
        );
    }
}
