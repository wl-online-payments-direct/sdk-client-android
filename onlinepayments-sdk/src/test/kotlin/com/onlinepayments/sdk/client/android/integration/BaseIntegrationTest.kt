/*
 * Do not remove or alter the notices in this preamble.
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
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import com.onlinepayments.sdk.client.android.integration.utils.ServerApiHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.BeforeTest

/**
 * Base class for integration tests.
 * Sets up the SDK with real session data and provides common test utilities.
 */
@RunWith(RobolectricTestRunner::class)
abstract class BaseIntegrationTest {

    protected lateinit var sdk: OnlinePaymentsSdk
    protected lateinit var context: Context
    protected lateinit var paymentContext: PaymentContextWithAmount

    companion object {
        @BeforeClass
        @JvmStatic
        fun checkCredentials() {
            // Skip all integration tests if credentials are not available
            assumeTrue(
                "Integration tests skipped: credentials not configured. " +
                    "Please set up local.properties with required credentials.",
                TestConfig.canRunIntegrationTests()
            )
        }
    }

    @BeforeTest
    fun setUp() {
        // Get session data from the server API (cached to avoid excessive calls)
        val sessionData = ServerApiHelper.getCachedSession()

        // Initialize Android context
        context = RuntimeEnvironment.getApplication()

        // Create SDK configuration
        val sdkConfiguration = SdkConfiguration(
            false,
            "AndroidSDK/IntegrationTests",
            Constants.SDK_IDENTIFIER,
            true
        )

        // Initialize SDK
        sdk = OnlinePaymentsSdk(sessionData, context, sdkConfiguration)

        // Create default payment context
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
