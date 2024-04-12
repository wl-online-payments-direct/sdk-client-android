/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.communicate

import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.extensions.appendIf

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
    val appIdentifier: String,
    private val _sdkIdentifier: String
) {
    private val clientApiUrl: String = createClientUrl(_clientApiUrl)

    val sdkIdentifier = getValidSdkIdentifier(_sdkIdentifier)

    companion object {
        private const val API_BASE = "client/"
        private const val SDK_IDENTIFIER_PARTS = 2
        private const val SDK_IDENTIFIER_VERSION_PARTS = 3
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

    private fun getValidSdkIdentifier(identifier: String): String {
        val identifierParts = identifier.split("/")

        if (identifierParts.size == SDK_IDENTIFIER_PARTS
            && identifierParts.first() == "FlutterClientSDK"
            && identifierParts.last().startsWith("v")) {
            val versionParts = identifierParts.last().replace("v", "").split(".")

            if (versionParts.size == SDK_IDENTIFIER_VERSION_PARTS
                && versionParts.all { it.toIntOrNull() != null }
            ) {
                return identifier
            }
        }

        return Constants.SDK_IDENTIFIER
    }
}
