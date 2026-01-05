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
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData

internal object SessionDataValidator {

    fun validateRequiredFields(sessionData: SessionData) {
        val requiredFields = mapOf(
            "customerId" to sessionData.customerId,
            "assetUrl" to sessionData.assetUrl,
            "clientSessionId" to sessionData.clientSessionId,
            "clientApiUrl" to sessionData.clientApiUrl
        )

        requiredFields.forEach { (key, value) ->
            if (value.isBlank()) {
                throw ConfigurationException("The SessionData parameter '$key' is mandatory.")
            }
        }
    }
}
