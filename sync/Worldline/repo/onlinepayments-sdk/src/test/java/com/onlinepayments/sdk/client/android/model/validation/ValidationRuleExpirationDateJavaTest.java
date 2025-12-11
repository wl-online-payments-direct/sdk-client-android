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
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ValidationRuleExpirationDateJavaTest {

    private final ValidationRuleExpirationDate testValidationRule = new ValidationRuleExpirationDate();

    private final Date simulatedNow = new GregorianCalendar(2018, 8, 24, 5, 53, 33).getTime();
    private final Date simulatedFutureLimit = new GregorianCalendar(
        2033,
        11,
        24,
        5,
        53,
        33
    ).getTime();

    private final Date simulatedNowOnEdge = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
    private final Date getSimulatedFutureLimitOnEdge = new GregorianCalendar(
        2033,
        11,
        1,
        0,
        0,
        0
    ).getTime();

    @Test
    public void testObtainDateNormal() throws ParseException {
        Date toBeValidated = testValidationRule.obtainEnteredDateFromUnmaskedValue("0311");

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(toBeValidated);

        assertEquals(Calendar.MARCH, calendar.get(Calendar.MONTH));
        assertEquals(2011, calendar.get(Calendar.YEAR));
    }

    @Test
    public void testValidNormal() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0421");

        assert testEnteredDate != null;
        assertTrue(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInvalidNormalLower() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0316");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInvalidNormalUpper() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0956");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testValidCloseToLower() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0818");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInValidCloseToLowerMonth() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0718");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInValidCloseToLowerYear() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0817");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testValidCloseToUpper() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1233");
        assert testEnteredDate != null;
        assertTrue(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInValidCloseToUpperMonth() throws ParseException {
        // This test makes no sense, since only the year is validated for the upper bound.
        // That makes this test equal to the previous one.
        testValidCloseToUpper();
    }

    @Test
    public void testInValidCloseToUpperYear() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0134");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testValidOnEdgeLower() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0118");
        assert testEnteredDate != null;
        assertTrue(testValidationRule.validateDateIsBetween(
            simulatedNowOnEdge,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void random() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0226");
        assert testEnteredDate != null;
        assertTrue(testValidationRule.validateDateIsBetween(
            simulatedNowOnEdge,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testInValidOnEdgeLower() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1217");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNowOnEdge,
            simulatedFutureLimit,
            testEnteredDate
        ));
    }

    @Test
    public void testValidOnEdgeUpper() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("1233");
        assert testEnteredDate != null;
        assertTrue(testValidationRule.validateDateIsBetween(
            simulatedNow,
            getSimulatedFutureLimitOnEdge,
            testEnteredDate
        ));
    }

    @Test
    public void testInValidOnEdgeUpper() throws ParseException {
        Date testEnteredDate = testValidationRule.obtainEnteredDateFromUnmaskedValue("0134");
        assert testEnteredDate != null;
        assertFalse(testValidationRule.validateDateIsBetween(
            simulatedNow,
            getSimulatedFutureLimitOnEdge,
            testEnteredDate
        ));
    }
}
