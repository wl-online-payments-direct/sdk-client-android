/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.card

/**
 * Contains all data needed to create a Surcharge Calculation Request.
 */
class CardSource internal constructor(
    val card: Card?,
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
