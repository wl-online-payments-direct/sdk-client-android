/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.helpers

import com.onlinepayments.sdk.client.android.infrastructure.utils.StringFormatter
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Junit Test class which tests masking and unmasking functionality
 */
@RunWith(MockitoJUnitRunner::class)
class StringFormatterTest {
    data class TestCase(
        val newValue: String,
        val oldValue: String,
        val caretPosition: Int,
        val removedLength: Int,
        val insertedLength: Int,
        val expectedResult: String,
        val expectedCaretPosition: Int
    )

    private val maskExpiryDate = "{{99}}-{{99}}"
    private val maskCardNumber = "{{9999}} {{9999}} {{9999}} {{9999}} {{999}}"
    private val maskNumbers = "{{99}} {{99}} {{99}} {{99}} {{99}}"
    private val maskWildcards = "{{**}} {{**}} {{**}} {{**}} {{**}}"
    private val maskAlpha = "{{aa}} {{aa}} {{aa}} {{aa}} {{aa}}"

    private val maskNumbersString = "1234567890"
    private val maskWildcardsString = "!!!!!!!!!!"
    private val maskAlphaString = "abcdefghij"

    @Test
    fun testMaskSingleCharacter() {
        val maskedValue = StringFormatter.applyMask(maskExpiryDate, "1")
        assertEquals("1", maskedValue)
    }

    @Test
    fun testMaskingCreditCard() {
        val maskedValue = StringFormatter.applyMask(maskCardNumber, "411111XXXXXX1111")
        assertEquals("4111 11XX XXXX 1111", maskedValue)
    }

    @Test
    fun testRemoveMaskFromCreditCard() {
        val maskedValue = StringFormatter.removeMask(maskCardNumber, "4111 11XX XXXX 1111")
        assertEquals("411111XXXXXX1111", maskedValue)
    }

    @Test
    fun testMaskTwoCharacters() {
        val maskedValue = StringFormatter.applyMask(maskExpiryDate, "12")
        assertEquals("12-", maskedValue)
    }

    @Test
    fun testMaskFourCharacters() {
        val maskedValue = StringFormatter.applyMask(maskExpiryDate, "1234")
        assertEquals("12-34", maskedValue)
    }

    @Test
    fun testMaskTooManyCharacters() {
        val maskedValue = StringFormatter.applyMask(maskExpiryDate, "1234567890123456789")
        assertEquals("12-34", maskedValue)
    }

    @Test
    fun testMaskNumbers() {
        val maskedValue = StringFormatter.applyMask(maskNumbers, maskNumbersString)
        assertEquals("12 34 56 78 90", maskedValue)
    }

    @Test
    fun testMaskWildcards() {
        val maskedValue = StringFormatter.applyMask(maskWildcards, maskWildcardsString)
        assertEquals("!! !! !! !! !!", maskedValue)
    }

    @Test
    fun testMaskAlpha() {
        val maskedValue = StringFormatter.applyMask(maskAlpha, maskAlphaString)
        assertEquals("ab cd ef gh ij", maskedValue)
    }

    @Test
    fun testMaskWithCursorPosition() {
        val formatResult = StringFormatter.applyMask(maskAlpha, maskAlphaString, 1)
        assertEquals("ab cd ef gh ij", formatResult!!.formattedResult)
        assertEquals(1, formatResult.cursorIndex!!.toLong())
    }

    @Test
    fun testUnmaskingSingleCharacter() {
        val maskedValue = StringFormatter.removeMask(maskExpiryDate, "1")
        assertEquals("1", maskedValue)
    }

    @Test
    fun testUnmaskingMaskedCharacters() {
        val maskedValue = StringFormatter.removeMask(maskExpiryDate, "12-34")
        assertEquals("1234", maskedValue)
    }

    @Test
    fun testApplyMaskWithTextWatcherBeforeTextChangedInformationCreditCardNumber() {
        (getAddedTextTests() + getRemovedTextTests() + getReplacedTextCases()).forEach { test ->
            val maskResult = StringFormatter.applyMask(
                maskCardNumber,
                test.newValue,
                test.oldValue,
                test.caretPosition,
                test.removedLength,
                test.insertedLength
            )

            assertNotNull(maskResult)
            assertEquals(test.expectedResult, maskResult.formattedResult)
            assertEquals(test.expectedCaretPosition, maskResult.cursorIndex)
        }
    }

    private fun getAddedTextTests(): List<TestCase> {
        return listOf(
            // *** Simple add-to-the-end-tests ***
            // Test typing a single digit in the EditText
            TestCase("1", "", 0, 0, 1, "1", 1),

            // Test pasting 2 digits in the EditText
            TestCase("12", "", 0, 0, 2, "12", 2),

            // Test pasting 4 digits in the EditText
            TestCase("1234", "", 0, 0, 4, "1234 ", 5),

            // Test pasting a full number in the EditText
            TestCase("1234567890123456789", "", 0, 0, 19, "1234 5678 9012 3456 789", 23),

            // Test typing a single digit after two digits in the old value
            TestCase("123", "12", 2, 0, 1, "123", 3),

            // Test pasting 2 digits after two digits in the old value
            TestCase("1234", "12", 2, 0, 2, "1234 ", 5),

            // Test pasting 8 digits after two digits in the old value
            TestCase("1234567890", "12", 2, 0, 8, "1234 5678 90", 12),

            // Test typing a single digit after an already masked old value of "1234 "
            TestCase("1234 5", "1234 ", 5, 0, 1, "1234 5", 6),

            // Test typing a single digit right before a mask character
            TestCase("12345 ", "1234 ", 4, 0, 1, "1234 5", 6),

            // Test pasting two digits right before a mask character
            TestCase("123456 ", "1234 ", 4, 0, 2, "1234 56", 7),

            // Test pasting four digits right before a mask character in a String containing multiple mask characters
            TestCase("12340123 5678 9", "1234 5678 9", 4, 0, 4, "1234 0123 5678 9", 10),

            // Test typing a single digit after an already masked old value of "1234 567"
            TestCase("1234 5678", "1234 567", 8, 0, 1, "1234 5678 ", 10),

            // Test pasting 8 digits after an already masked old value of "1234 5678 901"
            TestCase("1234 5678 90123456789", "1234 5678 901", 13, 0, 8, "1234 5678 9012 3456 789", 23),

            // *** Adding in between other characters tests ***
            // Test adding a single digit between two digits
            TestCase("132", "12", 1, 0, 1, "132", 2),

            // Test adding a single digit between three digits
            TestCase("1423", "123", 1, 0, 1, "1423 ", 2),

            // Test adding two digits between two other digits
            TestCase("1342", "12", 1, 0, 2, "1342 ", 3),

            // Test adding 8 digits between two other digits
            TestCase("1345678902", "12", 1, 0, 8, "1345 6789 02", 11),

            // Test adding non-numerical characters
            TestCase("1a2bc3d4e5", "", 0, 0, 10, "1234 5", 6),

            // Test adding a trailing space
            TestCase("1234 ", "12", 2, 0, 3, "1234 ", 5),

            // Test adding a full credit card number that is already masked
            TestCase("1234 5678 9012 3456 789", "0", 0, 0, 23, "1234 5678 9012 3456 789", 23),

            // Test adding a single digit in front of four other digits
            TestCase("01234 ", "1234 ", 0, 0, 1, "0123 4", 1),

            // Test adding a single digit between two mask characters; test added due to find during monkey testing
            TestCase("1234 56278 901", "1234 5678 901", 7, 0, 1, "1234 5627 8901 ", 8)
        )
    }

    private fun getRemovedTextTests(): List<TestCase> {
        return listOf(
            // *** Test removing the entire entered String ***
            // Test removing a single character
            TestCase("", "1", 0, 1, 0, "", 0),

            // Test removing two characters
            TestCase("", "12", 0, 2, 0, "", 0),

            // Test removing six characters
            TestCase("", "1234 56", 0, 7, 0, "", 0),

            // *** Test removing the first character of a String ***
            // Test removing only the first character
            TestCase("23", "123", 0, 1, 0, "23", 0),

            // Test removing only the first character in a String containing a mask character
            TestCase("234 56", "1234 56", 0, 1, 0, "2345 6", 0),

            // Test removing only the first character in a String containing multiple mask characters
            TestCase("234 5678 9012 3456 789", "1234 5678 9012 3456 789", 0, 1, 0, "2345 6789 0123 4567 89", 0),

            // Test removing the first two characters
            TestCase("3", "123", 0, 2, 0, "3", 0),

            // Test removing the first two characters in a String that contains mask characters
            TestCase("34 5678 ", "1234 5678 ", 0, 2, 0, "3456 78", 0),

            // *** Test removing characters from the end of the String ***
            // Test remove single character from the end of a String
            TestCase("12", "123", 2, 1, 0, "12", 2),

            // Test remove two characters from the end of a String
            TestCase("1", "123", 1, 2, 0, "1", 1),

            // Test remove a character from the end of a String "across" a mask character
            TestCase("1234", "1234 ", 4, 1, 0, "1234 ", 4),

            // Test remove multiple characters from the end of a String "across" a mask character
            TestCase("123", "1234 5", 3, 3, 0, "123", 3),

            // Test remove multiple characters from the end of a String, ending "behind" a mask character
            TestCase("1234", "1234 56", 4, 3, 0, "1234 ", 4),

            // Test remove multiple characters from the end of a String, ending "before" a mask character
            TestCase("1234 ", "1234 56", 5, 2, 0, "1234 ", 5),

            // Test remove 19 characters from the end of a String "across" multiple mask characters
            TestCase("123", "1234 5678 9012 3456 78", 3, 19, 0, "123", 3),

            // Test remove 18 characters from the end of a String "across" multiple mask characters, ending "behind" a mask character
            TestCase("1234", "1234 5678 9012 3456 78", 4, 18, 0, "1234 ", 4),

            // Test remove 17 characters from the end of a String "across" multiple mask characters, ending "before" a mask character
            TestCase("1234 ", "1234 5678 9012 3456 78", 5, 17, 0, "1234 ", 5),

            // *** Test remove characters within the string ***
            // Test removing a single character in a String of only 3 characters
            TestCase("13", "123", 1, 1, 0, "13", 1),

            // Test removing a single character in a String of 4 characters
            TestCase("134", "1234 ", 1, 1, 0, "134", 1),

            // Test removing a single character that is before a mask character
            TestCase("123 ", "1234 ", 3, 1, 0, "123", 3),

            // Test removing multiple characters before a mask character
            TestCase("12 ", "1234 ", 2, 2, 0, "12", 2),

            // Test removing multiple characters inside a longer String, "across" a mask character
            TestCase("12378 9", "1234 5678 9", 3, 4, 0, "1237 89", 3),

            // Test removing a single character with the cursor after a mask character
            TestCase("123456", "1234 56", 4, 1, 0, "1234 56", 4)
        )
    }

    private fun getReplacedTextCases(): List<TestCase> {
        return listOf(
            // *** Test simple replacements that do not involve masking ***
            // Test replace a character by another
            TestCase("2", "1", 0, 1, 1, "2", 1),

            // Test replace two characters by two others
            TestCase("34", "12", 0, 2, 2, "34", 2),

            // Test replace one character by two others
            TestCase("23", "1", 0, 1, 2, "23", 2),

            // Test replace three characters by one other
            TestCase("4", "123", 0, 3, 1, "4", 1),

            // *** Test more complex replacements that touch mask characters ***
            // Test replace the character before a mask character
            TestCase("1235 ", "1234 ", 3, 1, 1, "1235 ", 5),

            // Test replace the two characters before a mask character by a single character
            TestCase("125 ", "1234 ", 2, 2, 1, "125", 3),

            // Test replace the character before a mask character along with the mask character
            TestCase("1235", "1234 ", 3, 2, 1, "1235 ", 5),

            // Test replace the two characters before a mask character, along with the mask character, by a single character
            TestCase("125", "1234 ", 2, 3, 1, "125", 3),

            // Test replace the two characters before a mask character by two characters
            TestCase("1256 ", "1234 ", 2, 2, 2, "1256 ", 5),

            // Test replace the two characters before a mask character, along with the mask character, by two characters
            TestCase("1256", "1234 ", 2, 3, 2, "1256 ", 5),

            // Test replace all characters before a mask character by four other characters
            TestCase("5678 ", "1234 ", 0, 4, 4, "5678 ", 5),

            // Test replace all characters, including the mask character by four other characters
            TestCase("5678", "1234 ", 0, 5, 4, "5678 ", 5),

            // Test replace the mask character by another character
            TestCase("12345", "1234 ", 4, 1, 1, "1234 5", 6),

            // Test replace the character before the mask character and the mask character by two other characters
            TestCase("12356", "1234 ", 3, 2, 2, "1235 6", 6),

            // *** Test replacements with selections that span across mask characters ***
            // Test replace a String with mask character by a single character
            TestCase("6", "1234 5", 0, 6, 1, "6", 1),

            // Test replace a String with mask character by four characters
            TestCase("6789", "1234 5", 0, 6, 4, "6789 ", 5),

            // Test replace a subString "across" a mask character
            TestCase("1237896", "1234 56", 3, 3, 3, "1237 896", 7),

            // Test replace a subString "across" a mask character, where the cursor ends near a mask character
            TestCase("123789016", "1234 56", 3, 3, 5, "1237 8901 6", 10),

            // Test replace a subString "across" a mask character, after another mask character which remains untouched
            TestCase("1234 56734501", "1234 5678 901", 8, 3, 3, "1234 5673 4501 ", 12),

            // Test replace a full credit card number, including masks, by another full credit card number
            TestCase("9876543210987654321", "1234 5678 9012 3456 789", 0, 23, 19, "9876 5432 1098 7654 321", 23)
        )
    }
}