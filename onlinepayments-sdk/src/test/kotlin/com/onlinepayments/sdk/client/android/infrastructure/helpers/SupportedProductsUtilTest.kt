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

package com.onlinepayments.sdk.client.android.infrastructure.helpers

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.infrastructure.utils.SupportedProductsUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportedProductsUtilTest {

    @Test
    fun `isSupportedInSdk returns true when product id is not in unavailable list`() {
        val result = SupportedProductsUtil.isSupportedInSdk(1)

        assertTrue(result)
    }

    @Test
    fun `isSupportedInSdk returns false when product id is in unavailable list`() {
        val result = SupportedProductsUtil.isSupportedInSdk(Constants.PAYMENT_PRODUCT_ID_APPLEPAY)

        assertFalse(result)
    }

    @Test
    fun `isSupportedInSdk returns true when product id is null`() {
        val result = SupportedProductsUtil.isSupportedInSdk(null)

        assertTrue(result)
    }

    @Test
    fun `get404Error returns ApiError with expected error id and error code`() {
        val error = SupportedProductsUtil.get404Error()

        assertEquals("48b78d2d-1b35-4f8b-92cb-57cc2638e901", error.errorId)
        val errors = error.errors!!
        assertEquals(1, errors.size)

        val errorItem = errors.first()
        assertEquals("1007", errorItem.errorCode)
        assertEquals("productId", errorItem.propertyName)
        assertEquals("UNKNOWN_PRODUCT_ID", errorItem.message)
        assertEquals(404, errorItem.httpStatusCode)
    }
}
