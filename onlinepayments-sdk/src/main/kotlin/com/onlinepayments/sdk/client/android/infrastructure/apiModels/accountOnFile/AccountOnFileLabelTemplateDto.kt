/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile

import com.google.gson.annotations.SerializedName

internal data class AccountOnFileLabelTemplateDto(
    @SerializedName("attributeKey")
    val attributeKey: String?,

    @SerializedName("mask")
    val mask: String?
)
