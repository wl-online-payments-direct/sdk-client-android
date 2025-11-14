/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

@file:Suppress("unused")

package com.onlinepayments.sdk.client.android.session

import android.content.Context
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicatorConfiguration
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener
import com.onlinepayments.sdk.client.android.listener.CurrencyConversionResponseListener
import com.onlinepayments.sdk.client.android.listener.GenericResponseListener
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.Card
import com.onlinepayments.sdk.client.android.model.CardSource
import com.onlinepayments.sdk.client.android.model.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentProductCacheKey
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.api.ApiErrorItem
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.util.Logger
import com.onlinepayments.sdk.client.android.util.Util
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable

/**
 * The main entry point of the SDK. The Session object provides methods for getting the data and encrypting the
 * payment request.
 * Use this object to perform Client API requests such as get Payment Products or get IIN Details.
 *
 * @param clientSessionId used for identifying the session on the Online Payments gateway
 * @param customerId used for identifying the customer on the Online Payments gateway
 * @param clientApiUrl the endpoint baseurl
 * @param assetBaseUrl the asset baseurl
 * @param environmentIsProduction states if the environment is production
 * @param appIdentifier used to create device metadata
 * @param loggingEnabled indicates whether requests and responses should be logged to the console; default is false; should be false in production
 */
@Suppress("LongParameterList")
class Session @JvmOverloads constructor(
    private val clientSessionId: String,
    private val customerId: String,
    private val clientApiUrl: String,
    private val assetBaseUrl: String,
    private val environmentIsProduction: Boolean,
    private val appIdentifier: String,
    private var loggingEnabled: Boolean = false,
    private val sdkIdentifier: String,
    private val context: Context,
    private var sessionScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : Serializable {

    private val logger: Logger = LoggerProvider.logger

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
        this.communicator = C2sCommunicator(configuration, loggingEnabled, context)
    }

    /**
     * Overloaded constructor.
     *
     * @param clientSessionId used for identifying the session on the Online Payments gateway
     * @param customerId used for identifying the customer on the Online Payments gateway
     * @param clientApiUrl the endpoint baseurl
     * @param assetBaseUrl the asset baseurl
     * @param environmentIsProduction states if the environment is production
     * @param appIdentifier used to create device metadata
     * @param loggingEnabled indicates whether requests and responses should be logged to the console; default is false; should be false in production
     * @param context The application context
     */
    constructor(
        clientSessionId: String,
        customerId: String,
        clientApiUrl: String,
        assetBaseUrl: String,
        environmentIsProduction: Boolean,
        appIdentifier: String,
        loggingEnabled: Boolean = false,
        context: Context,
    ) : this(
        clientSessionId,
        customerId,
        clientApiUrl,
        assetBaseUrl,
        environmentIsProduction,
        appIdentifier,
        loggingEnabled,
        Constants.SDK_IDENTIFIER,
        context
    )

    /**
     * Gets the logging enabled indicator.
     *
     * @return [Boolean]
     */
    fun getLoggingEnabled(): Boolean {
        return communicator.loggingEnabled
    }

    /**
     * Sets the logging enabled indicator.
     *
     * @param loggingEnabled `True` if the logging should be enabled.
     */
    fun setLoggingEnabled(loggingEnabled: Boolean) {
        this.communicator.setLoggingEnabled(loggingEnabled)
    }

    /**
     * Gets the basic payment items for the provided context.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [BasicPaymentItems]
     */
    suspend fun getBasicPaymentItems(paymentContext: PaymentContext): BasicPaymentItems {
        return invokeApiCall("getBasicPaymentItems") {
            communicator.getBasicPaymentItems(paymentContext)
        }
    }

    /**
     * Synchronous execution of the Gets basic payment items method.
     * Note that this method will block the thread when executed.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [BasicPaymentItems]
     */
    fun getBasicPaymentItemsSync(paymentContext: PaymentContext): BasicPaymentItems = runBlocking {
        getBasicPaymentItems(paymentContext)
    }

    /**
     * Listener-based method for getting the basic payment items for the provided context.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     * @param listener The [BasicPaymentItemsResponseListener] listener instance for handling the response and errors.
     *
     */
    fun getBasicPaymentItems(
        paymentContext: PaymentContext,
        listener: BasicPaymentItemsResponseListener
    ) {
        invokeApiCallWithListener(listener, "getBasicPaymentItems") {
            communicator.getBasicPaymentItems(paymentContext)
        }
    }

    /**
     * Gets the basic payment products for the provided context.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [BasicPaymentProducts]
     */
    suspend fun getBasicPaymentProducts(paymentContext: PaymentContext): BasicPaymentProducts {
        return invokeApiCall("getBasicPaymentProducts") {
            communicator.getBasicPaymentProducts(paymentContext)
        }
    }

    /**
     * Synchronous execution of the Get basic payment products method.
     * Note that this method will block the thread when executed.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [BasicPaymentProducts]
     */
    fun getBasicPaymentProductsSync(paymentContext: PaymentContext): BasicPaymentProducts = runBlocking {
        getBasicPaymentProducts(paymentContext)
    }

    /**
     * Listener-based method for getting the basic payment products for the provided context.
     *
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     * @param listener The [BasicPaymentProductsResponseListener] listener instance for handling the response and errors.
     */
    fun getBasicPaymentProducts(
        paymentContext: PaymentContext,
        listener: BasicPaymentProductsResponseListener
    ) {
        invokeApiCallWithListener(listener, "getBasicPaymentProducts") {
            communicator.getBasicPaymentProducts(paymentContext)
        }
    }

    /**
     * Gets the payment product for the provided identifier and payment context.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [PaymentProduct].
     */
    suspend fun getPaymentProduct(
        paymentProductId: String,
        paymentContext: PaymentContext,
    ): PaymentProduct? {
        // Create a cacheKey for this paymentProduct
        val cacheKey = createPaymentItemCacheKey(paymentProductId, paymentContext)

        // If the paymentProduct is already in the cache, return it
        if (paymentProductMapping.containsKey(cacheKey)) {
            val cachedPaymentProduct = paymentProductMapping[cacheKey]

            if (cachedPaymentProduct != null) {
                return cachedPaymentProduct
            }
        }

        return invokeApiCall("getPaymentProduct") {
            val result = communicator.getPaymentProduct(paymentProductId, paymentContext)
            cachePaymentProduct(result, paymentContext)

            result
        }
    }

    /**
     * Synchronous execution of the Get payment product method.
     * Note that this method will block the thread when executed.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [PaymentProduct] or `null`
     */
    fun getPaymentProductSync(
        paymentProductId: String,
        paymentContext: PaymentContext,
    ): PaymentProduct? = runBlocking {
        getPaymentProduct(paymentProductId, paymentContext)
    }

    /**
     * Listener-based method for getting the payment product for the provided identifier and payment context.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     * @param listener The [PaymentProductResponseListener] listener instance for
     *  handling the response and errors.
     *
     */
    fun getPaymentProduct(
        paymentProductId: String,
        paymentContext: PaymentContext,
        listener: PaymentProductResponseListener
    ) {
        // Create a cacheKey for this paymentProduct
        val cacheKey = createPaymentItemCacheKey(paymentProductId, paymentContext)

        // If the paymentProduct is already in the cache, call the listener with the paymentProduct from the cache
        if (paymentProductMapping.containsKey(cacheKey)) {
            val cachedPaymentProduct = paymentProductMapping[cacheKey]

            if (cachedPaymentProduct != null) {
                listener.onSuccess(cachedPaymentProduct)

                return
            }
        }

        invokeApiCallWithListener(listener, "getPaymentProduct") {
            val result = communicator.getPaymentProduct(paymentProductId, paymentContext)
            cachePaymentProduct(result, paymentContext)

            result
        }
    }

    /**
     * Gets product networks for the payment product and payment context.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [PaymentProductNetworkResponse]
     */
    suspend fun getNetworksForPaymentProduct(
        paymentProductId: String,
        paymentContext: PaymentContext,
    ): PaymentProductNetworkResponse {
        return invokeApiCall("getNetworksForPaymentProduct") {
            communicator.getPaymentProductNetworks(paymentProductId, paymentContext)
        }
    }

    /**
     * Synchronous execution of the get networks for payment product method.
     * Note that this method will block the thread when executed.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     *
     * @return [PaymentProductNetworkResponse]
     */
    fun getNetworksForPaymentProductSync(
        paymentProductId: String,
        paymentContext: PaymentContext,
    ): PaymentProductNetworkResponse = runBlocking {
        getNetworksForPaymentProduct(paymentProductId, paymentContext)
    }

    /**
     * Listener-based method for getting product networks for the payment product and payment context.
     *
     * @param paymentProductId The payment product identifier.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode.
     * @param listener The [PaymentProductNetworkResponseListener] listener instance for handling the response and errors.
     *
     */
    fun getNetworksForPaymentProduct(
        paymentProductId: String,
        paymentContext: PaymentContext,
        listener: PaymentProductNetworkResponseListener
    ) {
        invokeApiCallWithListener(listener, "getNetworksForPaymentProduct") {
            communicator.getPaymentProductNetworks(paymentProductId, paymentContext)
        }
    }

    /**
     * Gets IIN details for the provided partial card number adn payment context.
     *
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode, or `null`.
     *
     * @return [IinDetailsResponse]
     */
    suspend fun getIinDetails(
        partialCreditCardNumber: String,
        paymentContext: PaymentContext?,
    ): IinDetailsResponse {
        // Check if a request is already in progress
        if (iinLookupPending) {
            throw ApiException("IIN lookup is already in progress")
        }

        iinLookupPending = true

        return try {
            invokeApiCall("getIinDetails") {
                communicator.getIinDetails(partialCreditCardNumber, paymentContext)
            }
        } finally {
            iinLookupPending = false
        }
    }

    /**
     * Synchronous execution of the Get IIN details method.
     * Note that this method will block the thread when executed.
     *
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode, or `null`.
     *
     * @return [IinDetailsResponse]
     */
    fun getIinDetailsSync(
        partialCreditCardNumber: String,
        paymentContext: PaymentContext?,
    ): IinDetailsResponse = runBlocking {
        getIinDetails(partialCreditCardNumber, paymentContext)
    }

    /**
     * Listener-based method for Getting IIN details for the provided partial card number.
     *
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param listener The [IinLookupResponseListener] listener instance for
     *  handling the response and errors.
     * @param paymentContext The [PaymentContext] object with information about a payment,
     *  like its [AmountOfMoney] and countryCode, or `null`.
     */
    fun getIinDetails(
        partialCreditCardNumber: String,
        listener: IinLookupResponseListener,
        paymentContext: PaymentContext?
    ) {
        // Only execute the IIN details call when a call is not yet in progress
        if (!iinLookupPending) {
            iinLookupPending = true

            try {
                invokeApiCallWithListener(listener, "getIinDetails") {
                    communicator.getIinDetails(partialCreditCardNumber, paymentContext)
                }
            } finally {
                iinLookupPending = false
            }
        }
    }

    /**
     * Gets the public key needed for data encryption.
     *
     * @return [PublicKeyResponse]
     */
    suspend fun getPublicKey(): PublicKeyResponse {
        return invokeApiCall("getPublicKey") {
            communicator.getPublicKey()
        }
    }

    /**
     * Synchronous execution of the Get public key method.
     * Note that this method will block the thread when executed.
     *
     * @return [PublicKeyResponse]
     */
    fun getPublicKeySync(): PublicKeyResponse = runBlocking {
        getPublicKey()
    }

    /**
     * Listener-based method for getting the public key.
     *
     * @param listener The [PublicKeyResponseListener] listener instance for handling the response and errors.
     */
    fun getPublicKey(listener: PublicKeyResponseListener) {
        invokeApiCallWithListener(listener, "getPublicKey") {
            communicator.getPublicKey()
        }
    }

    /**
     * Prepares the payment request by encrypting its fields.
     *
     * @param paymentRequest The [PaymentRequest] object with payment data.
     *
     * @return [PreparedPaymentRequest]
     */
    suspend fun preparePaymentRequest(paymentRequest: PaymentRequest): PreparedPaymentRequest {
        val metaData = Util.getMetadata(context, appIdentifier, sdkIdentifier)
        val sessionEncryptionHelper = SessionEncryptionHelper.Payment(
            context,
            clientSessionId,
            metaData,
            null,
            paymentRequest,
        )

        return invokeApiCall("preparePaymentRequest") {
            val result = getPublicKey()
            sessionEncryptionHelper.getPreparedRequest(result)
        }
    }

    /**
     * Synchronous execution of the Prepare payment request method.
     * Note that this method will block the thread when executed.
     *
     * @param paymentRequest The [PaymentRequest] object with payment data.
     *
     * @return [PreparedPaymentRequest]
     */
    fun preparePaymentRequestSync(paymentRequest: PaymentRequest): PreparedPaymentRequest = runBlocking {
        preparePaymentRequest(paymentRequest)
    }

    /**
     * Listener-based method for preparing the payment request.
     *
     * @param paymentRequest The [PaymentRequest] object with payment data.
     * @param listener The [PaymentRequestPreparedListener] listener instance for handling the response and errors.
     */
    fun preparePaymentRequest(
        paymentRequest: PaymentRequest,
        listener: PaymentRequestPreparedListener
    ) {
        val metaData = Util.getMetadata(context, appIdentifier, sdkIdentifier)
        val sessionEncryptionHelper = SessionEncryptionHelper.Payment(
            context,
            clientSessionId,
            metaData,
            listener,
            paymentRequest,
        )

        sessionScope.launch {
            withContext(mainDispatcher) {
                val result = communicator.getPublicKey()
                sessionEncryptionHelper.onPublicKeyReceived(result)
            }
        }
    }

    /**
     * Prepares the token request by encrypting its fields.
     *
     * @param tokenRequest The [CreditCardTokenRequest] object with payment data.
     *
     * @return [PreparedPaymentRequest]
     */
    suspend fun prepareTokenPaymentRequest(tokenRequest: CreditCardTokenRequest): PreparedPaymentRequest {
        val metaData = Util.getMetadata(context, appIdentifier, sdkIdentifier)
        val sessionEncryptionHelper = SessionEncryptionHelper.TokenPayment(
            context,
            clientSessionId,
            metaData,
            null,
            tokenRequest,
        )

        return invokeApiCall("prepareTokenPaymentRequest") {
            val result = getPublicKey()
            sessionEncryptionHelper.getPreparedRequest(result)
        }
    }

    /**
     * Synchronous execution of the Prepare token payment request method.
     * Note that this method will block the thread when executed.
     *
     * @param tokenRequest The [CreditCardTokenRequest] object with payment data.
     *
     * @return [PreparedPaymentRequest]
     */
    fun prepareTokenPaymentRequestSync(tokenRequest: CreditCardTokenRequest): PreparedPaymentRequest = runBlocking {
        prepareTokenPaymentRequest(tokenRequest)
    }

    /**
     * Listener-based method for preparing the token request.
     *
     * @param tokenRequest The [CreditCardTokenRequest] object with payment data.
     * @param listener The [PaymentRequestPreparedListener] listener instance for handling the response and errors.
     */
    fun prepareTokenPaymentRequest(
        tokenRequest: CreditCardTokenRequest,
        listener: PaymentRequestPreparedListener
    ) {
        val metaData = Util.getMetadata(context, appIdentifier, sdkIdentifier)
        val sessionEncryptionHelper = SessionEncryptionHelper.TokenPayment(
            context,
            clientSessionId,
            metaData,
            listener,
            tokenRequest,
        )

        sessionScope.launch {
            withContext(mainDispatcher) {
                val result = communicator.getPublicKey()
                sessionEncryptionHelper.onPublicKeyReceived(result)
            }
        }
    }

    /**
     * Gets the currency conversion quote for the provided context and the product identifier.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     *
     * @return [CurrencyConversionResponse]
     */
    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): CurrencyConversionResponse {
        return invokeApiCall("getCurrencyConversionQuote") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())
            val cardSource = CardSource(card)

            communicator.getCurrencyConversionQuote(amountOfMoney, cardSource)
        }
    }

    /**
     * Synchronous execution of the Get currency conversion quote method.
     * Note that this method will block the thread when executed.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     *
     * @return [CurrencyConversionResponse]
     */
    fun getCurrencyConversionQuoteSync(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): CurrencyConversionResponse = runBlocking {
        getCurrencyConversionQuote(amountOfMoney, partialCreditCardNumber, paymentProductId)
    }

    /**
     * Listener-based method for getting the currency conversion quote for the provided context and partial card number.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     * @param listener The [CurrencyConversionResponseListener] listener instance for handling the response and errors.
     */
    fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
        listener: CurrencyConversionResponseListener
    ) {
        invokeApiCallWithListener(listener, "getCurrencyConversionQuote") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            communicator.getCurrencyConversionQuote(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Gets the currency conversion quote for the provided context and payment token.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     *
     * @return [CurrencyConversionResponse]
     */
    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): CurrencyConversionResponse {
        return invokeApiCall("getCurrencyConversionQuote") {
            communicator.getCurrencyConversionQuote(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Synchronous execution of the Get currency conversion quote method.
     * Note that this method will block the thread when executed.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     *
     * @return [CurrencyConversionResponse]
     */
    fun getCurrencyConversionQuoteSync(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): CurrencyConversionResponse = runBlocking {
        getCurrencyConversionQuote(amountOfMoney, token)
    }

    /**
     * Listener-based method for getting the currency conversion quote for the provided context and payment token.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     * @param listener The [CurrencyConversionResponseListener] listener instance for
     *  handling the response and errors.
     */
    fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: CurrencyConversionResponseListener
    ) {
        invokeApiCallWithListener(listener, "getCurrencyConversionQuote") {
            communicator.getCurrencyConversionQuote(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Gets the surcharge calculation for the provided context and partial card number.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     *
     * @return [SurchargeCalculationResponse]
     */
    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): SurchargeCalculationResponse {
        return invokeApiCall("getSurchargeCalculation") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            communicator.getSurchargeCalculation(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Synchronous version of the Get surcharge calculation method.
     * Note that this method will block the thread when executed.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     *
     * @return [SurchargeCalculationResponse]
     */
    fun getSurchargeCalculationSync(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): SurchargeCalculationResponse = runBlocking {
        getSurchargeCalculation(amountOfMoney, partialCreditCardNumber, paymentProductId)
    }

    /**
     * Listener-based method for getting the surcharge calculation for the provided context and partial card number.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param partialCreditCardNumber The first six digits of the credit card number.
     * @param paymentProductId The payment product identifier.
     * @param listener The [SurchargeCalculationResponseListener] listener instance for
     *  handling the response and errors.
     */
    fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
        listener: SurchargeCalculationResponseListener
    ) {
        invokeApiCallWithListener(listener, "getSurchargeCalculation") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            communicator.getSurchargeCalculation(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Gets the surcharge calculation for the provided context and payment token.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     *
     * @return [SurchargeCalculationResponse]
     */
    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): SurchargeCalculationResponse {
        return invokeApiCall("getSurchargeCalculation") {
            communicator.getSurchargeCalculation(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Synchronous execution of the Get the surcharge calculation quote.
     * Note that this method will block the thread when executed.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     *
     * @return [SurchargeCalculationResponse]
     */
    fun getSurchargeCalculationSync(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): SurchargeCalculationResponse = runBlocking {
        getSurchargeCalculation(amountOfMoney, token)
    }

    /**
     * Listener-based method for getting the surcharge calculation for the provided context and payment token.
     *
     * @param amountOfMoney The [AmountOfMoney] object with amount and currency code.
     * @param token The token from a successful payment.
     * @param listener The [SurchargeCalculationResponseListener] listener instance for
     *  handling the response and errors.
     */
    fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: SurchargeCalculationResponseListener
    ) {
        invokeApiCallWithListener(listener, "getSurchargeCalculation") {
            communicator.getSurchargeCalculation(amountOfMoney, CardSource(token))
        }
    }

    private fun createPaymentItemCacheKey(
        paymentItemId: String,
        paymentContext: PaymentContext
    ): PaymentProductCacheKey {
        // Create the cache key for this retrieved BasicPaymentItem
        return PaymentProductCacheKey(
            paymentContext.amountOfMoney?.amount!!,
            paymentContext.countryCode!!,
            paymentContext.amountOfMoney?.currencyCode!!,
            paymentContext.isRecurring,
            paymentItemId
        )
    }

    private fun cachePaymentProduct(
        paymentProduct: PaymentProduct,
        paymentContext: PaymentContext
    ) {
        // Add paymentProduct to the paymentProductMapping
        val key = createPaymentItemCacheKey(paymentProduct.getId()!!, paymentContext)
        paymentProductMapping[key] = paymentProduct
    }

    private suspend inline fun <T> invokeApiCall(
        logTag: String = "",
        crossinline block: suspend () -> T
    ): T {
        try {
            val result = block()

            return result
        } catch (e: ApiException) {
            val errorResponse = e.errorResponse ?: ErrorResponse(e.message)
            onApiError(logTag, errorResponse)

            throw e
        } catch (e: Exception) {
            onApiException(logTag, e)

            throw e
        }
    }

    private inline fun <T> invokeApiCallWithListener(
        listener: GenericResponseListener<T>,
        logTag: String = "",
        crossinline block: suspend () -> T
    ) {
        sessionScope.launch {
            try {
                val result = block()

                withContext(mainDispatcher) {
                    listener.onSuccess(result)
                }
            } catch (e: ApiException) {
                val errorResponse = e.errorResponse ?: ErrorResponse(e.message)
                onApiError("$logTag[ListenerBased]", errorResponse)

                withContext(mainDispatcher) {
                    listener.onApiError(errorResponse)
                }
            } catch (e: Exception) {
                onApiException(logTag, e)
                withContext(mainDispatcher) {
                    listener.onException(e)
                }
            }
        }
    }

    private fun onApiError(logTag: String, error: ErrorResponse) {
        if (getLoggingEnabled()) {
            val apiErrorId = error.apiError?.errorId ?: ""
            val apiErrorList = getApiErrorItemListLogs(error.apiError?.errors)
            logger.e(
                "LocalResponseListener",
                "API Error while performing API call for `$logTag` \n" +
                    "ErrorResponse message : ${error.message}, \n" +
                    "apiError id: $apiErrorId \n" +
                    "errorList: $apiErrorList"
            )
        } else {
            logger.e(
                "LocalResponseListener",
                "API Error while performing API call for `$logTag` \n" +
                    "ErrorResponse message : ${error.message}"
            )
        }
    }

    private fun onApiException(logTag: String, t: Throwable) {
        if (getLoggingEnabled()) {
            logger.e(
                "LocalResponseListener",
                "Exception while performing API call for `$logTag` \n" +
                    "Exception ${t.message}",
                t
            )
        } else {
            logger.e("LocalResponseListener", "Exception while performing API call for `$logTag`")
        }
    }

    private fun getApiErrorItemListLogs(apiErrorItems: List<ApiErrorItem>?): String {
        val errorList: MutableList<String> = mutableListOf()

        apiErrorItems?.let {
            for (apiErrorItem in apiErrorItems) {
                errorList.addAll(
                    arrayOf(
                        "",
                        "ApiErrorItem errorCode: ${apiErrorItem.errorCode}",
                        "message: ${apiErrorItem.message}"
                    )
                )
            }
        }

        return errorList.joinToString("\n")
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4386453L

        var mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    }
}
