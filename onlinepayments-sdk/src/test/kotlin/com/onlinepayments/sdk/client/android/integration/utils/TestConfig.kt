/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration.utils

import java.io.FileInputStream
import java.util.Properties

/**
 * Test configuration for integration tests.
 * Reads credentials from local.properties file.
 */
object TestConfig {
    private val properties: Properties by lazy {
        Properties().apply {
            try {
                // Find local.properties by searching up from current directory
                var currentDir = java.io.File(System.getProperty("user.dir")!!)
                var localPropertiesFile: java.io.File? = null

                // Search up to 5 levels up to find local.properties
                @Suppress("unused")
                for (i in 0..5) {
                    val candidate = java.io.File(currentDir, "local.properties")
                    if (candidate.exists()) {
                        localPropertiesFile = candidate
                        break
                    }

                    currentDir = currentDir.parentFile ?: break
                }

                if (localPropertiesFile != null && localPropertiesFile.exists()) {
                    FileInputStream(localPropertiesFile).use { load(it) }
                } else {
                    println("Warning: local.properties not found in project hierarchy")
                }
            } catch (e: Exception) {
                println("Warning: Could not load local.properties: ${e.message}")
            }
        }
    }

    /**
     * Get a required environment variable or property.
     * Throws IllegalStateException if not found.
     */
    private fun getRequiredProperty(key: String): String {
        return properties.getProperty(key)
            ?: System.getenv(key)
            ?: throw IllegalStateException(
                "Missing required configuration: $key. " +
                    "Please set it in local.properties or as an environment variable."
            )
    }

    /**
     * Get an optional property with a default value.
     */
    private fun getOptionalProperty(key: String, default: String): String {
        return properties.getProperty(key) ?: System.getenv(key) ?: default
    }

    val merchantId: String
        get() = getRequiredProperty("ONLINEPAYMENTS_SDK_MERCHANT_ID")

    val apiKeyId: String
        get() = getRequiredProperty("ONLINEPAYMENTS_SDK_API_ID")

    val apiSecret: String
        get() = getRequiredProperty("ONLINEPAYMENTS_SDK_API_SECRET")

    val host: String
        get() = getOptionalProperty("ONLINEPAYMENTS_SDK_HOST", "payment.preprod.direct.worldline-solutions.com")

    val cardNumberWithSurcharge: String
        get() = getOptionalProperty("CARD_NUMBER_WITH_SURCHARGE", "4567350000427977")

    val cardNumberWithoutSurcharge: String
        get() = getOptionalProperty("CARD_NUMBER_WITHOUT_SURCHARGE", "4242424242424242")

    val cardNumberWithCoBrands: String
        get() = getOptionalProperty("CARD_NUMBER_WITH_CO_BRANDS", "5341013985664960")

    val cardNumberVisa: String
        get() = "4000000000000002"

    val productIdVisa: Int
        get() = 1

    /**
     * Check if integration tests can run (all required credentials are available).
     */
    fun canRunIntegrationTests(): Boolean {
        return try {
            merchantId
            apiKeyId
            apiSecret
            true
        } catch (_: IllegalStateException) {
            false
        }
    }
}
