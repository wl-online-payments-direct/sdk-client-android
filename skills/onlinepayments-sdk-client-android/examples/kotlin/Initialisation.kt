package com.example.checkout

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START initialise-sdk]
class CheckoutSession(
    val sdk: OnlinePaymentsSdk,
    val assetUrl: String, // from the session data: needed to build logo URLs
)

// Call once per checkout. Your backend creates new session data with Create Session; it holds no
// secret. Never put the API key or secret in the app.
suspend fun createCheckoutSession(context: Context, isProduction: Boolean): CheckoutSession {
    val sessionData = fetchSessionData()

    // Throws a ConfigurationException when a session value is blank or clientApiUrl is not a valid URL.
    val sdk = OnlinePaymentsSdk(
        sessionData,
        // The SDK keeps the context: pass the application context, never an Activity.
        context.applicationContext,
        SdkConfiguration(
            // true in release builds: it selects the Google Pay PRODUCTION environment.
            environmentIsProduction = isProduction,
            appIdentifier = "MyShopApp/1.0.0", // your app's name and version
            sdkIdentifier = null,
            // loggingEnabled stays false (the default): it must be off in production.
        ),
    )
    return CheckoutSession(sdk, sessionData.assetUrl)
}
// [END initialise-sdk]

// [START payment-context]
// The payment context is passed to most SDK calls. Use the real values of the order being paid: the
// payment products, surcharge and currency conversion depend on them. A PaymentContextWithAmount is
// a PaymentContext that also works for getIinDetails.
fun createPaymentContext(
    totalInMinorUnits: Long, // smallest unit of the currency: 1298 = EUR 12.98
    currencyCode: String, // ISO 4217, for example "EUR"
    countryCode: String, // ISO 3166-1 alpha-2, for example "NL"
): PaymentContextWithAmount = PaymentContextWithAmount(
    AmountOfMoneyWithAmount(totalInMinorUnits, currencyCode),
    countryCode,
    false, // isRecurring
)
// [END payment-context]
