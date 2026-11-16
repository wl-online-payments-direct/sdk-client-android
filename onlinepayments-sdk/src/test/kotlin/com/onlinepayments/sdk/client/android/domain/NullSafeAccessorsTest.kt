package com.onlinepayments.sdk.client.android.domain

import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NullSafeAccessorsTest {
    private val gson = Gson()

    @Test
    fun `currency conversion without proposal and session id has null safe accessors`() {
        val response = gson.fromJson("""{"result":{"result":"NoRate"}}""", CurrencyConversionResponse::class.java)

        assertNull(response.dccSessionIdOrNull)
        assertNull(response.proposalOrNull)
    }

    @Test
    fun `currency conversion with proposal returns it through the safe accessors`() {
        val json = """
            {"dccSessionId":"session-1","result":{"result":"Allowed"},
             "proposal":{"baseAmount":{"amount":1298,"currencyCode":"EUR"},
                         "targetAmount":{"amount":1400,"currencyCode":"USD"},
                         "rate":{"exchangeRate":1.08,"invertedExchangeRate":0.92,"markUpRate":3.12,
                                 "quotationDateTime":"2026-01-01T10:00:00Z","source":"test"}}}
        """.trimIndent()

        val response = gson.fromJson(json, CurrencyConversionResponse::class.java)

        assertEquals("session-1", response.dccSessionIdOrNull)
        assertEquals("USD", response.proposalOrNull?.targetAmount?.currencyCode)
        assertEquals(response.proposal, response.proposalOrNull)
    }

    @Test
    fun `surcharge without amounts has null safe accessors`() {
        val json = """{"surcharges":[{"paymentProductId":1,"result":"NO_SURCHARGE"}]}"""

        val surcharge = gson.fromJson(json, SurchargeCalculationResponse::class.java).surcharges.first()

        assertNull(surcharge.netAmountOrNull)
        assertNull(surcharge.surchargeAmountOrNull)
        assertNull(surcharge.totalAmountOrNull)
    }

    @Test
    fun `surcharge with amounts returns them through the safe accessors`() {
        val json = """
            {"surcharges":[{"paymentProductId":1,"result":"OK",
              "netAmount":{"amount":1000,"currencyCode":"EUR"},
              "surchargeAmount":{"amount":30,"currencyCode":"EUR"},
              "totalAmount":{"amount":1030,"currencyCode":"EUR"}}]}
        """.trimIndent()

        val surcharge = gson.fromJson(json, SurchargeCalculationResponse::class.java).surcharges.first()

        assertNotNull(surcharge.surchargeAmountOrNull)
        assertEquals(1030L, surcharge.totalAmountOrNull?.amount)
        assertEquals(1000L, surcharge.netAmountOrNull?.amount)
    }
}
