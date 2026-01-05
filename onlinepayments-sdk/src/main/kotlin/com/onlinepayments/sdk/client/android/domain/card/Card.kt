/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.card

/**
 * Contains a card number and optional payment product id, used to determine surcharge product type.
 *
 * @param cardNumber the partial credit card number for which the Surcharge product type should be determined
 * @param paymentProductId the id of the product for which the Surcharge product type should be determined, can be null
 */
@ConsistentCopyVisibility
data class Card internal constructor(val cardNumber: String, val paymentProductId: Int?)
