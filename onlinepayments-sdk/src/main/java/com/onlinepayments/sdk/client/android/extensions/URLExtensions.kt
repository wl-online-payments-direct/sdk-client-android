/*
 * Copyright 2024 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.extensions

import android.os.Build
import com.onlinepayments.sdk.client.android.communicate.TLSSocketFactory
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

@JvmSynthetic
internal fun URL.isHttps() : Boolean {
    return "https".equals(protocol, ignoreCase = true)
}

@JvmSynthetic
internal fun URL.openHttpsConnection() : HttpsURLConnection {
    val connection = openConnection() as HttpsURLConnection
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, null, null)
    val sslv3Factory = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        TLSSocketFactory(sslContext.socketFactory)
    } else {
        sslContext.socketFactory
    }
    connection.sslSocketFactory = sslv3Factory
    return connection
}
