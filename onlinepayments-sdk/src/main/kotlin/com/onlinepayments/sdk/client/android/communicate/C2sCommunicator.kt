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
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.exception.CommunicationException
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.CardSource
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.model.currencyconversion.Transaction
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.surcharge.request.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.util.GooglePayUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.MalformedURLException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

/**
 * Handles communication with the Online Payments Client API using Coroutines.
 */
internal class C2sCommunicator(
    val configuration: C2sCommunicatorConfiguration,
    loggingEnabled: Boolean,
    val context: Context
) {
    companion object {
        private const val MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 8
        private const val MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 6
        private const val EXPIRY_DATE_MASK = "{{99}}/{{99}}"
        private const val BASIC_CARD_NUMBER_MASK = "{{9999}} {{9999}} {{9999}} {{9999}}"
        private const val AMEX_CARD_NUMBER_MASK = "{{9999}} {{999999}} {{99999}}"
        private const val EXPIRY_DATE = "expiryDate"
        private const val CARD_NUMBER = "cardNumber"
    }

    init {
        ApiLogger.setLoggingEnabled(loggingEnabled)
    }

    private val apiService: ApiService by lazy {
        HttpServiceFactory.createApiService(configuration, context)
    }

    val loggingEnabled: Boolean
        get() = ApiLogger.getLoggingEnabled()

    val isEnvironmentTypeProduction: Boolean = configuration.environmentIsProduction

    fun setLoggingEnabled(loggingEnabled: Boolean) {
        ApiLogger.setLoggingEnabled(loggingEnabled)
    }

    suspend fun getBasicPaymentItems(
        paymentContext: PaymentContext
    ): BasicPaymentItems = withContext(Dispatchers.IO) {
        val basicProducts = getBasicPaymentProducts(paymentContext)
        BasicPaymentItems(
            basicProducts.getPaymentProductsAsItems(),
            basicProducts.getAccountsOnFile()
        )
    }

    suspend fun getBasicPaymentProducts(
        paymentContext: PaymentContext
    ): BasicPaymentProducts = withContext(Dispatchers.IO) {
        callApi {
            val response = this@C2sCommunicator.apiService.getBasicPaymentProducts(
                configuration.customerId,
                paymentContext.toMap(),
            )

            filterApplePayGooglePay(response)

            response.getBasicPaymentProducts().removeAll {
                it.getId() in Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS
            }

            response
        }
    }

    suspend fun getPaymentProduct(
        productId: String,
        paymentContext: PaymentContext
    ): PaymentProduct? = withContext(Dispatchers.IO) {
        if (productId == Constants.PAYMENT_PRODUCT_ID_APPLEPAY) {
            throw ApiException("Apple Pay is not supported on Android devices.")
        }

        if (productId in Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS) {
            throw ApiException("Product with id $productId not found.")
        }

        callApi {
            try {
                val response = apiService.getPaymentProduct(
                    configuration.customerId,
                    productId,
                    paymentContext.toMap(),
                )

                if (productId == Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY &&
                    !GooglePayUtil.isGooglePayAllowed(
                        context,
                        this@C2sCommunicator.isEnvironmentTypeProduction,
                        response
                    )
                ) {
                    throw ApiException("GooglePay is not supported for the current payment")
                }

                fixProductFieldsIfRequired(response).apply {
                    getPaymentProductFields().forEach { it.setValidationRules() }
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    throw ApiException("Product with id $productId not found.")
                }

                throw e
            }
        }
    }

    suspend fun getPaymentProductNetworks(
        productId: String,
        paymentContext: PaymentContext
    ): PaymentProductNetworkResponse = withContext(Dispatchers.IO) {
        callApi {
            apiService.getPaymentProductNetworks(
                configuration.customerId,
                productId,
                paymentContext.toMap(),
            )
        }
    }

    suspend fun getIinDetails(
        partialCreditCardNumber: String,
        paymentContext: PaymentContext?
    ): IinDetailsResponse = withContext(Dispatchers.IO) {
        val partialCCNumber = partialCreditCardNumber.take(MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP)
        if (partialCCNumber.length < MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
            return@withContext IinDetailsResponse(IinStatus.NOT_ENOUGH_DIGITS)
        }

        callApi {
            try {
                val request = IinDetailsRequest(partialCCNumber, paymentContext)
                val response = apiService.getIinDetails(configuration.customerId, request)
                if (response.paymentProductId == null) {
                    response.status = IinStatus.UNKNOWN
                } else if (!response.isAllowedInContext) {
                    response.status = IinStatus.EXISTING_BUT_NOT_ALLOWED
                } else {
                    response.status = IinStatus.SUPPORTED
                }

                response
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    return@callApi IinDetailsResponse(IinStatus.UNKNOWN)
                }

                throw e
            }
        }
    }

    suspend fun getPublicKey(): PublicKeyResponse = withContext(Dispatchers.IO) {
        callApi {
            apiService.getPublicKey(configuration.customerId)
        }
    }

    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): CurrencyConversionResponse = withContext(Dispatchers.IO) {
        callApi {
            apiService.getCurrencyConversionQuote(
                configuration.customerId,
                CurrencyConversionRequest(cardSource, Transaction(amountOfMoney))
            )
        }
    }

    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): SurchargeCalculationResponse = withContext(Dispatchers.IO) {
        callApi {
            apiService.getSurchargeCalculation(
                configuration.customerId,
                SurchargeCalculationRequest(amountOfMoney, cardSource)
            )
        }
    }

    private fun filterApplePayGooglePay(products: BasicPaymentProducts) {
        products.getBasicPaymentProducts()
            .removeAll { product ->
                val googlePayAllowed = product.getId() == Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY
                    && !GooglePayUtil.isGooglePayAllowed(context, isEnvironmentTypeProduction, product)

                product.getId() == Constants.PAYMENT_PRODUCT_ID_APPLEPAY || googlePayAllowed
            }
    }

    private fun fixProductFieldsIfRequired(product: PaymentProduct): PaymentProduct {
        product.getPaymentProductFields().forEach { field ->
            when (field.id) {
                EXPIRY_DATE -> {
                    field.displayHints.formElement?.type = FormElement.Type.TEXT
                    if (field.displayHints.mask.isNullOrEmpty()) {
                        field.displayHints.mask = EXPIRY_DATE_MASK
                    }
                }

                CARD_NUMBER -> {
                    if (field.displayHints.mask.isNullOrEmpty()) {
                        field.displayHints.mask =
                            if (product.getId() == Constants.PAYMENT_PRODUCT_ID_AMEX) AMEX_CARD_NUMBER_MASK else BASIC_CARD_NUMBER_MASK
                    }
                }
            }
        }

        return product
    }

    private suspend inline fun <T> callApi(crossinline apiCall: suspend () -> T): T {
        return try {
            apiCall()
        } catch (e: ApiException) {
            // Already a known API error - propagate it as is.
            throw e
        } catch (e: IOException) {
            // Retrofit will throw IOException when the request did not succeed.
            // We need to check whether our response interceptor threw underlying error.

            // Check the direct cause first.
            if (e.cause as? ApiException != null) {
                throw e.cause as ApiException
            }

            // Check the suppressed exceptions for an ApiErrorException.
            e.suppressed.firstOrNull { it is ApiException }?.let { suppressed ->
                throw suppressed as ApiException
            }

            // Check the suppressed exceptions of the cause for an ApiErrorException.
            e.cause?.suppressed?.firstOrNull { it is ApiException }?.let { suppressed ->
                throw suppressed as ApiException
            }

            if (e.cause as? CommunicationException != null) {
                throw e.cause as CommunicationException
            }

            // Check the suppressed exceptions for an ApiErrorException.
            e.suppressed.firstOrNull { it is CommunicationException }?.let { suppressed ->
                throw suppressed as CommunicationException
            }

            // Check the suppressed exceptions of the cause for an ApiErrorException.
            e.cause?.suppressed?.firstOrNull { it is CommunicationException }?.let { suppressed ->
                throw suppressed as CommunicationException
            }

            // If neither is found, wrap the original exception into the CommunicationException.
            throw CommunicationException("Error while performing a request", e)
        } catch (ex: Exception) {
            // For any other unexpected exception, wrap it as well.
            val errorResponse = getErrorResponse(ex)

            throw CommunicationException(errorResponse.message, ex, errorResponse)
        }
    }

    private fun getErrorResponse(exception: Exception): ErrorResponse {
        val errorResponseMessage = when (exception) {
            is MalformedURLException -> "Unable to parse the request URL"
            is IOException -> "IOException while opening connection: ${exception.message}"
            is KeyManagementException -> "KeyManagementException while opening connection: ${exception.message}"
            is NoSuchAlgorithmException -> "NoSuchAlgorithmException while opening connection: ${exception.message}"
            else -> "Unknown exception occurred while opening connection: ${exception.message}"
        }

        return ErrorResponse(errorResponseMessage, exception)
    }
}
