/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.communicate

import com.onlinepayments.sdk.client.android.extensions.appendIf
import java.util.Locale

/**
 * Contains all configuration parameters needed for communicating with the Online Payments gateway.
 */
@Suppress("ConstructorParameterNaming")
internal data class C2sCommunicatorConfiguration(
    val clientSessionId: String,
    val customerId: String,
    private val _clientApiUrl: String,
    val assetUrl: String,
    val environmentIsProduction: Boolean,
    val appIdentifier: String
) {
    private val clientApiUrl: String = createClientUrl(_clientApiUrl)

    companion object {
        private const val API_BASE = "client/"
    }

    internal fun getClientApiUrl(apiVersion: ApiVersion, apiPath: String): String {
        return clientApiUrl + apiVersion.version + apiPath
    }

    private fun createClientUrl(clientApiUrl: String): String {
        return StringBuilder(clientApiUrl)
            .appendIf({ !it.endsWith("/") }, "/")
            .appendIf({ !it.endsWith(API_BASE, true) }, API_BASE)
            .toString()
    }
}
