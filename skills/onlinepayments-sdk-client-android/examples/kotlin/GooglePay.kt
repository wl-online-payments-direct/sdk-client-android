package com.example.checkout

import androidx.activity.ComponentActivity
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Currency

// [START google-pay-configuration]
// The SDK has no Google Pay payment flow: it does not show the payment sheet. It lists Google Pay
// (product 320) only when the device can pay with it, and returns the gateway and card networks
// for the payment sheet request.
class GooglePayConfiguration(val gateway: String, val networks: List<String>)

fun googlePayConfiguration(product: BasicPaymentProduct): GooglePayConfiguration? {
    val data = product.paymentProduct320SpecificData ?: return null
    val gateway = data.gateway ?: return null
    return GooglePayConfiguration(gateway, data.networks.orEmpty().filterNotNull())
}
// [END google-pay-configuration]

// [START google-pay-request]
// Google wants the price as a decimal string in the currency's units ("12.98"), not in minor units.
// The number of decimals depends on the currency (JPY has none, KWD has three).
fun googlePayAmount(totalInMinorUnits: Long, currencyCode: String): String {
    val fractionDigits = Currency.getInstance(currencyCode).defaultFractionDigits.coerceAtLeast(0)
    return BigDecimal.valueOf(totalInMinorUnits, fractionDigits).toPlainString()
}

fun buildPaymentDataRequest(
    configuration: GooglePayConfiguration,
    gatewayMerchantId: String, // your merchant id (PSPID) on the Worldline platform
    googleMerchantId: String?, // from the Google Pay & Wallet Console; required in production
    merchantName: String,
    paymentContext: PaymentContext, // the real order values
): PaymentDataRequest {
    val total = requireNotNull(paymentContext.amountOfMoney.amount) { "The payment context needs an amount" }
    val cardMethod = JSONObject()
        .put("type", "CARD")
        .put(
            "parameters",
            JSONObject()
                .put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
                .put("allowedCardNetworks", JSONArray(configuration.networks)),
        )
        .put(
            "tokenizationSpecification",
            JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put(
                    "parameters",
                    JSONObject()
                        .put("gateway", configuration.gateway)
                        .put("gatewayMerchantId", gatewayMerchantId),
                ),
        )
    val merchantInfo = JSONObject().put("merchantName", merchantName)
    if (googleMerchantId != null) {
        merchantInfo.put("merchantId", googleMerchantId)
    }

    val request = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)
        .put("allowedPaymentMethods", JSONArray().put(cardMethod))
        .put("merchantInfo", merchantInfo)
        .put(
            "transactionInfo",
            JSONObject()
                .put("totalPriceStatus", "FINAL")
                .put("totalPrice", googlePayAmount(total, paymentContext.amountOfMoney.currencyCode))
                .put("currencyCode", paymentContext.amountOfMoney.currencyCode)
                // Required for merchants in the European Economic Area.
                .put("countryCode", requireNotNull(paymentContext.countryCode)),
        )
    return PaymentDataRequest.fromJson(request.toString())
}
// [END google-pay-request]

// [START google-pay-launcher]
sealed interface GooglePayResult {
    class Success(val paymentData: PaymentData) : GooglePayResult

    data object Cancelled : GooglePayResult // the customer closed the sheet: not an error

    class Failed(val statusCode: Int) : GooglePayResult
}

// Create it while the Activity is being created (before it is started), for example as a property.
class GooglePayLauncher(
    activity: ComponentActivity,
    isProduction: Boolean, // the same value as SdkConfiguration.environmentIsProduction
    onResult: (GooglePayResult) -> Unit,
) {
    private val paymentsClient = Wallet.getPaymentsClient(
        activity,
        Wallet.WalletOptions.Builder()
            .setEnvironment(if (isProduction) WalletConstants.ENVIRONMENT_PRODUCTION else WalletConstants.ENVIRONMENT_TEST)
            .build(),
    )

    private val launcher = activity.registerForActivityResult(TaskResultContracts.GetPaymentDataResult()) { result ->
        val paymentData = result.result
        when {
            result.status.statusCode == CommonStatusCodes.SUCCESS && paymentData != null ->
                onResult(GooglePayResult.Success(paymentData))
            result.status.statusCode == CommonStatusCodes.CANCELED -> onResult(GooglePayResult.Cancelled)
            // Log only the status code: never the PaymentData or the token.
            else -> onResult(GooglePayResult.Failed(result.status.statusCode))
        }
    }

    fun start(request: PaymentDataRequest) {
        launcher.launch(paymentsClient.loadPaymentData(request))
    }
}
// [END google-pay-launcher]

// [START google-pay-payment-request]
// Put the Google Pay token into a PaymentRequest for product 320 and pay it like a card payment:
// encrypt it (see payWithEncryptedRequest) and send the result to your backend. Never log, store or
// send the token or the PaymentData itself.
suspend fun createGooglePayPaymentRequest(
    sdk: OnlinePaymentsSdk,
    paymentContext: PaymentContext,
    paymentData: PaymentData,
): PaymentRequest {
    val token = JSONObject(paymentData.toJson())
        .getJSONObject("paymentMethodData")
        .getJSONObject("tokenizationData")
        .getString("token")

    val googlePayProduct = sdk.getPaymentProduct(GOOGLE_PAY_PAYMENT_PRODUCT_ID, paymentContext)
    val paymentRequest = PaymentRequest(googlePayProduct)

    // The one required field of product 320. The token is set as a quoted JSON string (as in the
    // SDK's example apps).
    paymentRequest.setValue("encryptedPaymentData", JSONObject.quote(token))
    return paymentRequest
}
// [END google-pay-payment-request]
