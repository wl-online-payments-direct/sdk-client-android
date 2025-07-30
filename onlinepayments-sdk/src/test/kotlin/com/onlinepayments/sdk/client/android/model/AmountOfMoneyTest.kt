/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@RunWith(MockitoJUnitRunner::class)
class AmountOfMoneyTest {

    @Test
    fun testConstructorWithBothParameters() {
        val amount = AmountOfMoney(1250L, "EUR")

        assertEquals("Amount should be set correctly", 1250L, amount.amount)
        assertEquals("Currency code should be set correctly", "EUR", amount.currencyCode)
    }

    @Test
    fun testConstructorWithAmountOnly() {
        val amount = AmountOfMoney(amount = 2500L)

        assertEquals("Amount should be set correctly", 2500L, amount.amount)
        assertNull("Currency code should be null when not provided", amount.currencyCode)
    }

    @Test
    fun testConstructorWithCurrencyOnly() {
        val amount = AmountOfMoney(currencyCode = "USD")

        assertEquals("Amount should default to 0", 0L, amount.amount)
        assertEquals("Currency code should be set correctly", "USD", amount.currencyCode)
    }

    @Test
    fun testDefaultConstructor() {
        val amount = AmountOfMoney()

        assertEquals("Amount should default to 0", 0L, amount.amount)
        assertNull("Currency code should be null by default", amount.currencyCode)
    }

    @Test
    fun testNullAmount() {
        val amount = AmountOfMoney(amount = null, currencyCode = "GBP")

        assertNull("Amount should be null when explicitly set", amount.amount)
        assertEquals("Currency code should be set correctly", "GBP", amount.currencyCode)
    }

    @Test
    fun testNullCurrencyCode() {
        val amount = AmountOfMoney(1000L, null)

        assertEquals("Amount should be set correctly", 1000L, amount.amount)
        assertNull("Currency code should be null when explicitly set", amount.currencyCode)
    }

    @Test
    fun testBothParametersNull() {
        val amount = AmountOfMoney(null, null)

        assertNull("Amount should be null", amount.amount)
        assertNull("Currency code should be null", amount.currencyCode)
    }

    @Test
    fun testEquality() {
        val amount1 = AmountOfMoney(1000L, "EUR")
        val amount2 = AmountOfMoney(1000L, "EUR")
        val amount3 = AmountOfMoney(2000L, "EUR")
        val amount4 = AmountOfMoney(1000L, "USD")

        assertEquals("Same amount and currency should be equal", amount1, amount2)
        assertNotEquals("Different amounts should not be equal", amount1, amount3)
        assertNotEquals("Different currencies should not be equal", amount1, amount4)
    }

    @Test
    fun testEqualityWithNulls() {
        val amount1 = AmountOfMoney(null, null)
        val amount2 = AmountOfMoney(null, null)
        val amount3 = AmountOfMoney(1000L, null)
        val amount4 = AmountOfMoney(null, "EUR")

        assertEquals("Both null should be equal", amount1, amount2)
        assertNotEquals("Null amount vs non-null should not be equal", amount1, amount3)
        assertNotEquals("Null currency vs non-null should not be equal", amount1, amount4)
    }

    @Test
    fun testHashCode() {
        val amount1 = AmountOfMoney(1000L, "EUR")
        val amount2 = AmountOfMoney(1000L, "EUR")
        val amount3 = AmountOfMoney(2000L, "EUR")

        assertEquals("Equal objects should have same hash code", amount1.hashCode(), amount2.hashCode())
        assertNotEquals("Different objects should have different hash codes", amount1.hashCode(), amount3.hashCode())
    }

    @Test
    fun testToString() {
        val amount1 = AmountOfMoney(1250L, "EUR")
        val amount2 = AmountOfMoney(null, "USD")
        val amount3 = AmountOfMoney(1000L, null)

        val toString1 = amount1.toString()
        val toString2 = amount2.toString()
        val toString3 = amount3.toString()

        assertTrue("toString should contain amount", toString1.contains("1250"))
        assertTrue("toString should contain currency", toString1.contains("EUR"))
        assertTrue("toString should contain null amount", toString2.contains("null"))
        assertTrue("toString should contain currency", toString2.contains("USD"))
        assertTrue("toString should contain amount", toString3.contains("1000"))
        assertTrue("toString should contain null currency", toString3.contains("null"))
    }

    @Test
    fun testLargeAmounts() {
        val maxAmount = Long.MAX_VALUE
        val minAmount = Long.MIN_VALUE

        val amount1 = AmountOfMoney(maxAmount, "EUR")
        val amount2 = AmountOfMoney(minAmount, "USD")

        assertEquals("Should handle maximum long value", maxAmount, amount1.amount)
        assertEquals("Should handle minimum long value", minAmount, amount2.amount)
    }

    @Test
    fun testZeroAmount() {
        val amount = AmountOfMoney(0L, "EUR")

        assertEquals("Should handle zero amount correctly", 0L, amount.amount)
        assertEquals("Currency should be preserved", "EUR", amount.currencyCode)
    }

    @Test
    fun testNegativeAmount() {
        val amount = AmountOfMoney(-1000L, "EUR")

        assertEquals("Should handle negative amounts", -1000L, amount.amount)
        assertEquals("Currency should be preserved", "EUR", amount.currencyCode)
    }

    @Test
    fun testCommonCurrencyCodes() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY")

        currencies.forEach { currency ->
            val amount = AmountOfMoney(1000L, currency)
            assertEquals("Currency $currency should be preserved", currency, amount.currencyCode)
        }
    }

    @Test
    fun testUncommonCurrencyCodes() {
        val currencies = listOf("XAU", "XAG", "BTC", "ETH", "XXX")

        currencies.forEach { currency ->
            val amount = AmountOfMoney(1000L, currency)
            assertEquals("Uncommon currency $currency should be preserved", currency, amount.currencyCode)
        }
    }

    @Test
    fun testLowerCaseCurrencyCode() {
        val amount = AmountOfMoney(1000L, "eur")

        assertEquals("Lowercase currency should be preserved as-is", "eur", amount.currencyCode)
    }

    @Test
    fun testInvalidCurrencyCode() {
        val amount = AmountOfMoney(1000L, "INVALID")

        assertEquals("Invalid currency code should be preserved", "INVALID", amount.currencyCode)
    }

    @Test
    fun testEmptyCurrencyCode() {
        val amount = AmountOfMoney(1000L, "")

        assertEquals("Empty currency code should be preserved", "", amount.currencyCode)
    }

    @Test
    fun testSerialization() {
        val originalAmount = AmountOfMoney(1500L, "EUR")

        // Serialize the object
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(originalAmount)
        }

        // Deserialize the object
        val bais = ByteArrayInputStream(baos.toByteArray())
        val deserializedAmount = ObjectInputStream(bais).use { ois ->
            ois.readObject() as AmountOfMoney
        }

        assertEquals("Deserialized object should be equal to original", originalAmount, deserializedAmount)
        assertEquals("Amount should be preserved after serialization", originalAmount.amount, deserializedAmount.amount)
        assertEquals(
            "Currency should be preserved after serialization",
            originalAmount.currencyCode,
            deserializedAmount.currencyCode
        )
    }

    @Test
    fun testSerializationWithNulls() {
        val originalAmount = AmountOfMoney(null, null)

        // Serialize the object
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(originalAmount)
        }

        // Deserialize the object
        val bais = ByteArrayInputStream(baos.toByteArray())
        val deserializedAmount = ObjectInputStream(bais).use { ois ->
            ois.readObject() as AmountOfMoney
        }

        assertEquals("Deserialized object with nulls should be equal to original", originalAmount, deserializedAmount)
        assertNull("Amount should remain null after serialization", deserializedAmount.amount)
        assertNull("Currency should remain null after serialization", deserializedAmount.currencyCode)
    }

    @Test
    fun testDataClassCopy() {
        val original = AmountOfMoney(1000L, "EUR")

        val copied1 = original.copy()
        val copied2 = original.copy(amount = 2000L)
        val copied3 = original.copy(currencyCode = "USD")
        val copied4 = original.copy(amount = 1500L, currencyCode = "GBP")

        assertEquals("Copy without parameters should be identical", original, copied1)
        assertEquals("Copy with new amount should have different amount", 2000L, copied2.amount)
        assertEquals("Copy with new amount should preserve currency", "EUR", copied2.currencyCode)
        assertEquals("Copy with new currency should preserve amount", 1000L, copied3.amount)
        assertEquals("Copy with new currency should have different currency", "USD", copied3.currencyCode)
        assertEquals("Copy with both parameters should have new values", 1500L, copied4.amount)
        assertEquals("Copy with both parameters should have new currency", "GBP", copied4.currencyCode)
    }

    @Test
    fun testComponentAccess() {
        val amount = AmountOfMoney(1200L, "CHF")

        val (amountValue, currencyValue) = amount

        assertEquals("Component1 should be amount", 1200L, amountValue)
        assertEquals("Component2 should be currency", "CHF", currencyValue)
    }

    @Test
    fun testComponentAccessWithNulls() {
        val amount = AmountOfMoney(null, null)

        val (amountValue, currencyValue) = amount

        assertNull("Component1 should be null amount", amountValue)
        assertNull("Component2 should be null currency", currencyValue)
    }
}