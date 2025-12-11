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
 * Data class which holds the Validator data.
 * Containing all the validation types.
 */
data class Validator(
    val expirationDate: ExpirationDate? = null,
    val emailAddress: EmailAddress? = null,
    val iban: IBAN? = null,
    val fixedList: FixedList? = null,
    val length: Length? = null,
    val luhn: Luhn? = null,
    val range: Range? = null,
    val regularExpression: RegularExpression? = null,
    val termsAndConditions: TermsAndConditions? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8524174888810141991L
    }
}
