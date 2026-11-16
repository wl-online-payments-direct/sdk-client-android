# Android SDK reference

## Installation

Add the dependency to your app module's `build.gradle.kts`, where `x.y.z` is the SDK version:

```kotlin
dependencies {
    implementation("com.worldline-solutions:sdk-client-android:x.y.z")
}
```

The library needs `minSdk` 23 or higher and is compiled with `compileSdk` 35. Its manifest already
declares the `INTERNET` and `ACCESS_NETWORK_STATE` permissions, and it ships its own R8/ProGuard
rules, so you add neither.

The SDK declares its own dependencies (coroutines, Retrofit, Gson, OkHttp, Google Play services
Wallet) as `implementation`, so they are **not** on your compile classpath. The examples also use
these libraries. Add them to your app when you use that code:

- `org.jetbrains.kotlinx:kotlinx-coroutines-android`: `launch` and the other coroutine builders
  (Kotlin code).
- `androidx.lifecycle:lifecycle-runtime-ktx`: `lifecycleScope`.
- `androidx.activity:activity` (or `activity-ktx`): `ComponentActivity` and the Activity Result API.
  The Activity Result API needs `androidx.fragment:fragment` 1.3.0 or newer; Play services bring an
  older one, so add it unless your app already uses AppCompat or Compose.
- `com.google.android.gms:play-services-wallet`: the Google Pay payment sheet. The SDK does not show
  it (see [flows.md](flows.md#google-pay)).

The examples are compiled with these versions. Use them, or newer ones, and do not go below them:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("com.google.android.gms:play-services-wallet:19.5.0")
}
```

## Imports

The SDK has no single entry file. The classes live in these packages under
`com.onlinepayments.sdk.client.android`:

- `facade`: `OnlinePaymentsSdk`; `facade.listeners`: the listener interfaces.
- `domain`: `PaymentContext`, `PaymentContextWithAmount`, `AmountOfMoney`, `AmountOfMoneyWithAmount`.
- `domain.configuration`: `SdkConfiguration`, `SessionData`.
- `domain.paymentProduct`, `domain.paymentProduct.productField`: `BasicPaymentProduct(s)`,
  `PaymentProduct`, `PaymentProductField`.
- `domain.accountOnFile`: `AccountOnFile`.
- `domain.paymentRequest`: `PaymentRequest`, `CreditCardTokenRequest`, `EncryptedRequest`.
- `domain.validation`: `ValidationResult`, `ValidationErrorMessage`.
- `domain.iin`: `IinDetailsResponse`, `IinDetailStatus`.
- `domain.surchargeCalculation`, `domain.currencyConversion`: the offers.
- `domain.exceptions`: the exceptions (see [errors.md](errors.md)).

The IDE adds each import from the example code.

## Three variants of every method

Each SDK method that calls the Client API exists three times:

| Variant | Signature | Runs on | Use it |
|---|---|---|---|
| `suspend` | `suspend fun getPaymentProduct(...)` | Can be called from the main thread: the SDK switches to `Dispatchers.IO` itself | From Kotlin, inside a coroutine (recommended) |
| `...Sync` | `fun getPaymentProductSync(...)` | Blocks the calling thread (`runBlocking`) | Only from your own background thread. Never on the main thread: it freezes the UI. |
| Listener | `fun getPaymentProduct(..., listener)` | The SDK launches the call in its own scope. `onSuccess` and `onFailure` run on the main thread (`Dispatchers.Main`) | From Java |

Rules for choosing:

- Kotlin: use the `suspend` variants. Throw and catch exceptions as usual.
- Java: use the listener variants (see [java.md](java.md)). Use `...Sync` only on a background
  thread you own.
- The listener variants keep running when your screen is closed. Do not touch views or a captured
  Activity in the callback without checking that the screen still exists, or deliver the result to a
  `ViewModel` instead.
- Always override both `onSuccess` and `onFailure` in a listener (Kotlin `object :` or Java anonymous
  class). A listener that does not override `onFailure` ignores every failure without any message.
- `getIinDetails` takes its parameters in a different order in the listener variant:
  `getIinDetails(partialCreditCardNumber, listener, paymentContext)`.

The SDK caches payment products, IIN details, surcharge and currency conversion results and the
public key for the lifetime of the `OnlinePaymentsSdk` instance, so create one instance per checkout.

## SDK methods

`OnlinePaymentsSdk(sessionData, context, configuration)` creates the SDK. `context` is your
application context. `configuration` is an `SdkConfiguration(environmentIsProduction, appIdentifier,
sdkIdentifier, loggingEnabled = false)`. `environmentIsProduction` selects the Google Pay environment
and should be `true` in release builds; `appIdentifier` is your app's name and version; leave
`sdkIdentifier` `null`. From Java, pass all four values.

| Method (the `suspend` variant) | Client API operation | Returns |
|---|---|---|
| `getBasicPaymentProducts(paymentContext)` | Get payment products | `BasicPaymentProducts` (`paymentProducts`, `accountsOnFile`) |
| `getPaymentProduct(paymentProductId: Int, paymentContext)` | Get payment product | `PaymentProduct` |
| `getNetworksForPaymentProduct(paymentProductId: Int, paymentContext)` | Get payment product networks | `PaymentProductNetworksResponse` |
| `getIinDetails(partialCreditCardNumber, paymentContext: PaymentContextWithAmount)` | Get IIN details | `IinDetailsResponse` |
| `getSurchargeCalculation(amountOfMoney, partialCreditCardNumber, paymentProductId: String?)` | Surcharge calculation | `SurchargeCalculationResponse` |
| `getSurchargeCalculation(amountOfMoney, token)` | Surcharge calculation for a stored card | `SurchargeCalculationResponse` |
| `getCurrencyConversionQuote(amountOfMoney, partialCreditCardNumber, paymentProductId: String?)` | Currency conversion quote | `CurrencyConversionResponse` |
| `getCurrencyConversionQuote(amountOfMoney, token)` | Currency conversion quote for a stored card | `CurrencyConversionResponse` |
| `encryptPaymentRequest(paymentRequest)` | Gets the public key, then encrypts on the device | `EncryptedRequest` |
| `encryptTokenRequest(tokenRequest)` | Gets the public key, then encrypts on the device | `EncryptedRequest` |
| `getPublicKey()` | Get public key | Used internally by the encrypt methods; do not use it to encrypt yourself |

For a stored card, `token` is the account on file's `id`. For a card being entered, pass the first 8
digits as `partialCreditCardNumber`: the SDK sends exactly what you pass in these two requests, and
they are not encrypted. `getIinDetails` takes the first 6 to 8 digits and uses at most 8.

`encodedClientMetaInfo` contains device information and the `appIdentifier`. The SDK collects it;
you only forward it.

## Domain objects

- `BasicPaymentProducts`: `paymentProducts`, `accountsOnFile`.
- `BasicPaymentProduct`: `id` (`Int?`), `label`, `logo` (a partial path), `displayOrder`,
  `allowsTokenization`, `allowsRecurring`, `usesRedirectionTo3rdParty`, `accountsOnFile`,
  `getAccountOnFile(id)`, `paymentProduct302SpecificData`, `paymentProduct320SpecificData`.
- `PaymentProduct`: everything above plus `fields` (sorted by display order), `getField(id)`,
  `requiredFields`.
- `PaymentProductField`: `id`, `type`, `label`, `placeholder`, `isRequired`, `shouldObfuscate`,
  `displayHints` (`mask`, `preferredInputType`, `displayOrder`, ...), `applyMask(value)`,
  `removeMask(value)`, `validate(value)` (returns `List<ValidationErrorMessage>`).
- `PaymentRequest(paymentProduct, accountOnFile, tokenize)`: `getField(id)`, `setValue(id, value)`,
  `getValue(id)`, `getValues()`, `validate()` (returns `ValidationResult` with `isValid` and
  `errors`), `getAccountOnFile()`, `setAccountOnFile()`, `getTokenize()`, `setTokenize()`.
- `PaymentRequestField`: `setValue()` (removes the mask), `getValue()`, `getMaskedValue()`,
  `clearValue()`, `validate()`.
- `AccountOnFile`: `id`, `paymentProductId`, `label`, `getValue(fieldId)`, `isWritable(fieldId)`.
- `ValidationErrorMessage`: `errorMessage` (English text), `paymentProductFieldId`, `type` (the
  name of a `ValidationRuleType`, for example `"LUHN"` or `"REQUIRED"`). To show your own translated
  texts, map `type` to your texts.
- `IinDetailsResponse`: `status`, `paymentProductId` (a `String?`), `coBrands`, `cardType`.

## Your backend

The examples call your own backend through the functions in `examples/kotlin/MerchantBackend.kt`
(Java: `examples/java/MerchantBackend.java`). The URL, paths and response shapes are placeholders;
the SDK does not call them. The same file also has `submitEncryptedToken` (tokenization).

Your backend:

- creates the client session with Create Session and returns `clientSessionId`, `customerId`,
  `clientApiUrl` and `assetUrl`. To show stored cards, it passes the customer's tokens to Create
  Session.
- creates the payment with Create Payment, passing `encryptedCustomerInput` and
  `encodedClientMetaInfo`, the order's amount, and `returnUrl` (your app's deep link) for 3-D
  Secure. With a currency conversion choice it sets
  `cardPaymentMethodSpecificInput.currencyConversion` to `{ dccSessionId, acceptedByUser }`. With
  surcharging it sets `order.surchargeSpecificInput.mode` to `on-behalf-of`.
- returns the redirect URL from the Create Payment response when there is one, and the status.
- returns the current payment status when the app asks after the customer came back.

<!-- region: merchant-backend -->
```kotlin
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
```

## Deep link for the return from 3-D Secure

The customer returns to the app through the `returnUrl` your backend sent in Create Payment.

- Choose a return URL that only your app handles. An `https` link verified with Android App Links
  (`android:autoVerify="true"` and an `assetlinks.json` file on your domain) is the safest. A custom
  scheme such as `myshop://payment-return` also works, but any other app can register the same scheme.
  With either, never read the payment outcome from the link: ask your backend for the status.
- Add an intent filter for the return URL to the root Activity of the app, the one with the launcher
  intent filter. Give it `android:launchMode="singleTask"` (or `"singleTop"`), so that a return link
  reaches the running Activity through `onNewIntent` and does not create a second instance. The
  Activity must be `android:exported="true"`.

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myshop" android:host="payment-return" />
    </intent-filter>
</activity>
```

- `RETURN_URL_PREFIX` in `examples/kotlin/RedirectHandling.kt` must match the scheme and host.
- The Activity handles the link in `onCreate` (the system stopped the app, and the link starts it) and
  in `onNewIntent` (the app is running). See [flows.md](flows.md#10-handle-the-result).
