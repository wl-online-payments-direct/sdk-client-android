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

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.google.android.gms.wallet.WalletConstants
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Util class containing Google Pay related methods.
 */
internal object GooglePayUtil {
    private val TAG: String = GooglePayUtil::class.java.getName()
    private val logger = LoggerProvider.logger

    /**
     * Check whether or not Google Pay is allowed by creating an IsReadyToPayRequest containing minimal information and sending it to Google through the Google Pay PaymentsClient.
     *
     * @param context used to interact with the Google Pay API to check if it is allowed
     * @param isProduction indicates if the current environment is production
     * @param googlePay the Google Pay payment product object containing the networks that are allowed for the current payment
     *
     * @return a Boolean indicating whether Google Pay is allowed
     */
    fun isGooglePayAllowed(
        context: Context,
        isProduction: Boolean,
        googlePay: BasicPaymentProduct
    ): Boolean {
        // This should never occur as it is controlled by the sdk
        if (Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY != googlePay.getId()) {
            throw InvalidParameterException("This method cannot be called with a product other than Google Pay")
        }

        // Retrieve the networks that, in the current context, can be used for Google Pay
        val networks: MutableList<String?> = getNetworks(googlePay)

        // Set up a client
        val environment =
            if (isProduction) WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST

        val client = Wallet.getPaymentsClient(
            context,
            WalletOptions.Builder().setEnvironment(environment).build()
        )

        // Create a simple request containing just enough info to check if Google Pay is available
        val request = IsReadyToPayRequest.fromJson(createGooglePayRequest(networks).toString())
        val task = client.isReadyToPay(request)

        // Wait for a response
        try {
            Tasks.await(
                task,
                Constants.ACCEPTABLE_WAIT_TIME_IN_MILLISECONDS.toLong(),
                TimeUnit.MILLISECONDS
            )

            // Handle result
            if (task.isSuccessful) {
                return task.getResult()!!
            }

            return false
        } catch (_: TimeoutException) {
            logger.e(TAG, "Timeout while making isReadyToPay call: ${task.exception}")

            return false
        } catch (_: Exception) {
            logger.e(TAG, "Exception occurred while making isReadyToPay call: ${task.exception}")

            return false
        }
    }

    /**
     * Assemble the minimal Google Pay payment request that can be used to verify whether the current user is Ready to Pay with Google Pay.
     *
     * @param networks needed for reading metadata
     */
    private fun createGooglePayRequest(networks: MutableList<String?>): JSONObject {
        val paymentRequest = JSONObject()
        // Assemble payment request
        try {
            // Insert API version
            paymentRequest.put("apiVersion", Constants.GOOGLE_API_VERSION)
            paymentRequest.put("apiVersionMinor", 0)

            val allowedPaymentMethods = JSONArray()
            val allowedPaymentMethodsContent: JSONObject =
                getAllowedPaymentMethodsJson(networks)

            allowedPaymentMethods.put(allowedPaymentMethodsContent)
            paymentRequest.put("allowedPaymentMethods", allowedPaymentMethods)
        } catch (e: JSONException) {
            logger.e(TAG, "Exception occurred while creating JSON object: $e")
        }

        return paymentRequest
    }

    private fun getAllowedPaymentMethodsJson(networks: MutableList<String?>): JSONObject {
        val cardPaymentMethod = JSONArray()
        val allowedNetworks = JSONArray()
        val allowedAuthMethods = JSONArray()
            .put("PAN_ONLY")
            .put("CRYPTOGRAM_3DS")

        // Convert networks and authMethods to JSON objects
        for (s in networks) {
            allowedNetworks.put(s)
        }

        val parameters = JSONObject()
        try {
            parameters.put("type", "CARD")
            parameters.put(
                "parameters",
                JSONObject()
                    .put("allowedAuthMethods", allowedAuthMethods)
                    .put("allowedCardNetworks", allowedNetworks)
            )
            cardPaymentMethod.put(parameters)
        } catch (e: JSONException) {
            logger.e(TAG, "Exception occurred while creating JSON object: $e")
        }

        return parameters
    }

    private fun getNetworks(paymentProduct: BasicPaymentProduct): MutableList<String?> {
        val paymentProductSpecificData = paymentProduct.paymentProduct320SpecificData

        val networks = paymentProductSpecificData?.networks

        if (networks != null && !networks.isEmpty()) {
            return networks
        }

        error("No networks found")
    }
}
