/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.utils

import com.onlinepayments.sdk.client.android.infrastructure.interceptors.LoggingInterceptor
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiLogger
import okhttp3.Interceptor
import java.util.concurrent.atomic.AtomicBoolean

object ApiLogger : IApiLogger {
    private val interceptor = LoggingInterceptor(AtomicBoolean(false))

    override fun getInterceptor(): Interceptor {
        return interceptor
    }

    override fun setLoggingEnabled(enabled: Boolean) {
        interceptor.loggingEnabled.set(enabled)
    }

    override fun getLoggingEnabled(): Boolean {
        return interceptor.loggingEnabled.get()
    }
}
