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

import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Cache manager with TTL (Time-To-Live) and size limits to prevent unbounded memory growth.
 *
 * @param maxSize Maximum number of items to store in cache. When exceeded, oldest entries are evicted.
 * @param ttlMillis Time-to-live for cached entries in milliseconds. Expired entries are automatically removed.
 */
class CacheManager(
    private val maxSize: Int = 100,
    private val ttlMillis: Long = TimeUnit.MINUTES.toMillis(30)
) : ICacheManager {

    private data class CacheEntry(
        val value: Any,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean {
            return System.currentTimeMillis() - timestamp > ttl
        }
    }

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun createCacheKeyFromContext(
        prefix: String,
        context: PaymentContext,
        suffix: String?
    ): String = buildString {
        append(prefix)
        append('-')

        listOfNotNull(
            context.amountOfMoney.amount,
            context.countryCode,
            context.isRecurring,
            context.amountOfMoney.currencyCode,
            suffix
        ).joinTo(this, separator = "_")
    }

    override fun hasCache(key: String): Boolean {
        val entry = cache[key] ?: return false

        if (entry.isExpired(ttlMillis)) {
            cache.remove(key)
            return false
        }

        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        val entry: CacheEntry = cache[key] ?: return null

        if (entry.isExpired(ttlMillis)) {
            cache.remove(key)
            return null
        }

        return entry.value as? T
    }

    override fun <T> set(key: String, value: T) {
        // Simple LRU: if cache is full, remove oldest entry
        if (cache.size >= maxSize) {
            val oldestKey = cache.entries
                .minByOrNull { it.value.timestamp }
                ?.key

            oldestKey?.let { cache.remove(it) }
        }

        cache[key] = CacheEntry(value as Any)
    }

    override fun clear() {
        cache.clear()
    }

    override suspend fun <T> getOrFetch(
        key: String,
        fetch: suspend () -> T
    ): T {
        return get(key) ?: fetch().also { set(key, it) }
    }
}
