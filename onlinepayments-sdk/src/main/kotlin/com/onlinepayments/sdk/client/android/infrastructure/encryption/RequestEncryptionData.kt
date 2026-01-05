/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.encryption

/**
 * Data class which contains the all the possible fields for request encryption.
 */
internal data class RequestEncryptionData(
    val accountOnFileId: String?,
    val clientSessionId: String,
    val nonce: String,
    val paymentProductId: Int?,
    val tokenize: Boolean,
    val paymentValues: Map<String, String>
)

