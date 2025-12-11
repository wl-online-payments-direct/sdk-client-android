/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate

import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.extensions.appendIf

/**
 * Contains all configuration parameters needed for communicating with the Online Payments gateway.
 */
internal data class C2sCommunicatorConfiguration(
    val clientSessionId: String,
    val customerId: String,
    val rawClientApiUrl: String,
    val assetUrl: String,
    val environmentIsProduction: Boolean,
    val appIdentifier: String,
    val rawSdkIdentifier: String
) {
    private val clientApiUrl = normalizeClientUrl(rawClientApiUrl)
    val sdkIdentifier = normalizeSdkIdentifier(rawSdkIdentifier)

    companion object {
        private const val API_BASE = "client/"
        private const val SDK_IDENTIFIER_PARTS = 2
        private const val SDK_IDENTIFIER_VERSION_PARTS = 3
    }

    internal fun getClientApiUrl(apiVersion: ApiVersion, apiPath: String? = ""): String {
        return clientApiUrl + apiVersion.version + apiPath
    }

    private fun normalizeClientUrl(clientApiUrl: String): String {
        return StringBuilder(clientApiUrl)
            .appendIf({ !it.endsWith("/") }, "/")
            .appendIf({ !it.endsWith(API_BASE, true) }, API_BASE)
            .toString()
    }

    private fun normalizeSdkIdentifier(identifier: String): String {
        val identifierParts = identifier.split("/")

        if (identifierParts.size == SDK_IDENTIFIER_PARTS
            && identifierParts.first() == "FlutterClientSDK"
            && identifierParts.last().startsWith("v")
        ) {
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
