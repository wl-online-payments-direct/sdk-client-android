/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.iin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Junit Test class which tests iin details response equality
 */
@RunWith(MockitoJUnitRunner.class)
public class IinDetailsResponseJavaTest {

    private final IinDetailsResponse fstNormalVisa = GsonHelperJava.fromResourceJson(
        "normalIINResponseVisa.json",
        IinDetailsResponse.class
    );
    private final IinDetailsResponse sndNormalVisa = GsonHelperJava.fromResourceJson(
        "normalIINResponseVisa.json",
        IinDetailsResponse.class
    );

    private final IinDetailsResponse fstNormalMC = GsonHelperJava.fromResourceJson(
        "normalIINResponseMC.json",
        IinDetailsResponse.class
    );

    private final IinDetailsResponse fstMinimalVisa = GsonHelperJava.fromResourceJson(
        "minimalIINResponseVisa.json",
        IinDetailsResponse.class
    );
    private final IinDetailsResponse sndMinimalVisa = GsonHelperJava.fromResourceJson(
        "minimalIINResponseVisa.json",
        IinDetailsResponse.class
    );

    private final IinDetailsResponse fstMinimalMC = GsonHelperJava.fromResourceJson(
        "minimalIINResponseMC.json",
        IinDetailsResponse.class
    );

    private final IinDetailsResponse fstEmptyWithCodeUnknown = new IinDetailsResponse(IinStatus.UNKNOWN);
    private final IinDetailsResponse sndEmptyWithCodeUnknown = new IinDetailsResponse(IinStatus.UNKNOWN);
    private final IinDetailsResponse fstEmptyWithCodeSupported = new IinDetailsResponse(IinStatus.SUPPORTED);
    private final IinDetailsResponse fstEmptyWithCodeExistingButNotAllowed = new IinDetailsResponse(
        IinStatus.EXISTING_BUT_NOT_ALLOWED);
    private final IinDetailsResponse fstEmptyWithCodeNotEnoughDigits = new IinDetailsResponse(
        IinStatus.NOT_ENOUGH_DIGITS);

    private final IinDetailsResponse fstNormalResponseVisaNoCoBrands = GsonHelperJava.fromResourceJson(
        "normalIINResponseVisaNoCoBrand.json",
        IinDetailsResponse.class
    );

    @Test
    public void testEqualsIinDetailsResponse() {
        // Test equality of two normal IinResponses
        assertEquals(fstNormalVisa, sndNormalVisa);
        assertEquals(sndNormalVisa, fstNormalVisa);

        // Test inequality of two normal IinResponses
        assertNotEquals(fstNormalMC, fstNormalVisa);

        // Test equality of two minimal IinResponses
        assertEquals(fstMinimalVisa, sndMinimalVisa);
        assertEquals(sndMinimalVisa, fstMinimalVisa);

        // Test inequality of two minimal IinResponses
        assertNotEquals(fstMinimalVisa, fstMinimalMC);

        // Test inequality of normal and minimal IinResponses
        assertNotEquals(fstNormalVisa, fstMinimalVisa);
        assertNotEquals(fstNormalMC, fstMinimalMC);

        // Test (in)equality of empty with different status codes
        assertEquals(fstEmptyWithCodeUnknown, sndEmptyWithCodeUnknown);
        assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeExistingButNotAllowed);
        assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeNotEnoughDigits);
        assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeSupported);

        // Test inequality of normals response with and without co-brands
        assertNotEquals(fstNormalVisa, fstNormalResponseVisaNoCoBrands);

        // Test null
        assertNotNull(fstNormalVisa);
        assertNotNull(fstEmptyWithCodeUnknown);
        assertNotNull(fstEmptyWithCodeSupported);
    }

    @Test
    public void testCardType() {
        assertEquals(fstNormalVisa.getCardType(), CardType.DEBIT);
        assertEquals(fstMinimalVisa.getCardType(), CardType.DEBIT);
        assertEquals(fstNormalResponseVisaNoCoBrands.getCardType(), CardType.DEBIT);
        assertEquals(sndNormalVisa.getCardType(), CardType.DEBIT);
        assertEquals(sndMinimalVisa.getCardType(), CardType.DEBIT);

        assertEquals(fstNormalMC.getCardType(), CardType.CREDIT);
        assertEquals(fstMinimalMC.getCardType(), CardType.CREDIT);
    }
}
