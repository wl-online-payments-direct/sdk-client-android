/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.listener

import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest

interface PaymentRequestPreparedListener {
    fun onPaymentRequestPrepared(preparedPaymentRequest: PreparedPaymentRequest?)
    fun onFailure(e: EncryptDataException?)
}
