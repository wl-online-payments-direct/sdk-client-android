/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate

import android.content.Context
import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.communicate.interceptors.ResponseInterceptor
import com.onlinepayments.sdk.client.android.util.Util
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal object HttpServiceFactory {

    fun createApiService(
        configuration: C2sCommunicatorConfiguration,
        context: Context
    ): ApiService {
        // 1. Build TLS socket factory
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, null, SecureRandom())
        }

        val tlsSocketFactory = TLSSocketFactory(sslContext.socketFactory)

        // 2. Create OkHttp interceptors
        val sessionInterceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val builder = original.newBuilder()

            builder.header(
                "Authorization",
                "GCS v1Client:${configuration.clientSessionId}"
            )

            val metadata =
                Util.getMetadata(context, configuration.appIdentifier, configuration.sdkIdentifier)
            if (metadata.isNotEmpty()) {
                val encoded = Util.getBase64EncodedMetadata(metadata)
                builder.header("X-GCS-ClientMetaInfo", encoded)
            }

            chain.proceed(builder.build())
        }

        // 3. Build the OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(tlsSocketFactory, getSystemTrustManager())
            .addInterceptor(sessionInterceptor)
            .addInterceptor(ApiLogger.getInterceptor())
            .addInterceptor(ResponseInterceptor())
            .build()

        // 4. Build the Retrofit instance
        return Retrofit.Builder()
            .baseUrl(configuration.getClientApiUrl(ApiVersion.V1))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    private fun getSystemTrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        require(trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
            "Unexpected default trust managers: ${trustManagers.contentToString()}"
        }

        return trustManagers[0] as X509TrustManager
    }
}
