/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import android.content.Context
import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.infrastructure.encryption.MetadataUtil
import com.onlinepayments.sdk.client.android.infrastructure.http.ApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IGoPayApi
import com.onlinepayments.sdk.client.android.infrastructure.models.ApiVersion
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiUrlBuilder
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
    /**
     * Creates an API client with proper error handling at the infrastructure boundary.
     * Returns IApiClient which wraps the Retrofit interface and handles HTTP exceptions.
     */
    fun createApiService(
        configuration: SdkConfiguration?,
        sessionData: SessionData,
        context: Context,
        apiLogger: IApiLogger? = ApiLogger,
    ): IApiClient {
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
                "GCS v1Client:${sessionData.clientSessionId}"
            )

            val metadata =
                MetadataUtil.getMetadata(
                    context,
                    configuration?.appIdentifier,
                    configuration?.sdkIdentifier ?: Constants.SDK_IDENTIFIER
                )
            if (metadata.isNotEmpty()) {
                val encoded = MetadataUtil.getBase64EncodedMetadata(metadata)
                builder.header("X-GCS-ClientMetaInfo", encoded)
            }

            chain.proceed(builder.build())
        }

        val okHttpClientBuilder = OkHttpClient.Builder()
            .sslSocketFactory(tlsSocketFactory, getSystemTrustManager())
            .addInterceptor(sessionInterceptor)

        // Only add logging interceptor if provided
        apiLogger?.let {
            okHttpClientBuilder.addInterceptor(it.getInterceptor())
        }

        val okHttpClient = okHttpClientBuilder.build()

        // Create Retrofit interface (infrastructure detail)
        val retrofitApi = Retrofit.Builder()
            .baseUrl(ApiUrlBuilder.getClientApiUrl(sessionData, ApiVersion.V1))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(okHttpClient)
            .build()
            .create(IGoPayApi::class.java)

        return ApiClient(retrofitApi)
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
