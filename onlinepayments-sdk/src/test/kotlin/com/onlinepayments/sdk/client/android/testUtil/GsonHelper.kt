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

package com.onlinepayments.sdk.client.android.testUtil

import com.google.gson.Gson
import java.io.IOException
import java.io.InputStreamReader

object GsonHelper {
    private val gson = Gson()

    fun <T> fromResourceJson(resource: String?, classOfT: Class<T>): T {
        try {
            InputStreamReader(GsonHelper::class.java.getClassLoader()?.getResourceAsStream(resource)).use { reader ->
                return gson.fromJson<T>(reader, classOfT)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
