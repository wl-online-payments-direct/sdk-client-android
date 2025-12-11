/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class ValidationRuleExpirationDateTest {
    private val testValidationRule = ValidationRuleExpirationDate()

    private val simulatedNow: Date = GregorianCalendar(2018, 8, 24, 5, 53, 33).getTime()
    private val simulatedFutureLimit: Date = GregorianCalendar(2033, 11, 24, 5, 53, 33).getTime()

    private val simulatedNowOnEdge: Date = GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime()
    private val getSimulatedFutureLimitOnEdge: Date = GregorianCalendar(2033, 11, 1, 0, 0, 0).getTime()

    @Test
    @Throws(ParseException::class)
    fun testObtainDateNormal() {
        val toBeValidated = testValidationRule.obtainEnteredDateFromUnmaskedValue("0311")

        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.setTime(toBeValidated!!)

        Assert.assertEquals(Calendar.MARCH.toLong(), calendar.get(Calendar.MONTH).toLong())
        Assert.assertEquals(2011, calendar.get(Calendar.YEAR).toLong())
    }

    @Test
    @Throws(ParseException::class)
    fun testValidNormal() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0421")

        TestCase.assertTrue(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInvalidNormalLower() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0316")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInvalidNormalUpper() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0956")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testValidCloseToLower() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0818")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidCloseToLowerMonth() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0718")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidCloseToLowerYear() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0817")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testValidCloseToUpper() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1233")
        TestCase.assertTrue(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidCloseToUpperMonth() {
        // This test makes no sense, since only the year is validated for the upper bound.
        // That makes this test equal to the previous one.
        testValidCloseToUpper()
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidCloseToUpperYear() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0134")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testValidOnEdgeLower() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0118")
        TestCase.assertTrue(
            testValidationRule.validateDateIsBetween(
                simulatedNowOnEdge,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun random() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0226")
        TestCase.assertTrue(
            testValidationRule.validateDateIsBetween(
                simulatedNowOnEdge,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidOnEdgeLower() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1217")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNowOnEdge,
                simulatedFutureLimit,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testValidOnEdgeUpper() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1233")
        TestCase.assertTrue(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                getSimulatedFutureLimitOnEdge,
                testEnteredDate!!
            )
        )
    }

    @Test
    @Throws(ParseException::class)
    fun testInValidOnEdgeUpper() {
        val testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0134")
        Assert.assertFalse(
            testValidationRule.validateDateIsBetween(
                simulatedNow,
                getSimulatedFutureLimitOnEdge,
                testEnteredDate!!
            )
        )
    }
}
