/*
 * Copyright 2023 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.model

/**
 * Contains a card number and optional payment product id, used to determine surcharge product type.
 *
 * @param cardNumber the partial credit card number for which the Surcharge product type should be determined
 * @param paymentProductId the id of the product for which the Surcharge product type should be determined, can be null
 */
internal data class Card(val cardNumber: String, val paymentProductId: Int?)
