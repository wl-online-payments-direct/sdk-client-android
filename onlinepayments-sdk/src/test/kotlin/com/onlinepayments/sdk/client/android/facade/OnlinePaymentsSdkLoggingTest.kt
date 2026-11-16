package com.onlinepayments.sdk.client.android.facade

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IServiceFactory
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiLogger
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class OnlinePaymentsSdkLoggingTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val sessionData = SessionData("session", "customer", "https://example.com", "https://assets.example.com")

    @BeforeTest
    fun setUp() = ApiLogger.setLoggingEnabled(false)

    @AfterTest
    fun tearDown() = ApiLogger.setLoggingEnabled(false)

    @Test
    fun `logging is enabled when the configuration enables it`() {
        OnlinePaymentsSdk(sessionData, context, SdkConfiguration(false, "App/1", null, loggingEnabled = true))

        assertTrue(ApiLogger.getLoggingEnabled())
    }

    @Test
    fun `logging stays off by default`() {
        OnlinePaymentsSdk(sessionData, context, SdkConfiguration(false, "App/1", null))

        assertFalse(ApiLogger.getLoggingEnabled())
    }

    @Test
    fun `logging stays off without a configuration`() {
        OnlinePaymentsSdk(sessionData, context, null)

        assertFalse(ApiLogger.getLoggingEnabled())
    }

    @Test
    fun `the instance created last decides`() {
        OnlinePaymentsSdk(sessionData, context, SdkConfiguration(false, "App/1", null, loggingEnabled = true))
        OnlinePaymentsSdk(sessionData, context, SdkConfiguration(false, "App/1", null, loggingEnabled = false))

        assertFalse(ApiLogger.getLoggingEnabled())
    }

    @Test
    fun `an instance with a custom service factory does not change the logging setting`() {
        ApiLogger.setLoggingEnabled(true)

        OnlinePaymentsSdk(
            sessionData,
            context,
            null,
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
            mockk<IServiceFactory>(relaxed = true),
        )

        assertTrue(ApiLogger.getLoggingEnabled())
    }
}
