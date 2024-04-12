/*
 * Copyright 2017 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.communicate

import android.content.Context
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener
import com.onlinepayments.sdk.client.android.listener.CurrencyConversionResponseListener
import com.onlinepayments.sdk.client.android.listener.GenericResponseListener
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.api.ApiResponse
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.CardSource
import com.onlinepayments.sdk.client.android.model.currencyconversion.Transaction
import com.onlinepayments.sdk.client.android.model.surcharge.request.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.util.ImageUtil
import com.onlinepayments.sdk.client.android.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Creates requests and handles responses of the Online Payments Client API.
 */
internal class C2sCommunicator(
    val configuration: C2sCommunicatorConfiguration,
    loggingEnabled: Boolean
) {
    companion object {
        // Maximum amount of chars which is used for getting PaymentProductId by CreditCardNumber
        private const val MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 8
        private const val MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 6

        // Values used for fixProductFieldsIfRequired
        private const val EXPIRY_DATE_MASK = "{{99}}/{{99}}"
        private const val BASIC_CARD_NUMBER_MASK = "{{9999}} {{9999}} {{9999}} {{9999}}"
        private const val AMEX_CARD_NUMBER_MASK = "{{9999}} {{999999}} {{99999}}"
        private const val AMEX_PRODUCT_ID = "2"
        private const val EXPIRY_DATE = "expiryDate"
        private const val CARD_NUMBER = "cardNumber"
    }

    private val httpClient = HttpClient(loggingEnabled)

    @get:JvmSynthetic
    val loggingEnabled: Boolean
        get() = httpClient.loggingEnabled

    @JvmSynthetic
    val isEnvironmentTypeProduction: Boolean = configuration.environmentIsProduction

    @JvmSynthetic
    fun setLoggingEnabled(loggingEnabled: Boolean) {
        httpClient.setLoggingEnabled(loggingEnabled)
    }

    // Api call for getting basic payment items
    @JvmSynthetic
    suspend fun getBasicPaymentItems(
        context: Context,
        paymentContext: PaymentContext,
        listener: BasicPaymentItemsResponseListener
    ) = withContext(Dispatchers.IO) {
        val basicPaymentProductsResponseListener = object : BasicPaymentProductsResponseListener {
            override fun onSuccess(response: BasicPaymentProducts) {
                val basicPaymentItems = BasicPaymentItems(response.paymentProductsAsItems, response.accountsOnFile)
                listener.onSuccess(basicPaymentItems)
            }

            override fun onApiError(error: ErrorResponse) {
                listener.onApiError(error)
            }

            override fun onException(t: Throwable) {
                listener.onException(t)
            }
        }

        getBasicPaymentProducts(context, paymentContext, basicPaymentProductsResponseListener)
    }

    // API call for getting basic payment products
    @JvmSynthetic
    suspend fun getBasicPaymentProducts(
        context: Context,
        paymentContext: PaymentContext,
        listener: BasicPaymentProductsResponseListener
    ) = withContext(Dispatchers.IO) {
        // Construct the url for the Basic Payment Products call
        val basicPaymentProductsPath =
            Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCTS_PATH.replace("[cid]", configuration.customerId)

        // Add query parameters to the url
        val queryString = StringBuilder()
            .append("?countryCode=").append(paymentContext.countryCode)
            .append("&amount=").append(paymentContext.amountOfMoney.amount)
            .append("&isRecurring=").append(paymentContext.isRecurring)
            .append("&currencyCode=").append(paymentContext.amountOfMoney.currencyCode)
            .append("&hide=fields")
            .append("&").append(createCacheBusterParameter())

        val url = configuration.getClientApiUrl(ApiVersion.V1, basicPaymentProductsPath + queryString)

        val response = httpClient.doHTTPGetRequest<BasicPaymentProducts>(
            url,
            configuration.clientSessionId,
            getMetadata(context)
        )

        response.data?.let {
            filterApplePayGooglePay(context, response.data!!)

            for (basicPaymentProduct in response.data!!.basicPaymentProducts) {
                ImageUtil.setLogoForDisplayHints(basicPaymentProduct.displayHints, context)
                ImageUtil.setLogoForDisplayHintsList(basicPaymentProduct.displayHintsList, context)
            }
        }

        invokeListener(response, listener)
    }

    private fun filterApplePayGooglePay(context: Context, basicPaymentProducts: BasicPaymentProducts) {
        // Remove Apple Pay
        removeApplePayPaymentProduct(basicPaymentProducts)

        // Remove Google Pay if it is returned and is not allowed
        val googlePayPaymentProduct = getPaymentProduct(basicPaymentProducts, Constants.PAYMENTPRODUCTID_GOOGLEPAY)

        if (
            googlePayPaymentProduct != null &&
            !GooglePayUtil.isGooglePayAllowed(context, this, googlePayPaymentProduct)
        ) {
            removePaymentProduct(basicPaymentProducts, googlePayPaymentProduct)
        }
    }

    private fun removeApplePayPaymentProduct(basicPaymentProducts: BasicPaymentProducts) {
        val applePayPaymentProduct = getPaymentProduct(basicPaymentProducts, Constants.PAYMENTPRODUCTID_APPLEPAY)
        removePaymentProduct(basicPaymentProducts, applePayPaymentProduct)
    }

    private fun getPaymentProduct(basicPaymentProducts: BasicPaymentProducts, productId: String): BasicPaymentProduct? {
        for (paymentProduct in basicPaymentProducts.basicPaymentProducts) {
            if (paymentProduct.id.equals(productId)) {
                return paymentProduct
            }
        }

        return null
    }

    private fun removePaymentProduct(basicPaymentProducts: BasicPaymentProducts, paymentProduct: BasicPaymentProduct?) {
        basicPaymentProducts.basicPaymentProducts.remove(paymentProduct)
    }

    // API call for getting payment product
    @JvmSynthetic
    suspend fun getPaymentProduct(
        productId: String,
        context: Context,
        paymentContext: PaymentContext,
        listeners: List<PaymentProductResponseListener>
    ) = withContext(Dispatchers.IO) {
        // Apple Pay is not supported for Android devices
        if (productId == Constants.PAYMENTPRODUCTID_APPLEPAY) {
            val errorResponse = ApiResponse<PaymentProduct>()
            errorResponse.error = ErrorResponse("Apple Pay is not supported on Android devices")
            for (listener in listeners) {
                invokeListener(errorResponse, listener)
            }
            return@withContext
        }

        // Construct the url for the Payment Product call
        val paymentProductPath = Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_PATH
            .replace("[cid]", configuration.customerId)
            .replace("[pid]", productId)

        // Add query parameters to the url
        val queryString = StringBuilder()
            .append("?countryCode=").append(paymentContext.countryCode)
            .append("&amount=").append(paymentContext.amountOfMoney.amount)
            .append("&isRecurring=").append(paymentContext.isRecurring)
            .append("&currencyCode=").append(paymentContext.amountOfMoney.currencyCode)
            .append("&").append(createCacheBusterParameter())

        val url = configuration.getClientApiUrl(ApiVersion.V1, paymentProductPath + queryString)

        val response = httpClient.doHTTPGetRequest<PaymentProduct>(
            url,
            configuration.clientSessionId,
            getMetadata(context)
        )

        response.data?.let { paymentProduct ->
            // Don't return Google Pay if it is not supported for the current payment
            if (
                productId == Constants.PAYMENTPRODUCTID_GOOGLEPAY &&
                !GooglePayUtil.isGooglePayAllowed(context, this@C2sCommunicator, paymentProduct)
            ) {
                response.data = null
                response.error = ErrorResponse("GooglePay is not supported for the current payment")
            } else {
                // Fix product fields if required
                response.data = fixProductFieldsIfRequired(paymentProduct)

                // Set tooltip images and validation rules
                for (paymentProductField in paymentProduct.paymentProductFields) {
                    ImageUtil.setImageForTooltip(paymentProductField.displayHints, context)
                    paymentProductField.setValidationRules()
                }
            }
        }

        for (listener in listeners) {
            invokeListener(response, listener)
        }
    }

    private fun fixProductFieldsIfRequired(paymentProduct: PaymentProduct): PaymentProduct {
        for (field in paymentProduct.paymentProductFields) {
            val fieldId = field.id
            if (fieldId != EXPIRY_DATE && fieldId != CARD_NUMBER) {
                continue
            }

            // If this is the expiry date field, change the mask and possibly the type
            if (EXPIRY_DATE == field.id) {
                // Change the type if it is LIST
                if (field.displayHints.formElement.formElementType == FormElement.Type.LIST) {
                    field.displayHints.formElement.setType(FormElement.Type.TEXT)
                }
                // Add the mask, if it's null or empty
                if (field.displayHints.mask.isNullOrEmpty()) {
                    field.displayHints.mask = EXPIRY_DATE_MASK
                }
            }

            // If this is the card number field, change the mask if it is null or empty
            if (CARD_NUMBER == fieldId && (field.displayHints.mask.isNullOrEmpty())) {
                field.displayHints.mask = if (AMEX_PRODUCT_ID == paymentProduct.id) {
                    // Set American Express card number mask
                    AMEX_CARD_NUMBER_MASK
                } else {
                    BASIC_CARD_NUMBER_MASK
                }
            }
        }
        return paymentProduct
    }

    // API call for getting payment product networks
    @JvmSynthetic
    suspend fun getPaymentProductNetworks(
        productId: String,
        context: Context,
        paymentContext: PaymentContext,
        listener: PaymentProductNetworkResponseListener
    ) = withContext(Dispatchers.IO) {
        // Construct the url for the Payment Product Networks call
        val paymentProductNetworksPath =
            Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_NETWORKS_PATH
                .replace("[cid]", configuration.customerId)
                .replace("[pid]", productId)

        // Add query parameters to the url
        val queryString = StringBuilder()
            .append("?countryCode=").append(paymentContext.countryCode)
            .append("&amount=").append(paymentContext.amountOfMoney.amount)
            .append("&isRecurring=").append(paymentContext.isRecurring)
            .append("&currencyCode=").append(paymentContext.amountOfMoney.currencyCode)
            .append("&").append(createCacheBusterParameter())

        val url = configuration.getClientApiUrl(ApiVersion.V1, paymentProductNetworksPath + queryString)

        val response = httpClient.doHTTPGetRequest<PaymentProductNetworkResponse>(
            url,
            configuration.clientSessionId,
            getMetadata(context)
        )

        invokeListener(response, listener)
    }

    // API call for getting IIN details
    @JvmSynthetic
    suspend fun getIinDetails(
        partialCreditCardNumber: String,
        context: Context,
        paymentContext: PaymentContext,
        listeners: List<IinLookupResponseListener>
    ) = withContext(Dispatchers.IO) {
        var response = ApiResponse<IinDetailsResponse>()

        var partialCCNumber = partialCreditCardNumber

        // If partialCreditCardNumber < minimum nr of characters -> return IinStatus.NOT_ENOUGH_DIGITS
        if (partialCCNumber.length < MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
            response.data = IinDetailsResponse(IinStatus.NOT_ENOUGH_DIGITS)
            for (listener in listeners) {
                invokeListener(response, listener)
            }
            return@withContext
        }

        // Trim partialCreditCardNumber to maxNrCharactersCCNumber digits
        if (partialCCNumber.length >= MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
            partialCCNumber = partialCCNumber.substring(0, MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP)
        } else if (partialCCNumber.length > MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
            partialCCNumber = partialCCNumber.substring(0, MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP)
        }

        // Construct the url for the IIN details call
        val iinDetailsPath =
            Constants.OP_GATEWAY_IIN_LOOKUP_PATH.replace("[cid]", configuration.customerId)
        val url = configuration.getClientApiUrl(ApiVersion.V1, iinDetailsPath)

        val iinDetailsRequest = IinDetailsRequest(partialCCNumber, paymentContext)
        response =
            httpClient.doHTTPPostRequest<IinDetailsRequest, IinDetailsResponse>(
                url,
                configuration.clientSessionId,
                getMetadata(context),
                iinDetailsRequest
            )

        // Determine the result of the lookup
        val status: IinStatus = if (response.error != null || response.data?.paymentProductId == null) {
            // If the iinResponse is null or the paymentProductId is null, then return IinStatus.UNKNOWN
            response.error = null
            IinStatus.UNKNOWN
        } else if (response.data != null && !response.data!!.isAllowedInContext) {
            // If the payment product is currently not allowed, then return IinStatus.SUPPORTED_BUT_NOT_ALLOWED
            IinStatus.EXISTING_BUT_NOT_ALLOWED
        } else if (response.data != null) {
            // This is a correct result, return IinStatus.SUPPORTED
            IinStatus.SUPPORTED
        } else {
            IinStatus.UNKNOWN
        }

        response.data?.apply {
            this.status = status
        } ?: run  {
            response.data = IinDetailsResponse(status)
        }

        for (listener in listeners) {
            invokeListener(response, listener)
        }
    }

    // API call for getting public key
    @JvmSynthetic
    suspend fun getPublicKey(context: Context, listener: PublicKeyResponseListener) = withContext(Dispatchers.IO) {
        // Construct the url for the Public Key call
        val publicKeyPath = Constants.OP_GATEWAY_PUBLIC_KEY_PATH.replace("[cid]", configuration.customerId)
        val url = configuration.getClientApiUrl(ApiVersion.V1, publicKeyPath)

        val response =
            httpClient.doHTTPGetRequest<PublicKeyResponse>(
                url,
                configuration.clientSessionId,
                getMetadata(context)
            )

        invokeListener(response, listener)
    }

    // API call for getting the currency conversion quote
    @JvmSynthetic
    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
        context: Context,
        listener: CurrencyConversionResponseListener
    ) = withContext(Dispatchers.IO) {
        val currencyConversionPath =
            Constants.OP_GATEWAY_CURRENCY_CONVERSION_QUOTE_PATH.replace("[cid]", configuration.customerId)
        val url = configuration.getClientApiUrl(ApiVersion.V2, currencyConversionPath)

        val transaction = Transaction(amountOfMoney)
        val currencyConversionRequest = CurrencyConversionRequest(cardSource, transaction)
        val response =
            httpClient.doHTTPPostRequest<CurrencyConversionRequest, CurrencyConversionResponse>(
                url,
                configuration.clientSessionId,
                getMetadata(context),
                currencyConversionRequest
            )

        invokeListener(response, listener)
    }

    // API call for getting surcharge calculation
    @JvmSynthetic
    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
        context: Context,
        listener: SurchargeCalculationResponseListener
    ) = withContext(Dispatchers.IO) {
        // Construct the url for the Surcharge Calculation call
        val surchargeCalculationPath =
            Constants.OP_GATEWAY_SURCHARGE_CALCULATION_PATH.replace("[cid]", configuration.customerId)
        val url = configuration.getClientApiUrl(ApiVersion.V1, surchargeCalculationPath)

        val surchargeCalculationRequest = SurchargeCalculationRequest(amountOfMoney, cardSource)
        val response =
            httpClient.doHTTPPostRequest<SurchargeCalculationRequest, SurchargeCalculationResponse>(
                url,
                configuration.clientSessionId,
                getMetadata(context),
                surchargeCalculationRequest
            )

        invokeListener(response, listener)
    }

    private suspend fun <T> invokeListener(
        apiResponse: ApiResponse<T>,
        listener: GenericResponseListener<T>
    ) = withContext(Dispatchers.Main) {
        val response = apiResponse.data
        val apiResponseError = apiResponse.error
        if (apiResponseError == null) {
            if (response != null) {
                listener.onSuccess(response)
            } else {
                val error = ErrorResponse("Empty Response without Error")
                listener.onApiError(error)
            }
        } else {
            if (apiResponseError.throwable != null) {
                listener.onException(apiResponseError.throwable!!)
            } else {
                listener.onApiError(apiResponseError)
            }
        }
    }

    @JvmSynthetic
    fun getMetadata(context: Context): Map<String, String> {
        return Util.getMetadata(context, configuration.appIdentifier, configuration.sdkIdentifier)
    }

    private fun createCacheBusterParameter(): String {
        return "cacheBuster=" + Date().time
    }
}
