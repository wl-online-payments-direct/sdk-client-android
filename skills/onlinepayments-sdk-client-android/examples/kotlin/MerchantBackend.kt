package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// [START merchant-backend]
// The URL, paths and JSON shapes are placeholders for your own backend; the SDK never calls them.
private const val BACKEND_URL = "https://shop.example.com"

/** The payment status as your backend reports it. */
enum class PaymentStatus { SUCCESS, PENDING, FAILED }

/** What your backend returns after it created the payment, or when the app asks for the status. */
class MerchantPaymentResponse(
    val status: PaymentStatus,
    /** Set when the customer must be redirected, for example for 3-D Secure. */
    val redirectUrl: String?,
)

/**
 * The customer's currency conversion choice. Your backend passes both fields to Create Payment in
 * cardPaymentMethodSpecificInput.currencyConversion. Both are mandatory once a quote was shown.
 */
class CurrencyConversionChoice(
    val dccSessionId: String,
    /** true: pay in the card's currency; false: pay in your currency. */
    val acceptedByUser: Boolean,
)

// Never log the body you send: it holds the encrypted customer input.
private suspend fun postJson(path: String, body: JSONObject = JSONObject()): JSONObject =
    withContext(Dispatchers.IO) {
        val connection = URL("$BACKEND_URL$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            if (connection.responseCode !in 200..299) {
                throw IOException("$path failed with HTTP ${connection.responseCode}")
            }
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

private fun JSONObject.toPaymentResponse() = MerchantPaymentResponse(
    status = PaymentStatus.valueOf(getString("status").uppercase()),
    redirectUrl = if (isNull("redirectUrl")) null else getString("redirectUrl"),
)

/** Your backend creates a new client session for this checkout (Create Session) and returns its data. */
suspend fun fetchSessionData(): SessionData {
    val json = postJson("/api/checkout/session")
    return SessionData(
        clientSessionId = json.getString("clientSessionId"),
        customerId = json.getString("customerId"),
        clientApiUrl = json.getString("clientApiUrl"),
        assetUrl = json.getString("assetUrl"),
    )
}

/** Your backend creates the payment (Create Payment) with the encrypted customer input. */
suspend fun submitEncryptedPayment(
    orderId: String,
    encryptedRequest: EncryptedRequest,
    currencyConversion: CurrencyConversionChoice? = null, // only when a currency conversion offer was shown
): MerchantPaymentResponse {
    val body = JSONObject()
        .put("orderId", orderId)
        .put("encryptedCustomerInput", encryptedRequest.encryptedCustomerInput)
        .put("encodedClientMetaInfo", encryptedRequest.encodedClientMetaInfo)
    if (currencyConversion != null) {
        body.put(
            "currencyConversion",
            JSONObject()
                .put("dccSessionId", currencyConversion.dccSessionId)
                .put("acceptedByUser", currencyConversion.acceptedByUser),
        )
    }
    return postJson("/api/checkout/payment", body).toPaymentResponse()
}

/** Your backend reads the payment status, for example after the customer returned from 3-D Secure. */
suspend fun fetchPaymentStatus(orderId: String): MerchantPaymentResponse =
    postJson("/api/checkout/status", JSONObject().put("orderId", orderId)).toPaymentResponse()

/** Your backend creates a payment token (Create Token) from the encrypted card data. Returns the token id. */
suspend fun submitEncryptedToken(encryptedRequest: EncryptedRequest): String {
    val body = JSONObject()
        .put("encryptedCustomerInput", encryptedRequest.encryptedCustomerInput)
        .put("encodedClientMetaInfo", encryptedRequest.encodedClientMetaInfo)
    return postJson("/api/checkout/token", body).getString("token")
}
// [END merchant-backend]
