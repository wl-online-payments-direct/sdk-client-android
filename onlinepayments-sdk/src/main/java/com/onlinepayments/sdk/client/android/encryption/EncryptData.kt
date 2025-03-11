/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.encryption

/**
 * POJO which contains the all the possible EncryptData fields.
 */
internal class EncryptData(
    @JvmSynthetic
    val accountOnFileId: String?,
    @JvmSynthetic
    val clientSessionId: String,
    @JvmSynthetic
    val nonce: String,
    @JvmSynthetic
    val paymentProductId: Int?,
    @JvmSynthetic
    val tokenize: Boolean,
    @JvmSynthetic
    val paymentValues: Map<String, String>
)
