/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.util

import android.util.Log

class Logger {
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun d(tag: String, message: String, t: Throwable?) {
        Log.d(tag, message, t)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun e(tag: String, message: String, t: Throwable?) {
        Log.e(tag, message, t)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    @Suppress("unused")
    fun i(tag: String, message: String, t: Throwable?) {
        Log.i(tag, message, t)
    }
}
