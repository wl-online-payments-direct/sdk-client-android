/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.surcharge

import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeResult
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Junit Test class which tests surcharge calculation details response
 */
@RunWith(MockitoJUnitRunner::class)
class SurchargeCalculationResponseTest {
    private val scWithSurcharge: SurchargeCalculationResponse =
        GsonHelper.fromResourceJson<SurchargeCalculationResponse>(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )
    private val scWithNoSurcharge: SurchargeCalculationResponse =
        GsonHelper.fromResourceJson<SurchargeCalculationResponse>(
            "scWithNoSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

    @Test
    fun testEqualsSurchargeCalculationResponseWithSurcharge() {
        Assert.assertEquals(scWithSurcharge.surcharges.size.toLong(), 1)

        val surcharge = scWithSurcharge.surcharges.get(0)

        Assert.assertEquals(surcharge.paymentProductId.toLong(), 1)
        Assert.assertEquals(surcharge.result, SurchargeResult.OK)
        Assert.assertEquals(surcharge.netAmount.amount, 1000L)
        Assert.assertEquals(surcharge.netAmount.currencyCode, "EUR")
        Assert.assertEquals(surcharge.surchargeAmount.amount, 366L)
        Assert.assertEquals(surcharge.surchargeAmount.currencyCode, "EUR")
        Assert.assertEquals(surcharge.totalAmount.amount, 1366L)
        Assert.assertEquals(surcharge.totalAmount.currencyCode, "EUR")
        Assert.assertNotNull(surcharge.surchargeRate)
        Assert.assertEquals(surcharge.surchargeRate!!.surchargeProductTypeId, "PAYMENT_PRODUCT_TYPE_ID")
        Assert.assertEquals(surcharge.surchargeRate.surchargeProductTypeVersion, "1a2b3c-4d5e-6f7g8h-9i0j")
        Assert.assertEquals(surcharge.surchargeRate.adValoremRate, 3.3, 0.0)
        Assert.assertEquals(surcharge.surchargeRate.specificRate.toLong(), 333L)
    }

    @Test
    fun testEqualsSurchargeCalculationResponseWithNoSurcharge() {
        Assert.assertEquals(scWithNoSurcharge.surcharges.size.toLong(), 1)

        val surcharge = scWithNoSurcharge.surcharges.get(0)

        Assert.assertEquals(surcharge.paymentProductId.toLong(), 2)
        Assert.assertEquals(surcharge.result, SurchargeResult.NO_SURCHARGE)
        Assert.assertEquals(surcharge.netAmount.amount, 1000L)
        Assert.assertEquals(surcharge.netAmount.currencyCode, "EUR")
        Assert.assertEquals(surcharge.surchargeAmount.amount, 0L)
        Assert.assertEquals(surcharge.surchargeAmount.currencyCode, "EUR")
        Assert.assertEquals(surcharge.totalAmount.amount, 1000L)
        Assert.assertEquals(surcharge.totalAmount.currencyCode, "EUR")
        Assert.assertNull(surcharge.surchargeRate)
    }
}
