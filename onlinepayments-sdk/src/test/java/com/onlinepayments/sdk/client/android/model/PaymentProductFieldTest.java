package com.onlinepayments.sdk.client.android.model;

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Junit Testclass which tests PaymentProductField apply-/removeMask methods with no Mask present
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProductFieldTest {

    private final PaymentProductField paymentProductFieldWithoutMask = GsonHelper.fromResourceJson("paymentProductFieldWithoutMask.json", PaymentProductField.class);
    private final PaymentProductField paymentProductFieldWithMask = GsonHelper.fromResourceJson("paymentProductFieldWithMask.json", PaymentProductField.class);


    @Test
    public void testApplyMaskingMethodsInputExpiry() {
        String oldValue = "";
        String newValue = "1";
        FormatResult expectedResult = new FormatResult("1", 1);
        testMaskingFor(expectedResult, newValue, oldValue, 0, 0, 1);

        oldValue = "1";
        newValue = "12";
        expectedResult = new FormatResult("12/", 3);
        testMaskingFor(expectedResult, newValue, oldValue, 1, 0, 1);

        // The step 12 to 12/ is not done by the user, but the result of the masking step above.

        oldValue = "12/";
        newValue = "12/3";
        expectedResult = new FormatResult("12/3", 4);
        testMaskingFor(expectedResult, newValue, oldValue, 3, 0, 1);

        oldValue = "12/3";
        newValue = "12/34";
        expectedResult = new FormatResult("12/34", 5);
        testMaskingFor(expectedResult, newValue, oldValue, 4, 0, 1);
    }

    @Test
    public void testMaskingWhenPastingExpiry() {
        FormatResult expectedResult = new FormatResult("12/34", 5);
        testMaskingFor(expectedResult, "12/34", "", 0, 0, 5);
        testMaskingFor(expectedResult, "1234", "", 0, 0, 4);

        expectedResult = new FormatResult("12/3", 4);
        testMaskingFor(expectedResult, "12/3", "", 0, 0, 4);
        testMaskingFor(expectedResult, "123", "", 0, 0, 3);

        expectedResult = new FormatResult("12/", 3);
        testMaskingFor(expectedResult, "12/", "", 0, 0, 3);
        testMaskingFor(expectedResult, "12", "", 0, 0, 2);
    }

    @Test
    public void testMaskingForChunkRemoval() {
        FormatResult expectedResult = new FormatResult("12/", 2);
        testMaskingFor(expectedResult, "12", "12/35", 2, 3, 0);

        expectedResult = new FormatResult("12/", 3);
        testMaskingFor(expectedResult, "12/", "12/35", 3, 3, 0);

        expectedResult = new FormatResult("1", 1);
        testMaskingFor(expectedResult, "1", "12/34", 1, 4, 0);

        expectedResult = new FormatResult("", 0);
        testMaskingFor(expectedResult, "", "12/34", 0, 5, 0);
    }

    @Test
    public void testApplyMaskingMethodsRemove() {
        String oldValue = "12/34";
        String newValue = "12/3";
        FormatResult expectedFormatResult = new FormatResult("12/3", 4);
        testMaskingFor(expectedFormatResult, newValue, oldValue, 4, 1, 0);

        oldValue = "12/3";
        newValue = "12/";
        expectedFormatResult = new FormatResult("12/", 3);
        testMaskingFor(expectedFormatResult, newValue, oldValue, 3, 1, 0);

        // Removing the slash effectively only moves the cursor.
        // The mask returns a String with slash, but the next delete will delete the '2' because the cursor moved. This removes the slash.
        oldValue = "12/";
        newValue = "12";
        expectedFormatResult = new FormatResult("12/", 2);
        testMaskingFor(expectedFormatResult, newValue, oldValue, 2, 1, 0);

        oldValue = "12/";
        newValue = "1/";
        expectedFormatResult = new FormatResult("1", 1);
        testMaskingFor(expectedFormatResult, newValue, oldValue, 1, 1, 0);

        oldValue = "1";
        newValue = "";
        expectedFormatResult = new FormatResult("", 0);
        testMaskingFor(expectedFormatResult, newValue, oldValue, 0, 1, 0);
    }

    @Test
    public void testMaskingSingleAddedCharacterInclCursor() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("123", "12", 2, 0, 1);
        validateFormatResult(result, "123", 3);
    }

    @Test
    public void testMaskingTwoAddedCharactersInclCursor() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("123", "1", 1, 0, 2);
        validateFormatResult(result, "123", 3);
    }

    @Test
    public void testMaskingSingleRemovedCharacterInclCursor() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("12", "123", 2, 1, 0);
        validateFormatResult(result, "12", 2);
    }

    @Test
    public void testMaskingTwoRemovedCharactersInclCursor() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("1", "123", 1, 2, 0);
        validateFormatResult(result, "1", 1);
    }


    @Test
    public void testMaskingSingleAddedCharacterInclCursorDeprecatedVersion() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("123", "12", 2);
        validateFormatResult(result, "123", 3);
    }

    @Test
    public void testMaskingTwoAddedCharactersInclCursorDeprecatedVersion() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("123", "1", 1);
        validateFormatResult(result, "123", 3);
    }

    @Test
    public void testMaskingSingleRemovedCharacterInclCursorDeprecatedVersion() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("12", "123", 2);
        validateFormatResult(result, "12",2);
    }

    @Test
    public void testMaskingTwoRemovedCharactersInclCursorDeprecatedVersion() {
        FormatResult result = paymentProductFieldWithoutMask.applyMask("1", "123", 1);
        validateFormatResult(result, "1", 1);
    }


    @Test
    public void testMaskingValueOnly() {
        String result = paymentProductFieldWithoutMask.applyMask("123");
        assertEquals("123", result);
    }

    @Test
    public void testUnmaskingValueOnly() {
        String result = paymentProductFieldWithoutMask.applyMask("123");
        assertEquals("123", result);
    }

    private void testMaskingFor(FormatResult expectedFormatResult, String newValue, String oldValue, int indexOfChange, int lengthOfAffectedString, int lengthOfChange) {
        FormatResult result = paymentProductFieldWithMask.applyMask(newValue, oldValue, indexOfChange);
        validateFormatResult(result, expectedFormatResult.getFormattedResult(), expectedFormatResult.getCursorIndex());

        result = paymentProductFieldWithMask.applyMask(newValue, oldValue, indexOfChange, lengthOfAffectedString, lengthOfChange);
        validateFormatResult(result, expectedFormatResult.getFormattedResult(), expectedFormatResult.getCursorIndex());

        String strResult = paymentProductFieldWithMask.applyMask(newValue);
        assertEquals(expectedFormatResult.getFormattedResult(), strResult);
    }

    private void validateFormatResult(FormatResult result, String expectedFormattedResult, Integer expectedCursorIndex) {
        assertEquals(expectedFormattedResult, result.getFormattedResult());
        assertEquals(expectedCursorIndex, result.getCursorIndex());
    }

}
