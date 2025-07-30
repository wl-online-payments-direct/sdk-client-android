/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate

import com.onlinepayments.sdk.client.android.communicate.interceptors.LoggingInterceptor
import okhttp3.Interceptor
import java.util.concurrent.atomic.AtomicBoolean

object ApiLogger {
    private val interceptor = LoggingInterceptor(AtomicBoolean(false))

    fun getInterceptor(): Interceptor {
        return interceptor
    }

    fun setLoggingEnabled(enabled: Boolean) {
        interceptor.loggingEnabled.set(enabled)
    }

    fun getLoggingEnabled(): Boolean {
        return interceptor.loggingEnabled.get()
    }
}
