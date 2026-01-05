/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.utils

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.infrastructure.models.ApiVersion

object ApiUrlBuilder {
    fun getClientApiUrl(
        sessionData: SessionData,
        apiVersion: ApiVersion,
        apiPath: String = ""
    ): String {
        return sessionData.clientApiUrl + apiVersion.version + apiPath
    }
}
