/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney;
import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration;
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto;
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Test;

import java.util.Map;

/**
 * Tests that public data classes that SDK users construct work correctly in Java.
 */

public class PublicDataClassesJavaTest {

    @Test
    public void testSessionDataConstruction() {
        SessionData sessionData = new SessionData(
            "session123",
            "customer456",
            "https://api.example.com",
            "https://assets.example.com"
        );

        assertEquals("session123", sessionData.getClientSessionId());
        assertEquals("customer456", sessionData.getCustomerId());
        assertEquals("https://api.example.com", sessionData.getClientApiUrl());
        assertEquals("https://assets.example.com", sessionData.getAssetUrl());
    }

    @Test
    public void testSdkConfigurationConstruction() {
        SdkConfiguration config = new SdkConfiguration(
            true,
            "MyApp",
            "AndroidSDK",
            false
        );

        assertEquals(Boolean.TRUE, config.getEnvironmentIsProduction());
        assertEquals("MyApp", config.getAppIdentifier());
        assertEquals("AndroidSDK", config.getSdkIdentifier());
        assertFalse(config.getLoggingEnabled());
    }

    @Test
    public void testSdkConfigurationConstructionWithNulls() {
        SdkConfiguration config = new SdkConfiguration(
            null,
            null,
            null,
            true
        );

        assertNull(config.getEnvironmentIsProduction());
        assertNull(config.getAppIdentifier());
        assertNull(config.getSdkIdentifier());
        assertTrue(config.getLoggingEnabled());
    }

    @Test
    public void testAmountOfMoneyConstruction() {
        AmountOfMoney amount = new AmountOfMoney(1298L, "EUR");

        assertEquals(Long.valueOf(1298L), amount.getAmount());
        assertEquals("EUR", amount.getCurrencyCode());
    }

    @Test
    public void testAmountOfMoneyConstructionWithDefaults() {
        AmountOfMoney amount = new AmountOfMoney(0L, "EUR");

        assertEquals(Long.valueOf(0L), amount.getAmount());
        assertEquals("EUR", amount.getCurrencyCode());
    }

    @Test
    public void testPaymentContextConstruction() {
        AmountOfMoney amount = new AmountOfMoney(1298L, "EUR");
        PaymentContext context = new PaymentContext(amount, "NL", false);

        assertNotNull(context.getAmountOfMoney());
        assertEquals(Long.valueOf(1298L), context.getAmountOfMoney().getAmount());
        assertEquals("EUR", context.getAmountOfMoney().getCurrencyCode());
        assertEquals("NL", context.getCountryCode());
        assertEquals(false, context.isRecurring());
    }

    @Test
    public void testPaymentContextSetters() {
        var amountOfMoney = new AmountOfMoney(0L, "EUR");
        var context = new PaymentContext(amountOfMoney);

        assertNull(context.getCountryCode());
        assertEquals(false, context.isRecurring());

        AmountOfMoney amount = new AmountOfMoney(500L, "USD");
        context.setAmountOfMoney(amount);
        context.setCountryCode("US");
        context.setRecurring(true);

        assertEquals(Long.valueOf(500L), context.getAmountOfMoney().getAmount());
        assertEquals("USD", context.getAmountOfMoney().getCurrencyCode());
        assertEquals("US", context.getCountryCode());
        assertEquals(true, context.isRecurring());
    }

    @Test
    public void testPaymentContextToMap() {
        AmountOfMoney amount = new AmountOfMoney(1298L, "EUR");
        PaymentContext context = new PaymentContext(amount, "NL", true);

        Map<String, String> map = context.toMap();

        assertEquals("NL", map.get("countryCode"));
        assertEquals("1298", map.get("amount"));
        assertEquals("true", map.get("isRecurring"));
        assertEquals("EUR", map.get("currencyCode"));
    }

    @Test
    public void testCreditCardTokenRequestConstruction() {
        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest(
            "4111111111111111",
            "John Doe",
            "1225",
            "123",
            1
        );

        assertEquals("4111111111111111", tokenRequest.getCardNumber());
        assertEquals("John Doe", tokenRequest.getCardholderName());
        assertEquals("1225", tokenRequest.getExpiryDate());
        assertEquals("123", tokenRequest.getSecurityCode());
        assertEquals(Integer.valueOf(1), tokenRequest.getPaymentProductId());
    }

    @Test
    public void testCreditCardTokenRequestConstructionWithDefaults() {
        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest();

        assertNull(tokenRequest.getCardNumber());
        assertNull(tokenRequest.getCardholderName());
        assertNull(tokenRequest.getExpiryDate());
        assertNull(tokenRequest.getSecurityCode());
        assertNull(tokenRequest.getPaymentProductId());
    }

    @Test
    public void testCreditCardTokenRequestSetters() {
        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest();

        tokenRequest.setCardNumber("5500000000000004");
        tokenRequest.setCardholderName("Jane Smith");
        tokenRequest.setExpiryDate("0626");
        tokenRequest.setSecurityCode("456");
        tokenRequest.setPaymentProductId(3);

        assertEquals("5500000000000004", tokenRequest.getCardNumber());
        assertEquals("Jane Smith", tokenRequest.getCardholderName());
        assertEquals("0626", tokenRequest.getExpiryDate());
        assertEquals("456", tokenRequest.getSecurityCode());
        assertEquals(Integer.valueOf(3), tokenRequest.getPaymentProductId());
    }

    @Test
    public void testCreditCardTokenRequestGetValues() {
        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest(
            "4111111111111111",
            "John Doe",
            "1225",
            "123",
            1
        );

        Map<String, ?> values = tokenRequest.getValues();

        assertEquals("4111111111111111", values.get("cardNumber"));
        assertEquals("John Doe", values.get("cardholderName"));
        assertEquals("1225", values.get("expiryDate"));
        assertEquals("123", values.get("cvv"));
        assertEquals(1, values.get("paymentProductId"));
    }

    @Test
    public void testPaymentRequestConstruction() {
        PaymentProductDto paymentProductDto = GsonHelperJava.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto.class
        );

        PaymentProduct paymentProduct = new PaymentProductFactory().createPaymentProduct(paymentProductDto);

        PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);

        assertNotNull(paymentRequest);
        assertEquals(paymentProduct, paymentRequest.getPaymentProduct());
        assertNull(paymentRequest.getAccountOnFile());
        assertFalse(paymentRequest.getTokenize());
    }

    @Test
    public void testPaymentRequestSetValue() {
        PaymentProductDto paymentProductDto = GsonHelperJava.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto.class
        );

        PaymentProduct paymentProduct = new PaymentProductFactory().createPaymentProduct(paymentProductDto);

        PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);

        paymentRequest.setValue("cardNumber", "4111111111111111");
        paymentRequest.setValue("cvv", "123");

        assertEquals("4111111111111111", paymentRequest.getValue("cardNumber"));
        assertEquals("123", paymentRequest.getValue("cvv"));
    }

    @Test
    public void testPaymentRequestGetValues() {
        PaymentProductDto paymentProductDto = GsonHelperJava.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto.class
        );

        PaymentProduct paymentProduct = new PaymentProductFactory().createPaymentProduct(paymentProductDto);

        PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);

        paymentRequest.setValue("cardNumber", "4111111111111111");
        paymentRequest.setValue("cvv", "123");
        paymentRequest.setValue("expiryDate", "1225");

        Map<String, String> values = paymentRequest.getValues();

        assertEquals(3, values.size());
        assertEquals("4111111111111111", values.get("cardNumber"));
        assertEquals("123", values.get("cvv"));
        assertEquals("1225", values.get("expiryDate"));
    }

    @Test
    public void testPaymentRequestSetTokenize() {
        PaymentProductDto paymentProductDto = GsonHelperJava.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto.class
        );

        PaymentProduct paymentProduct = new PaymentProductFactory().createPaymentProduct(paymentProductDto);

        PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);

        assertFalse(paymentRequest.getTokenize());

        paymentRequest.setTokenize(true);

        assertTrue(paymentRequest.getTokenize());
    }
}
