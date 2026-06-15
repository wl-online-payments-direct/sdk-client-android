/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.BeforeTest

/**
 * Base class for mock-backed integration tests.
 *
 * Unlike [BaseIntegrationTest], this class does NOT require live credentials — tests
 * extend this when they use [com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper]
 * to serve controlled responses.
 *
 * Each test class is responsible for starting its own [MockWebServer] and calling
 * [MockWebServer.shutdown] in an @After method or finally block.
 */
@RunWith(RobolectricTestRunner::class)
abstract class BaseMockIntegrationTest {

    protected lateinit var context: Context
    protected lateinit var paymentContext: PaymentContextWithAmount

    @BeforeTest
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        paymentContext = PaymentContextWithAmount(
            amountOfMoney = AmountOfMoneyWithAmount(1000L, "EUR"),
            countryCode = "NL",
            isRecurring = false
        )
    }

    /**
     * Create a payment context with custom amount.
     */
    protected fun createPaymentContext(
        amount: Long,
        currencyCode: String = "EUR",
        countryCode: String = "NL"
    ): PaymentContext {
        return PaymentContext(
            amountOfMoney = AmountOfMoney(amount, currencyCode),
            countryCode = countryCode,
            isRecurring = false
        )
    }
}