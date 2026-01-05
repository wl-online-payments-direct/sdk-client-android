/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade.helpers

import com.onlinepayments.sdk.client.android.domain.exceptions.ConfigurationException
import com.onlinepayments.sdk.client.android.infrastructure.extensions.appendIf
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData

internal object SessionDataNormalizer {

    private const val API_BASE = "client/"

    fun normalize(sessionData: SessionData): SessionData {
        var data: SessionData?

        try {
            data = sessionData.copy(
                clientApiUrl = sanitizeClientApiUrl(sessionData.clientApiUrl)
            )
        } catch (_: Exception) {
            throw ConfigurationException(
                "A valid URL is required for the 'clientApiUrl', you provided '${sessionData.clientApiUrl}'",
            )
        }

        return data
    }

    private fun sanitizeClientApiUrl(clientApiUrl: String): String {
        return StringBuilder(clientApiUrl)
            .appendIf({ !it.endsWith("/") }, "/")
            .appendIf({ !it.endsWith(API_BASE, true) }, API_BASE)
            .toString()
    }
}
