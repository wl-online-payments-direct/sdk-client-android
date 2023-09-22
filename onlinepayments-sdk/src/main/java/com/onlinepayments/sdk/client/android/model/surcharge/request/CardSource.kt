package com.onlinepayments.sdk.client.android.model.surcharge.request

/**
 * Contains all data needed to create a Surcharge Calculation Request.
 */
internal class CardSource {
    var card: Card? = null
        private set
    var token: String? = null
        private set

    /**
     * @param card the card for which the Surcharge should be calculated
     */
    constructor(card: Card) {
        this.card = card
    }

    /**
     * @param token the token for which the Surcharge should be calculated
     */
    constructor(token: String) {
        this.token = token
    }
}
