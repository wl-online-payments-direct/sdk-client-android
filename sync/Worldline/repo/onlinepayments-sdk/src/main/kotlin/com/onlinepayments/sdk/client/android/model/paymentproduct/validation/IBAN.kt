/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct.validation

import java.io.Serializable

/**
 * Data class which holds the IBAN data.
 * Used for validation.
 */
object IBAN : Serializable {
    @Suppress("ConstPropertyName")
    private const val serialVersionUID = -2599896096694861251L

    @Suppress("Unused")
    private fun readResolve(): Any = IBAN
}
