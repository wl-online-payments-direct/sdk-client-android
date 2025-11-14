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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AmountOfMoneyJavaTest {

    @Test
    public void testConstructorWithBothParameters() {
        AmountOfMoney amount = new AmountOfMoney(1250L, "EUR");

        assertEquals("Amount should be set correctly", Long.valueOf(1250L), amount.getAmount());
        assertEquals("Currency code should be set correctly", "EUR", amount.getCurrencyCode());
    }

    @Test
    public void testConstructorWithAmountOnly() {
        AmountOfMoney amount = new AmountOfMoney(2500L, null);

        assertEquals("Amount should be set correctly", Long.valueOf(2500L), amount.getAmount());
        assertNull("Currency code should be null when not provided", amount.getCurrencyCode());
    }

    @Test
    public void testConstructorWithCurrencyOnly() {
        AmountOfMoney amount = new AmountOfMoney(null, "USD");

        assertNull("Amount should be null when not provided", amount.getAmount());
        assertEquals("Currency code should be set correctly", "USD", amount.getCurrencyCode());
    }

    @Test
    public void testDefaultConstructor() {
        AmountOfMoney amount = new AmountOfMoney();

        assertEquals("Amount should default to 0", Long.valueOf(0L), amount.getAmount());
        assertNull("Currency code should be null by default", amount.getCurrencyCode());
    }

    @Test
    public void testNullAmount() {
        AmountOfMoney amount = new AmountOfMoney(null, "GBP");

        assertNull("Amount should be null when explicitly set", amount.getAmount());
        assertEquals("Currency code should be set correctly", "GBP", amount.getCurrencyCode());
    }

    @Test
    public void testNullCurrencyCode() {
        AmountOfMoney amount = new AmountOfMoney(1000L, null);

        assertEquals("Amount should be set correctly", Long.valueOf(1000L), amount.getAmount());
        assertNull("Currency code should be null when explicitly set", amount.getCurrencyCode());
    }

    @Test
    public void testBothParametersNull() {
        AmountOfMoney amount = new AmountOfMoney(null, null);

        assertNull("Amount should be null", amount.getAmount());
        assertNull("Currency code should be null", amount.getCurrencyCode());
    }

    @Test
    public void testEquality() {
        AmountOfMoney amount1 = new AmountOfMoney(1000L, "EUR");
        AmountOfMoney amount2 = new AmountOfMoney(1000L, "EUR");
        AmountOfMoney amount3 = new AmountOfMoney(2000L, "EUR");
        AmountOfMoney amount4 = new AmountOfMoney(1000L, "USD");

        assertEquals("Same amount and currency should be equal", amount1, amount2);
        assertNotEquals("Different amounts should not be equal", amount1, amount3);
        assertNotEquals("Different currencies should not be equal", amount1, amount4);
    }

    @Test
    public void testEqualityWithNulls() {
        AmountOfMoney amount1 = new AmountOfMoney(null, null);
        AmountOfMoney amount2 = new AmountOfMoney(null, null);
        AmountOfMoney amount3 = new AmountOfMoney(1000L, null);
        AmountOfMoney amount4 = new AmountOfMoney(null, "EUR");

        assertEquals("Both null should be equal", amount1, amount2);
        assertNotEquals("Null amount vs non-null should not be equal", amount1, amount3);
        assertNotEquals("Null currency vs non-null should not be equal", amount1, amount4);
    }

    @Test
    public void testHashCode() {
        AmountOfMoney amount1 = new AmountOfMoney(1000L, "EUR");
        AmountOfMoney amount2 = new AmountOfMoney(1000L, "EUR");
        AmountOfMoney amount3 = new AmountOfMoney(2000L, "EUR");

        assertEquals("Equal objects should have same hash code", amount1.hashCode(), amount2.hashCode());
        assertNotEquals("Different objects should have different hash codes", amount1.hashCode(), amount3.hashCode());
    }

    @Test
    public void testToString() {
        AmountOfMoney amount1 = new AmountOfMoney(1250L, "EUR");
        AmountOfMoney amount2 = new AmountOfMoney(null, "USD");
        AmountOfMoney amount3 = new AmountOfMoney(1000L, null);

        String toString1 = amount1.toString();
        String toString2 = amount2.toString();
        String toString3 = amount3.toString();

        assertTrue("toString should contain amount", toString1.contains("1250"));
        assertTrue("toString should contain currency", toString1.contains("EUR"));
        assertTrue("toString should contain null amount", toString2.contains("null"));
        assertTrue("toString should contain currency", toString2.contains("USD"));
        assertTrue("toString should contain amount", toString3.contains("1000"));
        assertTrue("toString should contain null currency", toString3.contains("null"));
    }

    @Test
    public void testLargeAmounts() {
        long maxAmount = Long.MAX_VALUE;
        long minAmount = Long.MIN_VALUE;

        AmountOfMoney amount1 = new AmountOfMoney(maxAmount, "EUR");
        AmountOfMoney amount2 = new AmountOfMoney(minAmount, "USD");

        assertEquals("Should handle maximum long value", Long.valueOf(maxAmount), amount1.getAmount());
        assertEquals("Should handle minimum long value", Long.valueOf(minAmount), amount2.getAmount());
    }

    @Test
    public void testZeroAmount() {
        AmountOfMoney amount = new AmountOfMoney(0L, "EUR");

        assertEquals("Should handle zero amount correctly", Long.valueOf(0L), amount.getAmount());
        assertEquals("Currency should be preserved", "EUR", amount.getCurrencyCode());
    }

    @Test
    public void testNegativeAmount() {
        AmountOfMoney amount = new AmountOfMoney(-1000L, "EUR");

        assertEquals("Should handle negative amounts", Long.valueOf(-1000L), amount.getAmount());
        assertEquals("Currency should be preserved", "EUR", amount.getCurrencyCode());
    }

    @Test
    public void testCommonCurrencyCodes() {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY"};

        for (String currency : currencies) {
            AmountOfMoney amount = new AmountOfMoney(1000L, currency);
            assertEquals("Currency " + currency + " should be preserved", currency, amount.getCurrencyCode());
        }
    }

    @Test
    public void testUncommonCurrencyCodes() {
        String[] currencies = {"XAU", "XAG", "BTC", "ETH", "XXX"};

        for (String currency : currencies) {
            AmountOfMoney amount = new AmountOfMoney(1000L, currency);
            assertEquals("Uncommon currency " + currency + " should be preserved", currency, amount.getCurrencyCode());
        }
    }

    @Test
    public void testLowerCaseCurrencyCode() {
        AmountOfMoney amount = new AmountOfMoney(1000L, "eur");

        assertEquals("Lowercase currency should be preserved as-is", "eur", amount.getCurrencyCode());
    }

    @Test
    public void testInvalidCurrencyCode() {
        AmountOfMoney amount = new AmountOfMoney(1000L, "INVALID");

        assertEquals("Invalid currency code should be preserved", "INVALID", amount.getCurrencyCode());
    }

    @Test
    public void testEmptyCurrencyCode() {
        AmountOfMoney amount = new AmountOfMoney(1000L, "");

        assertEquals("Empty currency code should be preserved", "", amount.getCurrencyCode());
    }

    @Test
    public void testSerialization() throws Exception {
        AmountOfMoney originalAmount = new AmountOfMoney(1500L, "EUR");

        // Serialize the object
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalAmount);
        oos.close();

        // Deserialize the object
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        AmountOfMoney deserializedAmount = (AmountOfMoney) ois.readObject();
        ois.close();

        assertEquals("Deserialized object should be equal to original", originalAmount, deserializedAmount);
        assertEquals("Amount should be preserved after serialization", originalAmount.getAmount(), deserializedAmount.getAmount());
        assertEquals("Currency should be preserved after serialization", originalAmount.getCurrencyCode(), deserializedAmount.getCurrencyCode());
    }

    @Test
    public void testSerializationWithNulls() throws Exception {
        AmountOfMoney originalAmount = new AmountOfMoney(null, null);

        // Serialize the object
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalAmount);
        oos.close();

        // Deserialize the object
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        AmountOfMoney deserializedAmount = (AmountOfMoney) ois.readObject();
        ois.close();

        assertEquals("Deserialized object with nulls should be equal to original", originalAmount, deserializedAmount);
        assertNull("Amount should remain null after serialization", deserializedAmount.getAmount());
        assertNull("Currency should remain null after serialization", deserializedAmount.getCurrencyCode());
    }
}
