/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.encryption

/**
 * POJO which contains the all the possible EncryptData fields.
 */
internal class EncryptData(
    val accountOnFileId: String?,
    val clientSessionId: String,
    val nonce: String,
    val paymentProductId: Int?,
    val tokenize: Boolean,
    val paymentValues: Map<String, String>
)
