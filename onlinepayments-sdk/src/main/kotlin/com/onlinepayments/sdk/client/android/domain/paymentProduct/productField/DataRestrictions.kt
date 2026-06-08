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

package com.onlinepayments.sdk.client.android.domain.paymentProduct.productField

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRule
import java.io.Serializable

class DataRestrictions internal constructor(
    private val required: Boolean,
    val validationRules: List<ValidationRule>
) : Serializable {
    fun isRequired(): Boolean = required

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -549503465906936684L
    }
}
