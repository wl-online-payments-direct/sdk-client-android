/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.card.Card
import com.onlinepayments.sdk.client.android.domain.card.CardSource
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.facade.helpers.ServiceCallWrapper
import com.onlinepayments.sdk.client.android.facade.helpers.SessionDataNormalizer
import com.onlinepayments.sdk.client.android.facade.helpers.SessionDataValidator
import com.onlinepayments.sdk.client.android.facade.listeners.BasicPaymentProductsResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.CurrencyConversionResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.IinLookupResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductNetworkResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.facade.listeners.PublicKeyResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.SurchargeCalculationResponseListener
import com.onlinepayments.sdk.client.android.infrastructure.factories.ServiceFactory
import com.onlinepayments.sdk.client.android.infrastructure.factories.ServiceFactoryConfiguration
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IServiceFactory
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger
import com.onlinepayments.sdk.client.android.services.interfaces.IClientService
import com.onlinepayments.sdk.client.android.services.interfaces.IEncryptionService
import com.onlinepayments.sdk.client.android.services.interfaces.IPaymentProductService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/**
 * Main entry point for the Online Payments Android SDK.
 *
 * This class provides methods to interact with the Online Payments platform, including:
 * - Retrieving payment products and their details
 * - Looking up card details via IIN (Issuer Identification Number)
 * - Getting currency conversion quotes and surcharge calculations
 * - Encrypting payment requests for secure transmission
 *
 * ## Usage
 *
 * Initialize the SDK with session data obtained from your server:
 *
 * ```kotlin
 * val sessionData = SessionData(
 *     clientSessionId = "session-id-from-server",
 *     customerId = "customer-id-from-server",
 *     clientApiUrl = "https://api.example.com",
 *     assetUrl = "https://assets.example.com"
 * )
 *
 * val configuration = SdkConfiguration(
 *     applicationIdentifier = "MyApp/1.0.0",
 *     loggingEnabled = true
 * )
 *
 * val sdk = OnlinePaymentsSdk(sessionData, context, configuration)
 * ```
 *
 * ## Threading
 *
 * The SDK provides three variants for most operations:
 * - **Suspend functions**: For use with Kotlin coroutines (recommended)
 * - **Sync functions**: Blocking calls that run on the current thread
 * - **Listener functions**: Callback-based for Java compatibility, callbacks run on main thread
 *
 * @see SessionData
 * @see SdkConfiguration
 * @see PaymentContext
 */
class OnlinePaymentsSdk {
    private val sessionData: SessionData
    private val context: Context
    private val configuration: SdkConfiguration?
    private val sessionScope: CoroutineScope
    private val serviceFactory: IServiceFactory
    private val encryptionService: IEncryptionService
    private val paymentProductService: IPaymentProductService
    private val clientService: IClientService
    private val logger: Logger = LoggerProvider.logger
    private var serviceCallWrapper: ServiceCallWrapper

    /**
     * Creates an instance of the Online Payments SDK.
     *
     * @param sessionData Session details obtained from your server by calling the
     *                    Create Client Session API. Contains client session ID, customer ID,
     *                    and API URLs.
     * @param context Android application context
     * @param configuration Optional SDK configuration including application identifier and logging settings
     *
     * @see SessionData
     * @see SdkConfiguration
     */
    constructor(
        sessionData: SessionData,
        context: Context,
        configuration: SdkConfiguration?
    ) : this(
        sessionData,
        context,
        configuration,
        CoroutineScope(SupervisorJob() + Dispatchers.IO),
        null
    )

    internal constructor(
        sessionData: SessionData,
        context: Context,
        configuration: SdkConfiguration?,
        sessionScope: CoroutineScope,
        factory: IServiceFactory?
    ) {
        SessionDataValidator.validateRequiredFields(sessionData)
        val normalizedSessionData = SessionDataNormalizer.normalize(sessionData)

        this.sessionData = normalizedSessionData
        this.context = context
        this.configuration = configuration
        this.sessionScope = sessionScope

        this.serviceFactory = factory ?: ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = normalizedSessionData,
                configuration = configuration,
                context = context,
                apiLogger = ApiLogger,
            )
        )

        this.encryptionService = serviceFactory.encryptionService
        this.paymentProductService = serviceFactory.paymentProductService
        this.clientService = serviceFactory.clientService

        serviceCallWrapper = ServiceCallWrapper(sessionScope, mainDispatcher, logger)
    }

    /**
     * Retrieves basic payment products available for the given payment context.
     *
     * This method fetches payment products that match the specified amount, currency,
     * country code, and recurrence status. Results are cached to improve performance.
     * Products in the unavailable list are automatically filtered out.
     *
     * @param paymentContext The payment context containing:
     *   - amountOfMoney: Payment amount and currency
     *   - countryCode: ISO 3166-1 alpha-2 country code
     *   - isRecurring: Whether this is a recurring payment
     *
     * @see BasicPaymentProducts
     * @see PaymentContext
     * @see BasicPaymentProductsResponseListener
     */
    suspend fun getBasicPaymentProducts(paymentContext: PaymentContext): BasicPaymentProducts {
        return serviceCallWrapper.wrap("getBasicPaymentProducts") {
            paymentProductService.getBasicPaymentProducts(paymentContext)
        }
    }

    /**
     * Synchronous variant of [getBasicPaymentProducts].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getBasicPaymentProducts
     */
    fun getBasicPaymentProductsSync(paymentContext: PaymentContext): BasicPaymentProducts = runBlocking {
        getBasicPaymentProducts(paymentContext)
    }

    /**
     * Retrieves basic payment products available for the given payment context.
     *
     * This method fetches payment products that match the specified amount, currency,
     * country code, and recurrence status. Results are cached to improve performance.
     * Products in the unavailable list are automatically filtered out.
     *
     * @param paymentContext The payment context containing:
     *   - amountOfMoney: Payment amount and currency
     *   - countryCode: ISO 3166-1 alpha-2 country code
     *   - isRecurring: Whether this is a recurring payment
     * @param listener Callback invoked on the main thread when operation completes
     *   - onSuccess: Called with BasicPaymentProducts on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see BasicPaymentProducts
     * @see PaymentContext
     * @see BasicPaymentProductsResponseListener
     */
    fun getBasicPaymentProducts(
        paymentContext: PaymentContext,
        listener: BasicPaymentProductsResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getBasicPaymentProducts") {
            paymentProductService.getBasicPaymentProducts(paymentContext)
        }
    }

    /**
     * Retrieves detailed information for a specific payment product.
     *
     * This method fetches complete payment product details including fields, validation rules,
     * display hints, and account-on-file data. Results are cached to improve performance.
     *
     * @param paymentProductId The payment product identifier (e.g., "1" for Visa, "302" for American Express)
     * @param paymentContext The payment context containing amount, currency, country code, and recurrence
     * @return PaymentProduct containing fields, validation rules, and display information
     *
     * @throws ResponseException if the API returns an error (e.g., product not found, invalid context)
     * @throws CommunicationException if network communication fails
     *
     * @see PaymentProduct
     * @see PaymentContext
     * @see PaymentProductResponseListener
     */
    suspend fun getPaymentProduct(
        paymentProductId: Int,
        paymentContext: PaymentContext,
    ): PaymentProduct {
        return serviceCallWrapper.wrap("getPaymentProduct") {
            paymentProductService.getPaymentProduct(paymentProductId, paymentContext)
        }
    }

    /**
     * Synchronous variant of [getPaymentProduct].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getPaymentProduct
     */
    fun getPaymentProductSync(
        paymentProductId: Int,
        paymentContext: PaymentContext,
    ): PaymentProduct = runBlocking {
        getPaymentProduct(paymentProductId, paymentContext)
    }

    /**
     * Callback-based variant of [getPaymentProduct].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with PaymentProduct on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getPaymentProduct
     * @see PaymentProductResponseListener
     */
    fun getPaymentProduct(
        paymentProductId: Int,
        paymentContext: PaymentContext,
        listener: PaymentProductResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getPaymentProduct") {
            paymentProductService.getPaymentProduct(paymentProductId, paymentContext)
        }
    }

    /**
     * Retrieves available payment product networks for a specific payment product.
     *
     * Some payment products support multiple networks (e.g., co-branded cards).
     * This method returns the available networks for the specified payment product.
     *
     * @param paymentProductId The payment product identifier
     * @param paymentContext The payment context
     * @return PaymentProductNetworkResponse containing available networks
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see PaymentProductNetworksResponse
     * @see PaymentProductNetworkResponseListener
     */
    suspend fun getNetworksForPaymentProduct(
        paymentProductId: Int,
        paymentContext: PaymentContext,
    ): PaymentProductNetworksResponse {
        return serviceCallWrapper.wrap("getNetworksForPaymentProduct") {
            paymentProductService.getPaymentProductNetworks(paymentProductId, paymentContext)
        }
    }

    /**
     * Synchronous variant of [getNetworksForPaymentProduct].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getNetworksForPaymentProduct
     */
    fun getNetworksForPaymentProductSync(
        paymentProductId: Int,
        paymentContext: PaymentContext,
    ): PaymentProductNetworksResponse = runBlocking {
        getNetworksForPaymentProduct(paymentProductId, paymentContext)
    }

    /**
     * Callback-based variant of [getNetworksForPaymentProduct].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with PaymentProductNetworkResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getNetworksForPaymentProduct
     * @see PaymentProductNetworkResponseListener
     */
    fun getNetworksForPaymentProduct(
        paymentProductId: Int,
        paymentContext: PaymentContext,
        listener: PaymentProductNetworkResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getNetworksForPaymentProduct") {
            paymentProductService.getPaymentProductNetworks(paymentProductId, paymentContext)
        }
    }

    /**
     * Looks up payment product details using an Issuer Identification Number (IIN).
     *
     * The IIN is the first 6-8 digits of a payment card number. This method can be used
     * to identify the card type and payment product as the customer enters their card number,
     * enabling features like:
     * - Displaying the correct card logo
     * - Pre-selecting the payment product
     * - Validating card number format
     *
     * Results are cached to avoid repeated lookups for the same IIN.
     *
     * @param partialCreditCardNumber The first 6+ digits of the card number (minimum 6 digits required)
     * @param paymentContext Optional payment context for additional validation
     * @return IinDetailsResponse containing payment product ID, card type, and co-brands
     *
     * @see IinDetailsResponse
     * @see IinLookupResponseListener
     */
    suspend fun getIinDetails(
        partialCreditCardNumber: String,
        paymentContext: PaymentContextWithAmount,
    ): IinDetailsResponse {
        return serviceCallWrapper.wrap("getIinDetails") {
            clientService.getIinDetails(partialCreditCardNumber, paymentContext)
        }
    }

    /**
     * Synchronous variant of [getIinDetails].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getIinDetails
     */
    fun getIinDetailsSync(
        partialCreditCardNumber: String,
        paymentContext: PaymentContextWithAmount,
    ): IinDetailsResponse = runBlocking {
        getIinDetails(partialCreditCardNumber, paymentContext)
    }

    /**
     * Callback-based variant of [getIinDetails].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with IinDetailsResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getIinDetails
     * @see IinLookupResponseListener
     */
    fun getIinDetails(
        partialCreditCardNumber: String,
        listener: IinLookupResponseListener,
        paymentContext: PaymentContextWithAmount
    ) {
        serviceCallWrapper.wrap(listener, "getIinDetails") {
            clientService.getIinDetails(partialCreditCardNumber, paymentContext)
        }
    }

    /**
     * Retrieves a currency conversion quote for Dynamic Currency Conversion (DCC).
     *
     * DCC allows customers to see the payment amount in their card's currency before
     * completing the transaction. This method provides the conversion rate and converted amount.
     *
     * @param amountOfMoney The payment amount in the merchant's currency
     * @param partialCreditCardNumber The customer's card number (at least first 6 digits)
     * @param paymentProductId Optional payment product ID for the card
     * @return CurrencyConversionResponse containing the conversion quote
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see CurrencyConversionResponse
     * @see AmountOfMoney
     */
    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): CurrencyConversionResponse {
        return serviceCallWrapper.wrap("getCurrencyConversionQuote") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())
            val cardSource = CardSource(card)

            clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)
        }
    }

    /**
     * Synchronous variant of [getCurrencyConversionQuote].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getCurrencyConversionQuote
     */
    fun getCurrencyConversionQuoteSync(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): CurrencyConversionResponse = runBlocking {
        getCurrencyConversionQuote(amountOfMoney, partialCreditCardNumber, paymentProductId)
    }

    /**
     * Callback-based variant of [getCurrencyConversionQuote].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with CurrencyConversionResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getCurrencyConversionQuote
     * @see CurrencyConversionResponseListener
     */
    fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
        listener: CurrencyConversionResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getCurrencyConversionQuote") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            clientService.getCurrencyConversionQuote(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Retrieves a currency conversion quote for Dynamic Currency Conversion (DCC) using a payment token.
     *
     * DCC allows customers to see the payment amount in their card's currency before
     * completing the transaction. This variant uses a saved payment token instead of card details.
     *
     * @param amountOfMoney The payment amount in the merchant's currency
     * @param token The payment token representing the customer's card
     * @return CurrencyConversionResponse containing the conversion quote
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see CurrencyConversionResponse
     * @see AmountOfMoney
     */
    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): CurrencyConversionResponse {
        return serviceCallWrapper.wrap("getCurrencyConversionQuote") {
            clientService.getCurrencyConversionQuote(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Synchronous variant of [getCurrencyConversionQuote].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getCurrencyConversionQuote
     */
    fun getCurrencyConversionQuoteSync(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): CurrencyConversionResponse = runBlocking {
        getCurrencyConversionQuote(amountOfMoney, token)
    }

    /**
     * Callback-based variant of [getCurrencyConversionQuote].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with CurrencyConversionResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getCurrencyConversionQuote
     * @see CurrencyConversionResponseListener
     */
    fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: CurrencyConversionResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getCurrencyConversionQuote") {
            clientService.getCurrencyConversionQuote(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Calculates the surcharge amount for a payment.
     *
     * Some merchants are allowed to add a surcharge (additional fee) for certain payment methods.
     * This method calculates the surcharge amount based on the payment amount and card details.
     *
     * @param amountOfMoney The base payment amount
     * @param partialCreditCardNumber The customer's card number (at least first 6 digits)
     * @param paymentProductId Optional payment product ID for the card
     * @return SurchargeCalculationResponse containing surcharge details and total amount
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see SurchargeCalculationResponse
     * @see AmountOfMoney
     */
    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): SurchargeCalculationResponse {
        return serviceCallWrapper.wrap("getSurchargeCalculation") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            clientService.getSurchargeCalculation(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Synchronous variant of [getSurchargeCalculation].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getSurchargeCalculation
     */
    fun getSurchargeCalculationSync(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
    ): SurchargeCalculationResponse = runBlocking {
        getSurchargeCalculation(amountOfMoney, partialCreditCardNumber, paymentProductId)
    }

    /**
     * Callback-based variant of [getSurchargeCalculation].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with SurchargeCalculationResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getSurchargeCalculation
     * @see SurchargeCalculationResponseListener
     */
    fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        partialCreditCardNumber: String,
        paymentProductId: String?,
        listener: SurchargeCalculationResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getSurchargeCalculation") {
            val card = Card(partialCreditCardNumber, paymentProductId?.toInt())

            clientService.getSurchargeCalculation(amountOfMoney, CardSource(card))
        }
    }

    /**
     * Calculates the surcharge amount for a payment using a payment token.
     *
     * Some merchants are allowed to add a surcharge (additional fee) for certain payment methods.
     * This variant uses a saved payment token instead of card details.
     *
     * @param amountOfMoney The base payment amount
     * @param token The payment token representing the customer's card
     * @return SurchargeCalculationResponse containing surcharge details and total amount
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see SurchargeCalculationResponse
     * @see AmountOfMoney
     */
    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): SurchargeCalculationResponse {
        return serviceCallWrapper.wrap("getSurchargeCalculation") {
            clientService.getSurchargeCalculation(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Synchronous variant of [getSurchargeCalculation].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getSurchargeCalculation
     */
    fun getSurchargeCalculationSync(
        amountOfMoney: AmountOfMoney,
        token: String,
    ): SurchargeCalculationResponse = runBlocking {
        getSurchargeCalculation(amountOfMoney, token)
    }

    /**
     * Callback-based variant of [getSurchargeCalculation].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with SurchargeCalculationResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getSurchargeCalculation
     * @see SurchargeCalculationResponseListener
     */
    fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        token: String,
        listener: SurchargeCalculationResponseListener
    ) {
        serviceCallWrapper.wrap(listener, "getSurchargeCalculation") {
            clientService.getSurchargeCalculation(amountOfMoney, CardSource(token))
        }
    }

    /**
     * Encrypts a payment request for secure transmission to your server.
     *
     * This method:
     * 1. Validates the payment request to ensure all required fields are present and valid
     * 2. Fetches the public encryption key from the API (if not cached)
     * 3. Encrypts the sensitive payment data using JWE (RSA-OAEP + A256CBC-HS512)
     * 4. Returns encrypted customer input and encoded client metadata
     *
     * The encrypted data should be sent to your server, which forwards it to the
     * Online Payments Server API to process the payment.
     *
     * ## Example
     *
     * ```kotlin
     * val paymentProduct = sdk.getPaymentProduct("1", paymentContext)
     * val paymentRequest = PaymentRequest(paymentProduct)
     *
     * paymentRequest.setValue("cardNumber", "4567350000427977")
     * paymentRequest.setValue("expiryDate", "1226")
     * paymentRequest.setValue("cvv", "123")
     * paymentRequest.setValue("cardholderName", "John Doe")
     *
     * val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)
     * // Send encryptedRequest.encryptedCustomerInput to your server
     * ```
     *
     * @param paymentRequest The payment request containing customer payment data
     * @return EncryptedRequest containing encrypted customer input and encoded metadata
     *
     * @throws EncryptionException if the payment request is invalid or encryption fails
     * @throws CommunicationException if fetching the public key fails
     *
     * @see PaymentRequest
     * @see EncryptedRequest
     * @see PaymentRequestPreparedListener
     */
    suspend fun encryptPaymentRequest(paymentRequest: PaymentRequest): EncryptedRequest {
        return serviceCallWrapper.wrap("createPaymentRequest") {
            encryptionService.encryptPaymentRequest(paymentRequest)
        }
    }

    /**
     * Synchronous variant of [encryptPaymentRequest].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see encryptPaymentRequest
     */
    fun encryptPaymentRequestSync(paymentRequest: PaymentRequest): EncryptedRequest = runBlocking {
        encryptPaymentRequest(paymentRequest)
    }

    /**
     * Callback-based variant of [encryptPaymentRequest].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when encryption completes
     *   - onSuccess: Called with EncryptedRequest on success
     *   - onFailure: Called with EncryptionException if encryption fails
     *
     * @see encryptPaymentRequest
     * @see PaymentRequestPreparedListener
     */
    fun encryptPaymentRequest(
        paymentRequest: PaymentRequest,
        listener: PaymentRequestPreparedListener
    ) {
        serviceCallWrapper.wrap(listener, "encryptPaymentRequest") {
            encryptionService.encryptPaymentRequest(paymentRequest)
        }
    }

    /**
     * Encrypts a credit card token request for secure transmission.
     *
     * This method encrypts essential credit card details (card number, expiry, CVV) for
     * creating a payment token via the Server API. Unlike [encryptPaymentRequest], this method
     * does not perform validation since the token request is not tied to a payment product.
     *
     * The encrypted data should be sent to your server, which uses the Server SDK to create
     * a payment token that can be used for future payments.
     *
     * ## Example
     *
     * ```kotlin
     * val tokenRequest = CreditCardTokenRequest()
     * tokenRequest.cardNumber = "4567350000427977"
     * tokenRequest.expiryDate = "1226"
     * tokenRequest.securityCode = "123"
     * tokenRequest.cardholderName = "John Doe"
     * tokenRequest.paymentProductId = 1
     *
     * val encryptedRequest = sdk.encryptTokenRequest(tokenRequest)
     * // Send encryptedRequest.encryptedCustomerInput to your server
     * ```
     *
     * @param tokenRequest The token request containing credit card details
     * @return EncryptedRequest containing encrypted customer input and encoded metadata
     *
     * @throws EncryptionException if encryption fails
     * @throws CommunicationException if fetching the public key fails
     *
     * @see CreditCardTokenRequest
     * @see EncryptedRequest
     * @see PaymentRequestPreparedListener
     */
    suspend fun encryptTokenRequest(tokenRequest: CreditCardTokenRequest): EncryptedRequest {
        return serviceCallWrapper.wrap("createTokenPaymentRequest") {
            encryptionService.encryptTokenPaymentRequest(tokenRequest)
        }
    }

    /**
     * Synchronous variant of [encryptTokenRequest].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see encryptTokenRequest
     */
    fun encryptTokenRequestSync(tokenRequest: CreditCardTokenRequest): EncryptedRequest = runBlocking {
        encryptTokenRequest(tokenRequest)
    }

    /**
     * Callback-based variant of [encryptTokenRequest].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when encryption completes
     *   - onPaymentRequestPrepared: Called with EncryptedRequest on success
     *   - onFailure: Called with EncryptionException if encryption fails
     *
     * @see encryptTokenRequest
     * @see PaymentRequestPreparedListener
     */
    fun encryptTokenRequest(
        tokenRequest: CreditCardTokenRequest,
        listener: PaymentRequestPreparedListener
    ) {
        serviceCallWrapper.wrap(listener, "encryptTokenRequest") {
            encryptionService.encryptTokenPaymentRequest(tokenRequest)
        }
    }

    /**
     * Retrieves the public encryption key from the API.
     *
     * This method is typically called internally by [encryptPaymentRequest] and
     * [encryptTokenRequest]. You usually don't need to call this method directly
     * unless you're implementing custom encryption logic.
     *
     * The public key is used to encrypt sensitive payment data before transmission.
     *
     * @return PublicKeyResponse containing the public key and key identifier
     *
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if network communication fails
     *
     * @see PublicKeyResponse
     * @see encryptPaymentRequest
     * @see encryptTokenRequest
     */
    suspend fun getPublicKey(): PublicKeyResponse {
        return serviceCallWrapper.wrap("getPublicKey") {
            encryptionService.getPublicKey()
        }
    }

    /**
     * Synchronous variant of [getPublicKey].
     *
     * **Warning**: This method blocks the current thread. Use the suspend variant for Kotlin
     * coroutines or the listener variant for callbacks.
     *
     * @see getPublicKey
     */
    fun getPublicKeySync(): PublicKeyResponse = runBlocking {
        getPublicKey()
    }

    /**
     * Callback-based variant of [getPublicKey].
     *
     * The listener callbacks are invoked on the main thread.
     *
     * @param listener Callback invoked when operation completes
     *   - onSuccess: Called with PublicKeyResponse on successful retrieval
     *   - onFailure: Called with SdkException if request fails
     *
     * @see getPublicKey
     * @see PublicKeyResponseListener
     */
    fun getPublicKey(listener: PublicKeyResponseListener) {
        serviceCallWrapper.wrap(listener, "getPublicKey") {
            encryptionService.getPublicKey()
        }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -2677783756091827465L

        var mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    }
}
