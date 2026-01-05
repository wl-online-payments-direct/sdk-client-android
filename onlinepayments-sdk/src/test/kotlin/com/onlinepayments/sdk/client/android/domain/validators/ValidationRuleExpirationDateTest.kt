/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validators

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleExpirationDate
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleExpirationDateTest {

    private val validator = ValidationRuleExpirationDate()

    @Test
    fun `should accept current month`() {
        val now = YearMonth.now()
        val currentMonth = now.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(currentMonth)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should accept current month in MMYY format`() {
        val now = YearMonth.now()
        val currentMonth = now.format(java.time.format.DateTimeFormatter.ofPattern("MMyy"))

        val result = validator.validate(currentMonth)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should accept next month`() {
        val nextMonth = YearMonth.now().plusMonths(1)
        val value = nextMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should accept date one year in future`() {
        val futureDate = YearMonth.now().plusYears(1)
        val value = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should accept date at maximum allowed limit (25 years)`() {
        val maxDate = YearMonth.now().plusYears(25)
        val value = maxDate.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should reject previous month`() {
        val previousMonth = YearMonth.now().minusMonths(1)
        val value = previousMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject date one year in past`() {
        val pastDate = YearMonth.now().minusYears(1)
        val value = pastDate.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject date beyond maximum allowed (26 years)`() {
        val tooFarFuture = YearMonth.now().plusYears(26)
        val value = tooFarFuture.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject null value`() {
        val result = validator.validate(null)

        assertFalse(result.valid)
        assertEquals("Expiration date is required.", result.message)
    }

    @Test
    fun `should reject empty string`() {
        val result = validator.validate("")

        assertFalse(result.valid)
        assertEquals("Expiration date is required.", result.message)
    }

    @Test
    fun `should reject invalid format - too short`() {
        val result = validator.validate("123")

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject invalid format - too long`() {
        val result = validator.validate("1234567")

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject invalid month - greater than 12`() {
        val result = validator.validate("132025")

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject invalid month - zero`() {
        val result = validator.validate("002025")

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should reject non-numeric characters`() {
        val result = validator.validate("ab2025")

        assertFalse(result.valid)
        assertEquals("Invalid expiration date.", result.message)
    }

    @Test
    fun `should accept December in MMYYYY format`() {
        val future = YearMonth.now().plusMonths(6)
        val december = YearMonth.of(future.year, 12)
        val value = december.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should accept January in MMYY format`() {
        val nextYear = YearMonth.now().year + 1
        val january = YearMonth.of(nextYear, 1)
        val value = january.format(java.time.format.DateTimeFormatter.ofPattern("MMyy"))

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should handle century rollover correctly for MMYY format`() {
        // Test that a year like "01" is interpreted correctly based on current year
        val currentYear = YearMonth.now().year
        val currentYearLastTwoDigits = currentYear % 100

        // If we're in 2025, "01" should mean 2101 (next century)
        if (currentYearLastTwoDigits > 1) {
            val result = validator.validate("0101")

            assertFalse(
                result.valid,
                "Year '01' should be interpreted as next century (2101) and rejected as too far in future"
            )
        }
    }

    @Test
    fun `should accept all valid months`() {
        val futureYear = YearMonth.now().plusYears(1)

        for (month in 1..12) {
            val value = String.format("%02d%04d", month, futureYear.year)
            val result = validator.validate(value)

            assertTrue(
                result.valid,
                "Month $month should be valid, but got: ${result.message}"
            )
        }
    }

    @Test
    fun `should parse MMYY format correctly when year matches current year`() {
        val now = YearMonth.now()
        val currentYearShort = String.format("%02d", now.year % 100)
        val nextMonth = now.plusMonths(1)

        // Create MMYY for next month with current year's last 2 digits
        val value = String.format("%02d%s", nextMonth.monthValue, currentYearShort)

        val result = validator.validate(value)

        assertTrue(result.valid)
        assertEquals("", result.message)
    }

    @Test
    fun `should validate typical valid credit card expiration dates`() {
        val testCases = listOf(
            YearMonth.now().plusYears(2),
            YearMonth.now().plusYears(3),
            YearMonth.now().plusYears(5)
        )

        testCases.forEach { yearMonth ->
            // Test both formats
            val mmyyyy = yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))
            val mmyy = yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMyy"))

            assertTrue(
                validator.validate(mmyyyy).valid,
                "MMYYYY format should be valid for $yearMonth"
            )
            assertTrue(
                validator.validate(mmyy).valid,
                "MMYY format should be valid for $yearMonth"
            )
        }
    }

    @Test
    fun `should reject typical expired credit card dates`() {
        val testCases = listOf(
            YearMonth.now().minusYears(1),
            YearMonth.now().minusYears(2),
            YearMonth.now().minusMonths(6)
        )

        testCases.forEach { yearMonth ->
            val value = yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMyyyy"))

            assertFalse(
                validator.validate(value).valid,
                "Expired date $yearMonth should be invalid"
            )
        }
    }

    @Test
    fun testValidationRuleType() {
        assertEquals(
            ValidationRuleType.EXPIRATIONDATE,
            validator.type,
            "ValidationRuleExpirationDate should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        assertEquals(
            "expirationDate",
            validator.messageId,
            "ValidationRuleExpirationDate should have correct messageId"
        )
    }
}
