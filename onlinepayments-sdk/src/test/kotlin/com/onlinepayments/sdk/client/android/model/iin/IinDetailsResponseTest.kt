/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.iin

import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Junit Test class which tests iin details response equality
 */
@RunWith(MockitoJUnitRunner::class)
class IinDetailsResponseTest {
    private val fstNormalVisa: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("normalIINResponseVisa.json", IinDetailsResponse::class.java)
    private val sndNormalVisa: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("normalIINResponseVisa.json", IinDetailsResponse::class.java)

    private val fstNormalMC: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("normalIINResponseMC.json", IinDetailsResponse::class.java)

    private val fstMinimalVisa: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("minimalIINResponseVisa.json", IinDetailsResponse::class.java)
    private val sndMinimalVisa: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("minimalIINResponseVisa.json", IinDetailsResponse::class.java)

    private val fstMinimalMC: IinDetailsResponse =
        GsonHelper.fromResourceJson<IinDetailsResponse>("minimalIINResponseMC.json", IinDetailsResponse::class.java)

    private val fstEmptyWithCodeUnknown = IinDetailsResponse(IinStatus.UNKNOWN)
    private val sndEmptyWithCodeUnknown = IinDetailsResponse(IinStatus.UNKNOWN)
    private val fstEmptyWithCodeSupported = IinDetailsResponse(IinStatus.SUPPORTED)
    private val fstEmptyWithCodeExistingButNotAllowed = IinDetailsResponse(IinStatus.EXISTING_BUT_NOT_ALLOWED)
    private val fstEmptyWithCodeNotEnoughDigits = IinDetailsResponse(IinStatus.NOT_ENOUGH_DIGITS)

    private val fstNormalResponseVisaNoCoBrands: IinDetailsResponse = GsonHelper.fromResourceJson<IinDetailsResponse>(
        "normalIINResponseVisaNoCoBrand.json",
        IinDetailsResponse::class.java
    )

    @Test
    fun testEqualsIinDetailsResponse() {
        // Test equality of two normal IinResponses
        Assert.assertEquals(fstNormalVisa, sndNormalVisa)
        Assert.assertEquals(sndNormalVisa, fstNormalVisa)

        // Test inequality of two normal IinResponses
        Assert.assertNotEquals(fstNormalMC, fstNormalVisa)

        // Test equality of two minimal IinResponses
        Assert.assertEquals(fstMinimalVisa, sndMinimalVisa)
        Assert.assertEquals(sndMinimalVisa, fstMinimalVisa)

        // Test inequality of two minimal IinResponses
        Assert.assertNotEquals(fstMinimalVisa, fstMinimalMC)

        // Test inequality of normal and minimal IinResponses
        Assert.assertNotEquals(fstNormalVisa, fstMinimalVisa)
        Assert.assertNotEquals(fstNormalMC, fstMinimalMC)

        // Test (in)equality of empty with different status codes
        Assert.assertEquals(fstEmptyWithCodeUnknown, sndEmptyWithCodeUnknown)
        Assert.assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeExistingButNotAllowed)
        Assert.assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeNotEnoughDigits)
        Assert.assertNotEquals(fstEmptyWithCodeUnknown, fstEmptyWithCodeSupported)

        // Test inequality of normals response with and without co-brands
        Assert.assertNotEquals(fstNormalVisa, fstNormalResponseVisaNoCoBrands)

        // Test null
        Assert.assertNotNull(fstNormalVisa)
        Assert.assertNotNull(fstEmptyWithCodeUnknown)
        Assert.assertNotNull(fstEmptyWithCodeSupported)
    }

    @Test
    fun testCardType() {
        Assert.assertEquals(fstNormalVisa.cardType, CardType.DEBIT)
        Assert.assertEquals(fstMinimalVisa.cardType, CardType.DEBIT)
        Assert.assertEquals(fstNormalResponseVisaNoCoBrands.cardType, CardType.DEBIT)
        Assert.assertEquals(sndNormalVisa.cardType, CardType.DEBIT)
        Assert.assertEquals(sndMinimalVisa.cardType, CardType.DEBIT)

        Assert.assertEquals(fstNormalMC.cardType, CardType.CREDIT)
        Assert.assertEquals(fstMinimalMC.cardType, CardType.CREDIT)
    }
}
