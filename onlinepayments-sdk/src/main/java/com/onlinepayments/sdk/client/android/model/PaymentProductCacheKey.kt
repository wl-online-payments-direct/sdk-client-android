/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.model

import java.io.Serializable

/**
 * Holds the PaymentProductCacheKey data.
 * It is used to determine if a PaymentProduct should be retrieved from the Online Payments platform, or retrieved from the memory cache.
 */
internal class PaymentProductCacheKey(
    private val amount: Long,
    private val countryCode: String,
    private val currencyCode: String,
    private val isRecurring: Boolean,
    private val paymentProductId: String
): Serializable {
    companion object {
        private const val serialVersionUID = -45L
    }

    @JvmSynthetic
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other.javaClass != javaClass) { return false }

        val otherKey = other as PaymentProductCacheKey
        return otherKey.amount == amount &&
               otherKey.countryCode == countryCode &&
               otherKey.currencyCode == currencyCode &&
               otherKey.isRecurring == isRecurring &&
               otherKey.paymentProductId == paymentProductId
    }

    @JvmSynthetic
    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + amount.hashCode()
        hash = 31 * hash + countryCode.hashCode()
        hash = 31 * hash + currencyCode.hashCode()
        hash = 31 * hash + paymentProductId.hashCode()
        hash = 31 * hash + java.lang.Boolean.valueOf(isRecurring).hashCode()
        return hash
    }
}
