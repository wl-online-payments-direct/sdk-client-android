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

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Junit Test class which tests PaymentProductField apply-/removeMask methods with no Mask present
 */
@RunWith(MockitoJUnitRunner::class)
class PaymentProductFieldTest {
    private val paymentProductFieldWithoutMask: PaymentProductField = GsonHelper.fromResourceJson<PaymentProductField>(
        "paymentProductFieldWithoutMask.json",
        PaymentProductField::class.java
    )

    private val paymentProductFieldWithMask: PaymentProductField = GsonHelper.fromResourceJson<PaymentProductField>(
        "paymentProductFieldWithMask.json",
        PaymentProductField::class.java
    )

    @Test
    fun testApplyMaskingMethodsInputExpiry() {
        var oldValue = ""
        var newValue = "1"
        var expectedResult = FormatResult("1", 1)
        testMaskingFor(expectedResult, newValue, oldValue, 0, 0, 1)

        oldValue = "1"
        newValue = "12"
        expectedResult = FormatResult("12/", 3)
        testMaskingFor(expectedResult, newValue, oldValue, 1, 0, 1)

        // The step 12 to 12/ is not done by the user, but the result of the masking step above.
        oldValue = "12/"
        newValue = "12/3"
        expectedResult = FormatResult("12/3", 4)
        testMaskingFor(expectedResult, newValue, oldValue, 3, 0, 1)

        oldValue = "12/3"
        newValue = "12/34"
        expectedResult = FormatResult("12/34", 5)
        testMaskingFor(expectedResult, newValue, oldValue, 4, 0, 1)
    }

    @Test
    fun testMaskingWhenPastingExpiry() {
        var expectedResult = FormatResult("12/34", 5)
        testMaskingFor(expectedResult, "12/34", "", 0, 0, 5)
        testMaskingFor(expectedResult, "1234", "", 0, 0, 4)

        expectedResult = FormatResult("12/3", 4)
        testMaskingFor(expectedResult, "12/3", "", 0, 0, 4)
        testMaskingFor(expectedResult, "123", "", 0, 0, 3)

        expectedResult = FormatResult("12/", 3)
        testMaskingFor(expectedResult, "12/", "", 0, 0, 3)
        testMaskingFor(expectedResult, "12", "", 0, 0, 2)
    }

    @Test
    fun testMaskingForChunkRemoval() {
        var expectedResult = FormatResult("12/", 2)
        testMaskingFor(expectedResult, "12", "12/35", 2, 3, 0)

        expectedResult = FormatResult("12/", 3)
        testMaskingFor(expectedResult, "12/", "12/35", 3, 3, 0)

        expectedResult = FormatResult("1", 1)
        testMaskingFor(expectedResult, "1", "12/34", 1, 4, 0)

        expectedResult = FormatResult("", 0)
        testMaskingFor(expectedResult, "", "12/34", 0, 5, 0)
    }

    @Test
    fun testApplyMaskingMethodsRemove() {
        var oldValue = "12/34"
        var newValue = "12/3"
        var expectedFormatResult = FormatResult("12/3", 4)
        testMaskingFor(expectedFormatResult, newValue, oldValue, 4, 1, 0)

        oldValue = "12/3"
        newValue = "12/"
        expectedFormatResult = FormatResult("12/", 3)
        testMaskingFor(expectedFormatResult, newValue, oldValue, 3, 1, 0)

        // Removing the slash effectively only moves the cursor.
        // The mask returns a String with slash, but the next delete will delete the '2' because the cursor moved. This removes the slash.
        oldValue = "12/"
        newValue = "12"
        expectedFormatResult = FormatResult("12/", 2)
        testMaskingFor(expectedFormatResult, newValue, oldValue, 2, 1, 0)

        oldValue = "12/"
        newValue = "1/"
        expectedFormatResult = FormatResult("1", 1)
        testMaskingFor(expectedFormatResult, newValue, oldValue, 1, 1, 0)

        oldValue = "1"
        newValue = ""
        expectedFormatResult = FormatResult("", 0)
        testMaskingFor(expectedFormatResult, newValue, oldValue, 0, 1, 0)
    }

    @Test
    fun testMaskingSingleAddedCharacterInclCursor() {
        val result = paymentProductFieldWithoutMask.applyMask("123", "12", 2, 0, 1)
        validateFormatResult(result!!, "123", 3)
    }

    @Test
    fun testMaskingTwoAddedCharactersInclCursor() {
        val result = paymentProductFieldWithoutMask.applyMask("123", "1", 1, 0, 2)
        validateFormatResult(result!!, "123", 3)
    }

    @Test
    fun testMaskingSingleRemovedCharacterInclCursor() {
        val result = paymentProductFieldWithoutMask.applyMask("12", "123", 2, 1, 0)
        validateFormatResult(result!!, "12", 2)
    }

    @Test
    fun testMaskingTwoRemovedCharactersInclCursor() {
        val result = paymentProductFieldWithoutMask.applyMask("1", "123", 1, 2, 0)
        validateFormatResult(result!!, "1", 1)
    }

    @Test
    fun testMaskingSingleAddedCharacterInclCursorDeprecatedVersion() {
        val result = paymentProductFieldWithoutMask.applyMask("123", "12", 2)
        validateFormatResult(result!!, "123", 3)
    }

    @Test
    fun testMaskingTwoAddedCharactersInclCursorDeprecatedVersion() {
        val result = paymentProductFieldWithoutMask.applyMask("123", "1", 1)
        validateFormatResult(result!!, "123", 3)
    }

    @Test
    fun testMaskingSingleRemovedCharacterInclCursorDeprecatedVersion() {
        val result = paymentProductFieldWithoutMask.applyMask("12", "123", 2)
        validateFormatResult(result!!, "12", 2)
    }

    @Test
    fun testMaskingTwoRemovedCharactersInclCursorDeprecatedVersion() {
        val result = paymentProductFieldWithoutMask.applyMask("1", "123", 1)
        validateFormatResult(result!!, "1", 1)
    }

    @Test
    fun testMaskingValueOnly() {
        val result = paymentProductFieldWithoutMask.applyMask("123")
        Assert.assertEquals("123", result)
    }

    @Test
    fun testUnmaskingValueOnly() {
        val result = paymentProductFieldWithoutMask.applyMask("123")
        Assert.assertEquals("123", result)
    }

    private fun testMaskingFor(
        expectedFormatResult: FormatResult,
        newValue: String,
        oldValue: String,
        indexOfChange: Int,
        lengthOfAffectedString: Int,
        lengthOfChange: Int
    ) {
        var result = paymentProductFieldWithMask.applyMask(newValue, oldValue, indexOfChange)
        validateFormatResult(result!!, expectedFormatResult.formattedResult, expectedFormatResult.cursorIndex)

        result = paymentProductFieldWithMask.applyMask(
            newValue,
            oldValue,
            indexOfChange,
            lengthOfAffectedString,
            lengthOfChange
        )
        validateFormatResult(result!!, expectedFormatResult.formattedResult, expectedFormatResult.cursorIndex)

        val strResult = paymentProductFieldWithMask.applyMask(newValue)
        Assert.assertEquals(expectedFormatResult.formattedResult, strResult)
    }

    private fun validateFormatResult(
        result: FormatResult,
        expectedFormattedResult: String?,
        expectedCursorIndex: Int?
    ) {
        Assert.assertEquals(expectedFormattedResult, result.formattedResult)
        Assert.assertEquals(expectedCursorIndex, result.cursorIndex)
    }
}
