/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.interfaces

import com.onlinepayments.sdk.client.android.domain.PaymentContext

internal interface ICacheManager {
    fun createCacheKeyFromContext(
        prefix: String,
        context: PaymentContext,
        suffix: String? = null
    ): String

    fun hasCache(key: String): Boolean

    fun <T> get(key: String): T?

    fun <T> set(key: String, value: T)

    fun clear()

    suspend fun <T> getOrFetch(
        key: String,
        fetch: suspend () -> T
    ): T
}
