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

import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IPaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IServiceFactory
import com.onlinepayments.sdk.client.android.infrastructure.utils.CacheManager
import com.onlinepayments.sdk.client.android.services.ClientService
import com.onlinepayments.sdk.client.android.services.EncryptionService
import com.onlinepayments.sdk.client.android.services.PaymentProductService
import com.onlinepayments.sdk.client.android.services.interfaces.IClientService
import com.onlinepayments.sdk.client.android.services.interfaces.IEncryptionService
import com.onlinepayments.sdk.client.android.services.interfaces.IPaymentProductService

/**
 * Factory for creating and managing service instances
 */
internal class ServiceFactory internal constructor(
    private val props: ServiceFactoryConfiguration
) : IServiceFactory {

    override val apiClient: IApiClient by lazy {
        props.apiClient ?: HttpServiceFactory.createApiService(
            props.configuration,
            props.sessionData,
            props.context,
            props.apiLogger,
        )
    }

    override val cacheManager: ICacheManager by lazy { CacheManager() }

    private val paymentProductFactory: IPaymentProductFactory by lazy {
        props.paymentProductFactory ?: PaymentProductFactory()
    }

    override val encryptionService: IEncryptionService by lazy {
        props.encryptionService ?: EncryptionService(
            apiClient = apiClient,
            sessionData = props.sessionData,
            context = props.context,
            configuration = props.configuration
        )
    }

    override val clientService: IClientService by lazy {
        props.clientService ?: ClientService(
            apiClient = apiClient,
            sessionData = props.sessionData,
            cacheManager = cacheManager
        )
    }

    override val paymentProductService: IPaymentProductService by lazy {
        props.paymentProductService ?: PaymentProductService(
            apiClient = apiClient,
            context = props.context,
            sessionData = props.sessionData,
            configuration = props.configuration,
            cacheManager = cacheManager,
            paymentProductFactory = paymentProductFactory
        )
    }
}
