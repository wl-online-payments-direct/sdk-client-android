/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IPaymentProductFactory
import com.onlinepayments.sdk.client.android.services.interfaces.IClientService
import com.onlinepayments.sdk.client.android.services.interfaces.IEncryptionService
import com.onlinepayments.sdk.client.android.services.interfaces.IPaymentProductService

internal data class ServiceFactoryConfiguration(
    val sessionData: SessionData,
    val configuration: SdkConfiguration?,
    val context: Context,
    val apiLogger: IApiLogger? = null,
    val apiClient: IApiClient? = null,
    val encryptionService: IEncryptionService? = null,
    val paymentProductService: IPaymentProductService? = null,
    val paymentProductFactory: IPaymentProductFactory? = null,
    val clientService: IClientService? = null
)
