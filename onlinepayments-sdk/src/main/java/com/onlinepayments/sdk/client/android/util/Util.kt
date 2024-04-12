/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.encryption.EncryptUtil
import java.nio.charset.StandardCharsets

internal object Util {
    private val encryptUtil = EncryptUtil()

    // Metadata map keys
    private const val METADATA_PLATFORM_IDENTIFIER = "platformIdentifier"
    private const val METADATA_APP_IDENTIFIER = "appIdentifier"
    private const val METADATA_SDK_IDENTIFIER = "sdkIdentifier"
    private const val METADATA_SDK_CREATOR = "sdkCreator"

    private const val METADATA_SCREENSIZE = "screenSize"
    private const val METADATA_DEVICE_BRAND = "deviceBrand"
    private const val METADATA_DEVICE_TYPE = "deviceType"

    /**
     * Returns map of metadata of the device this SDK is running on.
     * The map contains the SDK version, OS, OS version and screen size.
     *
     * @param context used for retrieving device metadata
     * @param appIdentifier a String that describes the application, preferably with version number
     *
     * @return a Map containing key/values of metadata
     */
    @JvmSynthetic
    fun getMetadata(context: Context, appIdentifier: String?, sdkIdentifier: String): Map<String, String> {
        val metaData = mutableMapOf<String, String>()

        // Add OS + build version
        metaData[METADATA_PLATFORM_IDENTIFIER] = "Android/" + Build.VERSION.RELEASE

        // Add app identifier (optional)
        if (!appIdentifier.isNullOrEmpty()) {
            metaData[METADATA_APP_IDENTIFIER] = appIdentifier
        } else {
            metaData[METADATA_APP_IDENTIFIER] = "unknown"
        }

        // Add SDK version
        metaData[METADATA_SDK_IDENTIFIER] = sdkIdentifier
        metaData[METADATA_SDK_CREATOR] = Constants.SDK_CREATOR

        // Add screen size
        val metrics = getDefaultDisplayMetrics(context)
        metaData[METADATA_SCREENSIZE] = metrics.heightPixels.toString() + "x" + metrics.widthPixels.toString()

        // Add device brand
        metaData[METADATA_DEVICE_BRAND] = Build.MANUFACTURER

        // Add device type
        metaData[METADATA_DEVICE_TYPE] = Build.MODEL

        return metaData
    }

    private fun getDefaultDisplayMetrics(context: Context) : DisplayMetrics {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        // Uses deprecated methods for older Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            metrics.widthPixels = bounds.width()
            metrics.heightPixels = bounds.height()
        } else {
            wm.defaultDisplay.getMetrics(metrics)
        }
        return metrics
    }

    /**
     * Returns base64 encoded version of a map of metadata of the device this SDK is running on.
     * The map contains the SDK version, OS, OS version and screen size.
     *
     * @param context used for retrieving device metadata
     * @param appIdentifier a String that describes the application, preferably with version number
     *
     * @return String containing base64 url of json representation of the metadata
     */
    @JvmSynthetic
    fun getBase64EncodedMetadata(context: Context, appIdentifier: String?, sdkIdentifier: String): String {
        val jsonMetadata = Gson().toJson(getMetadata(context, appIdentifier, sdkIdentifier))

        return encode(jsonMetadata)
    }

    /**
     * Returns base64 encoded map of metadata.
     *
     * @param metadata map of metadata which is base64 encoded
     *
     * @return String containing base64 url of json representation of the metadata
     */
    @JvmSynthetic
    fun getBase64EncodedMetadata(metadata: Map<String, String>): String {
        val jsonMetadata = Gson().toJson(metadata)

        return encode(jsonMetadata)
    }

    private fun encode(jsonMetadata: String): String {
        return encryptUtil.base64UrlEncode(jsonMetadata.toByteArray(StandardCharsets.UTF_8))
    }
}
