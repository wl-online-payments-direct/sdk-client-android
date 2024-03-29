/*
 * Copyright 2024 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.model.currencyconversion

import com.onlinepayments.sdk.client.android.model.AmountOfMoney

/**
 * POJO that contains transaction information.
 */
internal data class Transaction(
    val amount: AmountOfMoney
)
