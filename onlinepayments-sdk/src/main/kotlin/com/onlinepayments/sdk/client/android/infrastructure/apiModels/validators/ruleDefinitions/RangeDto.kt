/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions

/**
 * Data class which holds the Range data.
 * Used for validation.
 */
internal data class RangeDto(
    val minValue: Int? = null,
    val maxValue: Int? = null
)

