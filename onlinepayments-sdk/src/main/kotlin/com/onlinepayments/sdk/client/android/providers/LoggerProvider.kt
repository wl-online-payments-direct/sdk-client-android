/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.providers

import com.onlinepayments.sdk.client.android.util.Logger

object LoggerProvider {
    var logger: Logger = Logger()

    fun reset() {
        logger = Logger()
    }
}