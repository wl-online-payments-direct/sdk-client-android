/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.formatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.onlinepayments.sdk.client.android.model.FormatResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Objects;

/**
 * Junit Test class which tests masking and unmasking functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class StringFormatterJavaTest {
    record TestCase(
        String newValue,
        String oldValue,
        int caretPosition,
        int removedLength,
        int insertedLength,
        String expectedResult,
        int expectedCaretPosition) {
    }

    private final String maskExpiryDate = "{{99}}-{{99}}";
    private final String maskCardNumber = "{{9999}} {{9999}} {{9999}} {{9999}} {{999}}";
    private final String maskAlpha = "{{aa}} {{aa}} {{aa}} {{aa}} {{aa}}";

    private final String maskTestString1 = "1";
    private final String maskAlphaString = "abcdefghij";

    @Test
    public void testMaskSingleCharacter() {
        String maskedValue = StringFormatter.applyMask(maskExpiryDate, maskTestString1);
        assertEquals("1", maskedValue);
    }

    @Test
    public void testMaskTwoCharacters() {
        String maskTestString2 = "12";
        String maskedValue = StringFormatter.applyMask(maskExpiryDate, maskTestString2);
        assertEquals("12-", maskedValue);
    }

    @Test
    public void testMaskFourCharacters() {
        String maskTestString3 = "1234";
        String maskedValue = StringFormatter.applyMask(maskExpiryDate, maskTestString3);
        assertEquals("12-34", maskedValue);
    }

    @Test
    public void testMaskTooManyCharacters() {
        String maskTestString4 = "1234567890123456789";
        String maskedValue = StringFormatter.applyMask(maskExpiryDate, maskTestString4);
        assertEquals("12-34", maskedValue);
    }

    @Test
    public void testMaskNumbers() {
        String maskNumbers = "{{99}} {{99}} {{99}} {{99}} {{99}}";
        String maskNumbersString = "1234567890";
        String maskedValue = StringFormatter.applyMask(maskNumbers, maskNumbersString);
        assertEquals("12 34 56 78 90", maskedValue);
    }

    @Test
    public void testMaskWildcards() {
        String maskWildcards = "{{**}} {{**}} {{**}} {{**}} {{**}}";
        String maskWildcardsString = "!!!!!!!!!!";
        String maskedValue = StringFormatter.applyMask(maskWildcards, maskWildcardsString);
        assertEquals("!! !! !! !! !!", maskedValue);
    }

    @Test
    public void testMaskAlpha() {
        String maskedValue = StringFormatter.applyMask(maskAlpha, maskAlphaString);
        assertEquals("ab cd ef gh ij", maskedValue);
    }

    @Test
    public void testMaskWithCursorPosition() {
        FormatResult formatResult = StringFormatter.applyMask(maskAlpha, maskAlphaString, 1);
        assertEquals("ab cd ef gh ij", Objects.requireNonNull(formatResult).getFormattedResult());
        assertEquals(1, Objects.requireNonNull(formatResult.getCursorIndex()).intValue());
    }

    @Test
    public void testUnmaskingSingleCharacter() {
        String maskedValue = StringFormatter.removeMask(maskExpiryDate, maskTestString1);
        assertEquals("1", maskedValue);
    }

    @Test
    public void testUnmaskingMaskedCharacters() {
        String maskTestString5 = "12-34";
        String maskedValue = StringFormatter.removeMask(maskExpiryDate, maskTestString5);
        assertEquals("1234", maskedValue);
    }

    @Test
    public void testApplyMaskWithTextWatcherBeforeTextChangedInformationCreditCardNumber() {
        List<TestCase> testCases = List.of(
            // *** Simple add-to-the-end-tests ***
            // Test typing a single digit in the EditText
            new TestCase("1", "", 0, 0, 1, "1", 1),
            new TestCase("12", "", 0, 0, 2, "12", 2),
            new TestCase("1234", "", 0, 0, 4, "1234 ", 5),
            new TestCase("1234567890123456789", "", 0, 0, 19, "1234 5678 9012 3456 789", 23),
            new TestCase("123", "12", 2, 0, 1, "123", 3),
            new TestCase("1234", "12", 2, 0, 2, "1234 ", 5),
            new TestCase("1234567890", "12", 2, 0, 8, "1234 5678 90", 12),
            new TestCase("1234 5", "1234 ", 5, 0, 1, "1234 5", 6),
            new TestCase("12345 ", "1234 ", 4, 0, 1, "1234 5", 6),
            new TestCase("123456 ", "1234 ", 4, 0, 2, "1234 56", 7),
            new TestCase("12340123 5678 9", "1234 5678 9", 4, 0, 4, "1234 0123 5678 9", 10),
            new TestCase("1234 5678", "1234 567", 8, 0, 1, "1234 5678 ", 10),
            new TestCase(
                "1234 5678 90123456789",
                "1234 5678 901",
                13,
                0,
                8,
                "1234 5678 9012 3456 789",
                23
            ),
            // *** Adding in between other characters tests ***
            new TestCase("132", "12", 1, 0, 1, "132", 2),
            new TestCase("1423", "123", 1, 0, 1, "1423 ", 2),
            new TestCase("1342", "12", 1, 0, 2, "1342 ", 3),
            new TestCase("1345678902", "12", 1, 0, 8, "1345 6789 02", 11),
            new TestCase("1a2bc3d4e5", "", 0, 0, 10, "1234 5", 6),
            new TestCase("1234 ", "12", 2, 0, 3, "1234 ", 5),
            new TestCase("1234 5678 9012 3456 789", "0", 0, 0, 23, "1234 5678 9012 3456 789", 23),
            new TestCase("01234 ", "1234 ", 0, 0, 1, "0123 4", 1),
            new TestCase("1234 56278 901", "1234 5678 901", 7, 0, 1, "1234 5627 8901 ", 8),

            // Removing single/multiple characters
            new TestCase("", "1", 0, 1, 0, "", 0),
            new TestCase("", "12", 0, 2, 0, "", 0),
            new TestCase("", "1234 56", 0, 7, 0, "", 0),

            // Removing from the beginning
            new TestCase("23", "123", 0, 1, 0, "23", 0),
            new TestCase("2345 6", "1234 56", 0, 1, 0, "2345 6", 0),
            new TestCase(
                "2345 6789 0123 4567 89",
                "1234 5678 9012 3456 789",
                0,
                1,
                0,
                "2345 6789 0123 4567 89",
                0
            ),
            new TestCase("3", "123", 0, 2, 0, "3", 0),
            new TestCase("3456 78", "1234 5678 ", 0, 2, 0, "3456 78", 0),

            // Removing from the end
            new TestCase("12", "123", 2, 1, 0, "12", 2),
            new TestCase("1", "123", 1, 2, 0, "1", 1),
            new TestCase("1234 ", "1234 ", 4, 1, 0, "1234 ", 4),
            new TestCase("123", "1234 5", 3, 3, 0, "123", 3),
            new TestCase("1234 ", "1234 56", 4, 3, 0, "1234 ", 4),
            new TestCase("1234 ", "1234 56", 5, 2, 0, "1234 ", 5),
            new TestCase("123", "1234 5678 9012 3456 78", 3, 19, 0, "123", 3),
            new TestCase("1234 ", "1234 5678 9012 3456 78", 4, 18, 0, "1234 ", 4),
            new TestCase("1234 ", "1234 5678 9012 3456 78", 5, 17, 0, "1234 ", 5),

            // Removing from within
            new TestCase("13", "123", 1, 1, 0, "13", 1),
            new TestCase("134", "1234 ", 1, 1, 0, "134", 1),
            new TestCase("123", "1234 ", 3, 1, 0, "123", 3),
            new TestCase("12", "1234 ", 2, 2, 0, "12", 2),
            new TestCase("1237 89", "1234 5678 9", 3, 4, 0, "1237 89", 3),
            new TestCase("1234 56", "1234 56", 4, 1, 0, "1234 56", 4),

            // Simple replacements (no masking involved)
            new TestCase("2", "1", 0, 1, 1, "2", 1),
            new TestCase("34", "12", 0, 2, 2, "34", 2),
            new TestCase("23", "1", 0, 1, 2, "23", 2),
            new TestCase("4", "123", 0, 3, 1, "4", 1),

            // Replacements involving mask characters
            new TestCase("1235 ", "1234 ", 3, 1, 1, "1235 ", 5),
            new TestCase("125", "1234 ", 2, 2, 1, "125", 3),
            new TestCase("1235 ", "1234 ", 3, 2, 1, "1235 ", 5),
            new TestCase("125", "1234 ", 2, 3, 1, "125", 3),
            new TestCase("1256 ", "1234 ", 2, 2, 2, "1256 ", 5),
            new TestCase("1256 ", "1234 ", 2, 3, 2, "1256 ", 5),
            new TestCase("5678 ", "1234 ", 0, 4, 4, "5678 ", 5),
            new TestCase("5678 ", "1234 ", 0, 5, 4, "5678 ", 5),
            new TestCase("1234 5", "1234 ", 4, 1, 1, "1234 5", 6),
            new TestCase("1235 6", "1234 ", 3, 2, 2, "1235 6", 6),

            // Replacements spanning across mask characters
            new TestCase("6", "1234 5", 0, 6, 1, "6", 1),
            new TestCase("6789 ", "1234 5", 0, 6, 4, "6789 ", 5),
            new TestCase("1237 896", "1234 56", 3, 3, 3, "1237 896", 7),
            new TestCase("1237 8901 6", "1234 56", 3, 3, 5, "1237 8901 6", 10),
            new TestCase("1234 5673 4501 ", "1234 5678 901", 8, 3, 3, "1234 5673 4501 ", 12),
            new TestCase(
                "9876 5432 1098 7654 321",
                "1234 5678 9012 3456 789",
                0,
                23,
                19,
                "9876 5432 1098 7654 321",
                23
            )
        );

        testCases.forEach(testCase -> {
            FormatResult maskResult = StringFormatter.applyMask(
                maskCardNumber,
                testCase.newValue,
                testCase.oldValue,
                testCase.caretPosition,
                testCase.removedLength,
                testCase.insertedLength
            );
            assertNotNull(maskResult);

            assertEquals(testCase.expectedResult, maskResult.getFormattedResult());
            assertEquals((Integer) testCase.expectedCaretPosition, maskResult.getCursorIndex());
        });
    }
}