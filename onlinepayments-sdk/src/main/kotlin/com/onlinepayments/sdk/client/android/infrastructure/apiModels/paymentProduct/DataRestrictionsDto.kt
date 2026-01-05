/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct

import com.google.gson.annotations.SerializedName
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ValidatorsDto

/**
 * DTO for DataRestrictions from JSON
 */
internal data class DataRestrictionsDto(
    @SerializedName("isRequired")
    val isRequired: Boolean? = null,

    @SerializedName("validators")
    val validators: ValidatorsDto? = null
)
