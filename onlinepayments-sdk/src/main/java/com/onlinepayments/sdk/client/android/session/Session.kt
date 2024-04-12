/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.session

import android.content.Context
import android.util.Log
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicatorConfiguration
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener
import com.onlinepayments.sdk.client.android.listener.CurrencyConversionResponseListener
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentProductCacheKey
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.api.ApiErrorItem
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.Card
import com.onlinepayments.sdk.client.android.model.CardSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ObjectInputStream
import java.io.Serializable

/**
 * Use this object to perform Client API requests such as get Payment Products or get IIN Details.
 *
 * @param clientSessionId used for identifying the session on the Online Payments gateway
 * @param customerId used for identifying the customer on the Online Payments gateway
 * @param clientApiUrl the endpoint baseurl
 * @param assetBaseUrl the asset baseurl
 * @param environmentIsProduction states if the environment is production
 * @param appIdentifier used to create device metadata
 * @param loggingEnabled indicates whether requests and responses should be logged to the console; default is false; should be false in production
 *
 */
@Suppress("LongParameterList")
class Session(
    private val clientSessionId: String,
    private val customerId: String,
    private val clientApiUrl: String,
    private val assetBaseUrl: String,
    private val environmentIsProduction: Boolean,
    private val appIdentifier: String,
    private var loggingEnabled: Boolean = false,
    private val sdkIdentifier: String
): Serializable {
    companion object {
        private const val serialVersionUID = -4386453L
    }

    @Transient
    private var sessionScope = CoroutineScope(Dispatchers.IO)

    private val paymentProductMapping = HashMap<PaymentProductCacheKey, PaymentProduct>()

    @Transient
    private var communicator: C2sCommunicator
    private var iinLookupPending = false

    init {
        val configuration = C2sCommunicatorConfiguration(
            clientSessionId,
            customerId,
            clientApiUrl,
            assetBaseUrl,
            environmentIsProduction,
            appIdentifier,
            sdkIdentifier
        )
        this.communicator = C2sCommunicator(configuration, loggingEnabled)
    }

    constructor(
        clientSessionId: String,
        customerId: String,
        clientApiUrl: String,
        assetBaseUrl: String,
        environmentIsProduction: Boolean,
        appIdentifier: String,
        loggingEnabled: Boolean = false
    ): this(
        clientSessionId,
        customerId,
        clientApiUrl,
        assetBaseUrl,
        environmentIsProduction,
        appIdentifier,
        loggingEnabled,
        Constants.SDK_IDENTIFIER
    )

    fun getLoggingEnabled(): Boolean {
        return communicator.loggingEnabled
    }

    fun setLoggingEnabled(loggingEnabled: Boolean) {
        this.communicator.setLoggingEnabled(loggingEnabled)
    }

    @Suppress("UnusedParameter")
    fun getBasicPaymentItems(
        context: Context,
        paymentContext: PaymentContext,
        groupPaymentProducts: Boolean,
        listener: BasicPaymentItemsResponseListener
    ) {
        sessionScope.launch {
            communicator.getBasicPaymentItems(context, paymentContext, listener)
        }
    }

    fun getBasicPaymentProducts(
        context: Context,
        paymentContext: PaymentContext,
        listener: BasicPaymentProductsResponseListener
    ) {
        sessionScope.launch {
            communicator.getBasicPaymentProducts(context, paymentContext, listener)
        }
    }

    fun getPaymentProduct(
        context: Context,
        productId: String,
        paymentContext: PaymentContext,
        listener: PaymentProductResponseListener
    ) {
        // Create a cacheKey for this paymentProduct
        val cacheKey = createPaymentItemCacheKey(paymentContext, productId)

        // If the paymentProduct is already in the cache, call the listener with the paymentProduct from the cache
        if (paymentProductMapping.containsKey(cacheKey)) {
            val cachedPaymentProduct = paymentProductMapping[cacheKey]
            listener.onSuccess(cachedPaymentProduct)
            return
        }
        // This listener is used to store the paymentProduct in the cache
        val paymentProductCacheListener = object : PaymentProductResponseListener {
            override fun onSuccess(response: PaymentProduct) {
                // Store the loaded paymentProduct in the cache
                cachePaymentProduct(response, paymentContext)
            }

            override fun onApiError(error: ErrorResponse) {
                this@Session.onApiError("PaymentProduct", error)
            }

            override fun onException(t: Throwable) {
                this@Session.onApiException("PaymentProduct", t)
            }
        }

        val listeners = listOf(listener, paymentProductCacheListener)

        sessionScope.launch {
            communicator.getPaymentProduct(productId, context, paymentContext, listeners)
        }
    }

    fun getNetworksForPaymentProduct(
        productId: String,
        context: Context,
        paymentContext: PaymentContext,
        listener: PaymentProductNetworkResponseListener
    ) {
        sessionScope.launch {
            communicator.getPaymentProductNetworks(productId, context, paymentContext, listener)
        }
    }

    fun getIinDetails(
        context: Context,
        partialCreditCardNumber: String,
        listener: IinLookupResponseListener,
        paymentContext: PaymentContext
    ) {
        // Only execute the IIN details call when a call is not yet in progress
        if (!iinLookupPending) {
            val iinLookupPendingListener = object : IinLookupResponseListener {
                override fun onSuccess(response: IinDetailsResponse) {
                    iinLookupPending = false
                }

                override fun onApiError(error: ErrorResponse) {
                    iinLookupPending = false
                }

                override fun onException(t: Throwable) {
                    iinLookupPending = false
                }
            }

            val listeners = listOf(listener, iinLookupPendingListener)

            iinLookupPending = true

            sessionScope.launch {
                communicator.getIinDetails(
                    partialCreditCardNumber,
                    context,
                    paymentContext,
                    listeners
                )
            }
        }
    }

    fun getPublicKey(context: Context, listener: PublicKeyResponseListener) {
        sessionScope.launch {
            communicator.getPublicKey(context, listener)
        }
    }

    fun preparePaymentRequest(
        paymentRequest: PaymentRequest,
        context: Context,
        listener: PaymentRequestPreparedListener
    ) {
        val metaData = communicator.getMetadata(context)
        val sessionEncryptionHelper = SessionEncryptionHelper(
            context,
            paymentRequest,
            clientSessionId,
            metaData,
            listener
        )

        val publicKeyResponseListener = object : PublicKeyResponseListener {
            override fun onSuccess(response: PublicKeyResponse) {
                sessionEncryptionHelper.onPublicKeyReceived(response)
            }

            override fun onApiError(error: ErrorResponse) {
                this@Session.onApiError("PublicKey", error)
                listener.onFailure(EncryptDataException(error.message))
            }

            override fun onException(t: Throwable) {
                this@Session.onApiException("PublicKey", t)
                listener.onFailure(EncryptDataException("Exception while retrieving Public Key", t))
            }
        }

        sessionScope.launch {
            communicator.getPublicKey(context, publicKeyResponseListener)
        }
    }

    fun getCurrencyConversionQuote(
        context: Context,
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: Int?,
        listener: CurrencyConversionResponseListener
    ) {
        sessionScope.launch {
            val card = Card(partialCreditCardNumber, paymentProductId)
            val cardSource = CardSource(card)

            communicator.getCurrencyConversionQuote(amountOfMoney, cardSource, context, listener)
        }
    }

    fun getCurrencyConversionQuote(
        context: Context,
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: CurrencyConversionResponseListener
    ) {
        sessionScope.launch {
            val cardSource = CardSource(token)

            communicator.getCurrencyConversionQuote(amountOfMoney, cardSource, context, listener)
        }
    }

    fun getSurchargeCalculation(
        context: Context,
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: Int?,
        listener: SurchargeCalculationResponseListener
    ) {
        sessionScope.launch {
            val card = Card(partialCreditCardNumber, paymentProductId)
            val cardSource = CardSource(card)

            communicator.getSurchargeCalculation(amountOfMoney, cardSource, context, listener)
        }
    }

    fun getSurchargeCalculation(
        context: Context,
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: SurchargeCalculationResponseListener
    ) {
        sessionScope.launch {
            val cardSource = CardSource(token)

            communicator.getSurchargeCalculation(amountOfMoney, cardSource, context, listener)
        }
    }

    private fun createPaymentItemCacheKey(
        paymentContext: PaymentContext,
        paymentItemId: String
    ): PaymentProductCacheKey {
        // Create the cache key for this retrieved BasicPaymentItem
        return PaymentProductCacheKey(
            paymentContext.amountOfMoney.amount,
            paymentContext.countryCode,
            paymentContext.amountOfMoney.currencyCode,
            paymentContext.isRecurring,
            paymentItemId
        )
    }

    private fun cachePaymentProduct(paymentProduct: PaymentProduct, paymentContext: PaymentContext) {
        // Add paymentProduct to the paymentProductMapping
        val key = createPaymentItemCacheKey(paymentContext, paymentProduct.id)
        paymentProductMapping[key] = paymentProduct
    }

    private fun onApiError(logTag: String, error: ErrorResponse) {
        if (loggingEnabled) {
            val apiErrorId = error.apiError?.errorId ?: ""
            val apiErrorList = getApiErrorItemListLogs(error.apiError?.errors)
            Log.e(
                "LocalResponseListener",
                "API Error while performing API call for : $logTag \n" +
                        "ErrorResponse message : ${error.message}, \n" +
                        "apiError id: $apiErrorId \nerrorList: $apiErrorList"
            )
        } else {
            Log.e(
                "LocalResponseListener",
                "API Error while performing API call for : $logTag \nErrorResponse message : ${error.message}"
            )
        }
    }

    private fun onApiException(logTag: String, t: Throwable) {
        if (loggingEnabled) {
            Log.e(
                "LocalResponseListener",
                "Exception while performing API call for : $logTag \nException ${t.message}",
                t
            )
        } else {
            Log.e("LocalResponseListener", "Exception while performing API call for : $logTag ")
        }
    }

    private fun getApiErrorItemListLogs(apiErrorItems: List<ApiErrorItem>?): String {
        var errorList = ""

        apiErrorItems?.let {
            for (apiErrorItem in apiErrorItems) {
                errorList += "\nApiErrorItem code: ${apiErrorItem.code} \nmessage: ${apiErrorItem.message}"
            }
        }

        return errorList
    }

    // Set properties on deserialization because CoroutineScope is not Serializable &
    // to avoid making internal classes Serializable
    @Suppress("UnusedPrivateMember")
    private fun readObject(input: ObjectInputStream) {
        input.defaultReadObject()
        this.sessionScope = CoroutineScope(Dispatchers.IO)
        val configuration = C2sCommunicatorConfiguration(
            clientSessionId,
            customerId,
            clientApiUrl,
            assetBaseUrl,
            environmentIsProduction,
            appIdentifier,
            sdkIdentifier
        )
        this.communicator = C2sCommunicator(configuration, loggingEnabled)
    }
}
