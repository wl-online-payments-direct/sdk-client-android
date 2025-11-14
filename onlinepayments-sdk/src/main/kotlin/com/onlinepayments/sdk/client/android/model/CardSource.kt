/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.model

/**
 * Contains all data needed to create a Surcharge Calculation Request.
 */
internal class CardSource private constructor(
    val card: Card?,
    @Suppress("Unused")
    val token: String?
) {
    /**
     * @param card the card for which the Surcharge should be calculated
     */
    constructor(card: Card) : this(card, null)

    /**
     * @param token the token for which the Surcharge should be calculated
     */
    constructor(token: String) : this(null, token)
}
