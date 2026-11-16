# Checkout flows (Kotlin)

Every code block below is copied from a file in `../examples/kotlin/`. The Java version of the same
steps is in [java.md](java.md). The examples call your backend through the functions in
`examples/kotlin/MerchantBackend.kt`; see [android.md](android.md#your-backend) for that contract.

The examples are plain functions and classes, not screens. Call the `suspend` functions from a
coroutine that follows your screen's lifecycle, for example `lifecycleScope.launch { ... }` or a
`ViewModel`'s `viewModelScope`. Keep the `OnlinePaymentsSdk` instance in the `ViewModel` or another
holder that survives a configuration change, but not longer than the checkout.

## 1. Create the SDK

Your backend creates a client session with Create Session and returns its data. Do this for every
checkout, and keep the `OnlinePaymentsSdk` instance for the whole checkout.

<!-- region: initialise-sdk -->
```kotlin
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
```

The SDK has no close or dispose method. When the checkout ends (paid, cancelled or restarted with new
session data), drop every reference to the instance.

## 2. Build the payment context

The payment context is passed to most SDK calls. The payment products, surcharge and currency
conversion depend on it, so it must match the order.

<!-- region: payment-context -->
```kotlin
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
```

## 3. List payment products and stored cards

`getBasicPaymentProducts` returns the payment products available for this context and the
customer's accounts on file (stored cards). Accounts on file are only returned when your backend
passed the customer's tokens to Create Session. The SDK already removes the products it cannot
process (Apple Pay, 302, among them). It keeps Google Pay (320) when the device can pay with it, so
remove it when your app does not implement Google Pay (see [Google Pay](#google-pay)). The SDK does
not sort the products. A product's `logo` is a partial path; build the full URL with the session's
`assetUrl`.

<!-- region: list-payment-products -->
```kotlin
// Google Pay (320) is returned by the SDK when the device can pay with it, but the payment sheet
// is your code (see the Google Pay section). Leave it out when your app does not implement it.
const val GOOGLE_PAY_PAYMENT_PRODUCT_ID = 320

// product.logo is a partial path: build the full URL with the assetUrl of the session data.
fun logoUrl(assetUrl: String, logo: String?): String? {
    if (logo.isNullOrEmpty()) {
        return null
    }
    return assetUrl.trimEnd('/') + "/" + logo.trimStart('/')
}

class StoredAccount(
    val accountOnFile: AccountOnFile, // accountOnFile.label is the display text, for example "4242 **** **** 4242"
    val logoUrl: String?,
)

class PaymentOptions(
    val storedAccounts: List<StoredAccount>,
    val products: List<BasicPaymentProduct>, // show each with logoUrl(assetUrl, product.logo)
)

suspend fun loadPaymentOptions(
    sdk: OnlinePaymentsSdk,
    paymentContext: PaymentContext,
    assetUrl: String, // from the session data
): PaymentOptions {
    // Throws a ResponseException with httpStatusCode 404 when no payment product is available.
    val basicPaymentProducts = sdk.getBasicPaymentProducts(paymentContext)

    // Accounts on file are the customer's stored cards. They are only returned when your backend
    // passed the customer's tokens to Create Session.
    val storedAccounts = basicPaymentProducts.accountsOnFile.map { accountOnFile ->
        val product = basicPaymentProducts.paymentProducts.firstOrNull { it.id == accountOnFile.paymentProductId }
        StoredAccount(accountOnFile, logoUrl(assetUrl, product?.logo))
    }

    // The SDK does not sort the products: sort them by displayOrder.
    val products = basicPaymentProducts.paymentProducts
        .filter { it.id != GOOGLE_PAY_PAYMENT_PRODUCT_ID }
        .sortedBy { it.displayOrder?.toInt() ?: Int.MAX_VALUE }

    return PaymentOptions(storedAccounts, products)
}
```

## 4. Render the payment form

When the customer selects a product or a stored card, get the full product. It contains the fields
to render. For a stored card, pass the account on file's `paymentProductId`.

<!-- region: get-payment-product -->
```kotlin
class SelectedProduct(
    val paymentProduct: PaymentProduct,
    val accountOnFile: AccountOnFile?,
)

suspend fun loadSelectedProduct(
    sdk: OnlinePaymentsSdk,
    paymentContext: PaymentContext,
    paymentProductId: Int, // for a stored card: accountOnFile.paymentProductId
    accountOnFileId: String?,
): SelectedProduct {
    val paymentProduct = sdk.getPaymentProduct(paymentProductId, paymentContext)
    val accountOnFile = accountOnFileId?.let { paymentProduct.getAccountOnFile(it) }
    return SelectedProduct(paymentProduct, accountOnFile)
}
```

Describe the form from the product's fields, then render one input per field. Fields of a stored
card that are not writable are shown with their stored value and cannot be edited.

<!-- region: describe-form-fields -->
```kotlin
class FieldSpec(
    val id: String,
    val label: String,
    val placeholder: String?,
    val required: Boolean,
    val obfuscate: Boolean, // the input must hide the text, like a password
    val type: PaymentProductFieldType?,
    val inputType: Int, // an android.text.InputType value for the EditText or keyboard options
    val prefilledValue: String?, // the stored value of the account on file, for example a masked card number
    val readOnly: Boolean, // a field of the selected account on file that the customer cannot change
)

// Build the form from the fields the API returns. Do not hard-code fields, masks or validation
// rules: they differ per payment product and per merchant configuration. The SDK returns the
// fields sorted by display order.
fun describeFormFields(paymentProduct: PaymentProduct, accountOnFile: AccountOnFile?): List<FieldSpec> =
    paymentProduct.fields.map { field ->
        FieldSpec(
            id = field.id,
            label = field.label,
            placeholder = field.placeholder,
            required = field.isRequired,
            obfuscate = field.shouldObfuscate,
            type = field.type,
            inputType = androidInputType(field),
            prefilledValue = accountOnFile?.getValue(field.id),
            readOnly = accountOnFile != null && !accountOnFile.isWritable(field.id),
        )
    }

private fun androidInputType(field: PaymentProductField): Int {
    // A field with a mask (spaces, slashes) needs a text input type: a numeric input type removes
    // the characters of the mask from the text.
    val numeric = field.displayHints.mask == null &&
        field.displayHints.preferredInputType == PreferredInputType.INTEGER_KEYBOARD
    val base = when {
        numeric -> InputType.TYPE_CLASS_NUMBER
        field.displayHints.preferredInputType == PreferredInputType.EMAIL_ADDRESS_KEYBOARD ->
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        field.displayHints.preferredInputType == PreferredInputType.PHONE_NUMBER_KEYBOARD -> InputType.TYPE_CLASS_PHONE
        else -> InputType.TYPE_CLASS_TEXT
    }
    return when {
        field.shouldObfuscate && numeric -> base or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        field.shouldObfuscate -> base or InputType.TYPE_TEXT_VARIATION_PASSWORD
        // Keep the keyboard from learning or suggesting what the customer types.
        base == InputType.TYPE_CLASS_TEXT -> base or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        else -> base
    }
}
```

## 5. Detect the card brand

When at least 6 digits have been entered, get the IIN details to find the payment product for the
card. Use the returned `paymentProductId` (a `String`) to show the brand logo and, if it differs from
the selected product, load that product with `getPaymentProduct` and create a new `PaymentRequest`
for it. A co-badged card has entries in `coBrands`; their `paymentProductId` can be `null`.

What `getIinDetails` returns and throws:

- Fewer than 6 digits: it does not call the API and returns `IinDetailStatus.NOT_ENOUGH_DIGITS`.
- A card number that matches no payment product of your account (the API answers HTTP 404): it
  returns `IinDetailStatus.UNKNOWN`.
- A card that is known but not accepted for this payment: `EXISTING_BUT_NOT_ALLOWED`. An accepted
  card: `SUPPORTED`. `UNSUPPORTED` is declared but never returned.
- Every other API error, for example an expired session, is thrown as a `ResponseException`. Unlike
  the 404, it is not turned into `UNKNOWN`.
- A call while another IIN lookup is running throws an `IllegalStateSdkException`.

<!-- region: iin-details -->
```kotlin
sealed interface CardDetection {
    data object NumberTooShort : CardDetection

    class Supported(
        val paymentProductId: Int,
        val coBrandProductIds: List<Int>, // co-branded card: let the customer choose the brand
    ) : CardDetection

    data object NotAllowed : CardDetection // a known card, but not accepted for this payment

    data object Unknown : CardDetection // see the comment below
}

// Create one CardDetector per checkout and call detect() while the customer types the card number.
class CardDetector(
    private val sdk: OnlinePaymentsSdk,
    private val paymentContext: PaymentContextWithAmount, // getIinDetails needs the amount
) {
    // The SDK allows one IIN lookup at a time: a second call while one is running throws an
    // IllegalStateSdkException. The Mutex makes later calls wait.
    private val lookupLock = Mutex()

    suspend fun detect(cardNumberInput: String): CardDetection {
        val digits = cardNumberInput.filter { it.isDigit() }
        if (digits.length < 6) {
            return CardDetection.NumberTooShort // the SDK would answer NOT_ENOUGH_DIGITS without a request
        }

        // The SDK sends only the first 8 digits. A ResponseException (for example an expired
        // session) is thrown to the caller: see error handling.
        val response = lookupLock.withLock { sdk.getIinDetails(digits, paymentContext) }

        return when (response.status) {
            IinDetailStatus.SUPPORTED -> {
                val paymentProductId = response.paymentProductId?.toIntOrNull()
                    ?: return CardDetection.Unknown
                val coBrandProductIds = response.coBrands.orEmpty()
                    .filterNotNull()
                    .filter { it.isAllowedInContext == true }
                    .mapNotNull { it.paymentProductId?.toIntOrNull() }
                CardDetection.Supported(paymentProductId, coBrandProductIds)
            }
            IinDetailStatus.EXISTING_BUT_NOT_ALLOWED -> CardDetection.NotAllowed
            IinDetailStatus.NOT_ENOUGH_DIGITS -> CardDetection.NumberTooShort
            // UNKNOWN: the card number matches no payment product of your account (the API answered
            // HTTP 404). Do not block the form. UNSUPPORTED is declared but never returned.
            else -> CardDetection.Unknown
        }
    }
}
```

## 6. Format and validate input

Create the `PaymentRequest` once the product is loaded.

<!-- region: create-payment-request -->
```kotlin
fun createPaymentRequest(
    paymentProduct: PaymentProduct,
    accountOnFile: AccountOnFile?,
    customerWantsToStoreCard: Boolean,
): PaymentRequest {
    // Ask to store the card only when the product allows it and the customer agreed.
    val tokenize = customerWantsToStoreCard && paymentProduct.allowsTokenization == true
    return PaymentRequest(paymentProduct, accountOnFile, tokenize)
}
```

Format each writable field while the customer types. The value is stored in the `PaymentRequest` and
shown with the field's mask.

<!-- region: mask-input -->
```kotlin
// Stores the text the customer typed in the PaymentRequest and returns it with the field's mask.
// setValue removes the mask before it stores the value, so "4111 1111" is stored as "41111111".
// An empty string clears the value. Do not call it for a read-only field of the selected account
// on file: setValue throws an InvalidArgumentException for those (see FieldSpec.readOnly).
// Use it from a TextWatcher (below) or from the onValueChange of a Compose TextField.
fun formatFieldInput(paymentRequest: PaymentRequest, fieldId: String, input: String): String {
    val field = paymentRequest.getField(fieldId)
    field.setValue(input)
    return field.getMaskedValue().orEmpty()
}

// Add to the EditText of a writable field: editText.addTextChangedListener(PaymentFieldWatcher(...)).
// After the mask is applied, the cursor moves to the end of the text.
class PaymentFieldWatcher(
    private val paymentRequest: PaymentRequest,
    private val fieldId: String,
) : TextWatcher {
    private var updating = false

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(editable: Editable) {
        if (updating) {
            return
        }
        val masked = formatFieldInput(paymentRequest, fieldId, editable.toString())
        if (masked != editable.toString()) {
            updating = true
            editable.replace(0, editable.length, masked)
            updating = false
        }
    }
}
```

<!-- region: validate-field -->
```kotlin
// Call when a field loses focus. Returns the messages to show next to the field. The texts are in
// English: to translate them, map ValidationErrorMessage.type (the name of a ValidationRuleType,
// for example "LUHN" or "REQUIRED") to your own texts.
fun fieldErrors(paymentRequest: PaymentRequest, fieldId: String): List<String> =
    paymentRequest.getField(fieldId).validate().errors.map { it.errorMessage }
```

Before encrypting, validate the whole request.

<!-- region: validate-payment-request -->
```kotlin
// Call before encrypting. Returns the error messages per field id; empty when the request is valid.
// With an account on file, the SDK validates the fields the customer must enter (MUST_WRITE) and
// the fields the customer changed.
fun validatePaymentRequest(paymentRequest: PaymentRequest): Map<String, List<String>> =
    paymentRequest.validate().errors.groupBy(
        keySelector = { it.paymentProductFieldId },
        valueTransform = { it.errorMessage },
    )
```

## 7. Surcharge and currency conversion (optional)

Only when your merchant account has surcharging or dynamic currency conversion enabled. Call these
once the card product is known, and show the result before the customer pays.

<!-- region: card-source -->
```kotlin
// The card for a surcharge or currency conversion request. These requests are not encrypted: send
// only the first 8 digits of a card number being entered (at least 6) with the product id from the
// IIN details, never the complete number. The SDK sends what you pass. For a stored card, send the
// account on file id (the token) instead.
sealed interface QuoteCard {
    class Entered(cardNumberDigits: String, val paymentProductId: Int) : QuoteCard {
        val firstDigits: String = cardNumberDigits.take(8)
    }

    class Stored(val accountOnFileId: String) : QuoteCard
}

private suspend fun surchargeCalculation(
    sdk: OnlinePaymentsSdk,
    amount: AmountOfMoney,
    card: QuoteCard,
): SurchargeCalculationResponse = when (card) {
    is QuoteCard.Entered ->
        sdk.getSurchargeCalculation(amount, card.firstDigits, card.paymentProductId.toString())
    is QuoteCard.Stored -> sdk.getSurchargeCalculation(amount, card.accountOnFileId)
}

private suspend fun currencyConversionQuote(
    sdk: OnlinePaymentsSdk,
    amount: AmountOfMoney,
    card: QuoteCard,
): CurrencyConversionResponse = when (card) {
    is QuoteCard.Entered ->
        sdk.getCurrencyConversionQuote(amount, card.firstDigits, card.paymentProductId.toString())
    is QuoteCard.Stored -> sdk.getCurrencyConversionQuote(amount, card.accountOnFileId)
}
```

### Surcharge

The payment context keeps the net amount (without surcharge); the platform adds the surcharge when
your backend creates the payment with `order.surchargeSpecificInput.mode` set to `on-behalf-of`.
Show the surcharge and the total before the customer pays.

The API leaves the amounts out when no surcharge applies. The properties of `Surcharge` are declared
non-null but are `null` at runtime in that case, so read `result` first and the amounts only when it
is `SurchargeResult.OK`. `Surcharge` also has `netAmountOrNull`, `surchargeAmountOrNull` and
`totalAmountOrNull`, which return `null` as a declared value.

<!-- region: surcharge-calculation -->
```kotlin
class SurchargeOffer(val surchargeAmount: AmountOfMoney, val totalAmount: AmountOfMoney)

// Call once the card is known. Returns null when no surcharge applies.
// netAmount is the order amount from the payment context: it does not include the surcharge. The
// platform adds the surcharge when your backend creates the payment with
// order.surchargeSpecificInput.mode set to "on-behalf-of".
suspend fun getSurcharge(sdk: OnlinePaymentsSdk, netAmount: AmountOfMoney, card: QuoteCard): SurchargeOffer? {
    val surcharge = surchargeCalculation(sdk, netAmount, card).surcharges.firstOrNull() ?: return null

    // Check the result before reading the amounts: the API leaves them out when no surcharge
    // applies, and a property of the surcharge is then null although its Kotlin type is not.
    if (surcharge.result != SurchargeResult.OK) {
        return null
    }

    // Show both amounts to the customer before they pay.
    return SurchargeOffer(surcharge.surchargeAmount, surcharge.totalAmount)
}
```

### Currency conversion

Dynamic currency conversion (eDCC, Visa and Mastercard only) lets the customer pay in their card's
currency. The card schemes' rules apply to how you show the offer:

- Show both amounts as two equal options: same size, colour and font. Include your currency, the
  card's currency, the exchange rate, the mark-up and `proposal.disclaimerDisplay`.
- Preselect neither option. Do not use a yes/no or accept/decline question, a switch or a checkbox:
  the customer must actively choose one amount.
- If the customer chooses your currency, continue in your currency and do not offer the conversion
  again in this checkout.

When the result is not `ALLOWED`, `proposal` is absent and `dccSessionId` is empty. They are `null`
at runtime although the types do not allow it: check the result before you read them, or use `proposalOrNull` and `dccSessionIdOrNull`, which return
`null` as a declared value.

<!-- region: currency-conversion -->
```kotlin
class CurrencyConversionOffer(val dccSessionId: String, val proposal: DccProposal)

// Call once the card is known. Returns null when there is no offer: then pay in your currency and
// send no currency conversion data.
// amountToPay is the order amount, or the surcharge's totalAmount when a surcharge applies.
suspend fun getCurrencyConversionOffer(
    sdk: OnlinePaymentsSdk,
    amountToPay: AmountOfMoney,
    card: QuoteCard,
): CurrencyConversionOffer? {
    val response = currencyConversionQuote(sdk, amountToPay, card)

    // INVALID_CARD, INVALID_MERCHANT, NO_RATE and NOT_AVAILABLE mean: no offer. dccSessionId is
    // then empty and the response has no proposal (a null that the Kotlin type does not allow):
    // check the result before reading response.proposal.
    if (response.result.result != ConversionResultType.ALLOWED) {
        return null
    }

    // Show two options with the same size, colour and font: proposal.baseAmount (your currency)
    // and proposal.targetAmount (the card's currency), with proposal.rate.exchangeRate,
    // proposal.rate.markUpRate (a percentage: 3.12 means 3.12 %) and proposal.disclaimerDisplay.
    // Preselect neither option and do not ask a yes/no question: the customer must actively choose.
    return CurrencyConversionOffer(response.dccSessionId, response.proposal)
}
```

When the customer has chosen, send the choice with the payment. Your backend passes it to Create
Payment as `cardPaymentMethodSpecificInput.currencyConversion`. Both `dccSessionId` and
`acceptedByUser` are required, also when the customer chose your currency (`acceptedByUser: false`).

<!-- region: currency-conversion-choice -->
```kotlin
// Call when the customer actively selects one of the two options. Pass the result to
// submitEncryptedPayment, also when the customer chose your currency. Once the customer chose
// your currency, do not offer the conversion again in this checkout.
fun chooseCurrency(offer: CurrencyConversionOffer, payInCardCurrency: Boolean): CurrencyConversionChoice =
    CurrencyConversionChoice(dccSessionId = offer.dccSessionId, acceptedByUser = payInCardCurrency)
```

When both surcharging and currency conversion apply, calculate the surcharge first and request the
currency conversion quote for the surcharge's `totalAmount` (net amount plus surcharge), as the Client
API reference prescribes. Without a surcharge, request it for the order amount. The payment context
keeps the net amount in both cases.

## 8. Encrypt and send to your backend

`encryptPaymentRequest` validates the request again and throws an `InvalidArgumentException` if it is
not valid. The result has two parts: `encryptedCustomerInput` and `encodedClientMetaInfo`. Send both
to your backend; it passes them to Create Payment.

<!-- region: encrypt-payment-request -->
```kotlin
sealed interface PaymentSubmission {
    // Show these next to the fields. Nothing was sent.
    class InputInvalid(val errorsByField: Map<String, List<String>>) : PaymentSubmission

    class Submitted(val result: MerchantPaymentResponse) : PaymentSubmission
}

suspend fun payWithEncryptedRequest(
    sdk: OnlinePaymentsSdk,
    paymentRequest: PaymentRequest,
    orderId: String,
    currencyConversion: CurrencyConversionChoice?, // the customer's choice, when a conversion offer was shown
): PaymentSubmission {
    val errorsByField = validatePaymentRequest(paymentRequest)
    if (errorsByField.isNotEmpty()) {
        return PaymentSubmission.InputInvalid(errorsByField) // do not encrypt
    }

    // The only way card data leaves the device: encrypted by the SDK. It validates the request
    // again and throws an InvalidArgumentException when it is not valid.
    // Never send, log or store the raw field values.
    val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)

    // Your backend passes encryptedCustomerInput to Create Payment and decides the outcome.
    val result = submitEncryptedPayment(orderId, encryptedRequest, currencyConversion)
    return PaymentSubmission.Submitted(result)
}
```

## 9. Session expired or other API errors

When an SDK call fails with a `ResponseException`, the session is no longer usable. Get new session
data, create a new `OnlinePaymentsSdk`, load the payment product again and create a new
`PaymentRequest`. See [errors.md](errors.md). Whether the customer's input is kept after a new session
is your decision; this skill does not prescribe it.

## 10. Handle the result

Your backend returns what Create Payment returned. When the payment needs a redirect (for example
3-D Secure), the backend returns the redirect URL from that response. The app opens it in the
browser and records the order id, so it can ask for the status when the customer comes back.

<!-- region: pending-payment-store -->
```kotlin
// Remembers the order that waits for its result while the customer is on the 3-D Secure page.
// Persist it, because the system can stop the app meanwhile. Store only the order id: never card
// data or session data.
interface PendingPaymentStore {
    fun readOrderId(): String?
    fun saveOrderId(orderId: String)
    fun clear()
}

// commit() writes before it returns (apply() writes later): the app can be stopped as soon as the
// browser opens.
@SuppressLint("ApplySharedPref")
class SharedPreferencesPendingPaymentStore(context: Context) : PendingPaymentStore {
    private val preferences =
        context.applicationContext.getSharedPreferences("pending_payment", Context.MODE_PRIVATE)

    override fun readOrderId(): String? = preferences.getString("orderId", null)

    override fun saveOrderId(orderId: String) {
        preferences.edit().putString("orderId", orderId).commit()
    }

    override fun clear() {
        preferences.edit().remove("orderId").commit()
    }
}
```

<!-- region: handle-payment-result -->
```kotlin
fun handlePaymentResult(
    activity: Activity,
    result: MerchantPaymentResponse,
    orderId: String,
    pendingPayments: PendingPaymentStore,
) {
    val redirectUrl = result.redirectUrl
    if (redirectUrl != null) {
        // For example 3-D Secure or a third-party payment page. Open the URL exactly as received:
        // do not build or change it. Your backend sets the payment's returnUrl to a deep link of
        // this app, so the customer comes back to the app afterwards.
        pendingPayments.saveOrderId(orderId)
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl)))
        } catch (_: ActivityNotFoundException) {
            pendingPayments.clear()
            showStatusUnknown() // the payment exists; let the customer check the order later
        }
        return
    }

    // The status comes from your backend. The app never decides that a payment succeeded.
    showPaymentOutcome(result.status)
}
```

After the redirect, the customer comes back through your return URL (a deep link, see
[android.md](android.md#deep-link-for-the-return-from-3-d-secure)). Handle it in the root Activity of
the app, not in the checkout screen, and keep the order id in persistent storage, not only in memory.
The same code covers both cases: the app is running (`onNewIntent`), and the system stopped the app
while the customer was on the 3-D Secure page, so the link starts it (the launch intent in
`onCreate`). Act only when a payment waits for its result.

<!-- region: handle-return-deep-link -->
```kotlin
// The start of the returnUrl your backend sets in Create Payment. Your manifest has an intent filter
// for it (see references/android.md).
const val RETURN_URL_PREFIX = "myshop://payment-return"

// Runs when the customer comes back. Acts only when a payment waits for its result: the same link
// can be delivered again, for example after the Activity is recreated.
suspend fun onPaymentReturn(pendingPayments: PendingPaymentStore) {
    val orderId = pendingPayments.readOrderId() ?: return
    pendingPayments.clear()

    // Do not read the outcome from the link: ask your backend for the status.
    try {
        showPaymentOutcome(fetchPaymentStatus(orderId).status)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        showStatusUnknown() // the outcome is unknown, not failed: let the customer check the order later
    }
}

// Handle the link in the Activity that is the root of the app (the one with the launcher intent
// filter and android:launchMode="singleTask" or "singleTop"), not in the checkout screen: the
// customer can come back while another screen is shown.
class MainActivity : ComponentActivity() {
    private val pendingPayments by lazy { SharedPreferencesPendingPaymentStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the system stopped the app while the customer was on the 3-D Secure page, the deep link
        // starts the app again: the launch intent holds it. onPaymentReturn ignores an old link
        // because no payment waits any more.
        handleReturnLink(intent)
    }

    // The app is running: the link arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleReturnLink(intent)
    }

    private fun handleReturnLink(intent: Intent?) {
        val link = intent?.data?.toString() ?: return
        if (link.startsWith(RETURN_URL_PREFIX)) {
            lifecycleScope.launch { onPaymentReturn(pendingPayments) }
        }
    }
}
```

## Tokenization without a payment

To store a card without paying, use `CreditCardTokenRequest`. It applies no validation of its own,
so validate the values with the card product's fields first. Load the card product with
`getIinDetails` and `getPaymentProduct` as in a payment. There is no order amount, so create the
payment context with an amount of 1, the smallest unit of the currency (for example 1 cent for
EUR). Do not use 0: the Client API answers an amount of 0 with an error.

<!-- region: encrypt-token-request -->
```kotlin
// There is no order amount when a card is stored without a payment: use an amount of 1, the
// smallest unit of the currency (1 cent for EUR). Do not use 0: the Client API answers with an error.
// Use this context for getIinDetails and getPaymentProduct to load the card product.
fun createTokenizationContext(currencyCode: String, countryCode: String): PaymentContextWithAmount =
    PaymentContextWithAmount(AmountOfMoneyWithAmount(1, currencyCode), countryCode, false)

private val cardFieldIds = listOf("cardNumber", "cardholderName", "expiryDate", "cvv")

sealed interface StoreCardResult {
    // Show these next to the fields. Nothing was sent.
    class Invalid(val errorsByField: Map<String, List<String>>) : StoreCardResult

    class Stored(val token: String) : StoreCardResult
}

suspend fun storeCard(
    sdk: OnlinePaymentsSdk,
    cardProduct: PaymentProduct, // from sdk.getPaymentProduct for the detected card product
    form: Map<String, String>, // field id -> the value as shown in the input, with or without mask
): StoreCardResult {
    // Remove the masks first ("12/26" becomes "1226"): the validation rules and the token request
    // expect values without formatting.
    val values = cardFieldIds.associateWith { fieldId ->
        val value = form[fieldId].orEmpty()
        cardProduct.getField(fieldId)?.removeMask(value) ?: value
    }

    // CreditCardTokenRequest applies no validation, so validate with the product's fields first.
    val errorsByField = cardFieldIds.mapNotNull { fieldId ->
        val errors = cardProduct.getField(fieldId)?.validate(values[fieldId]).orEmpty()
        if (errors.isEmpty()) null else fieldId to errors.map { it.errorMessage }
    }.toMap()
    if (errorsByField.isNotEmpty()) {
        return StoreCardResult.Invalid(errorsByField)
    }

    val tokenRequest = CreditCardTokenRequest(
        cardNumber = values["cardNumber"],
        cardholderName = values["cardholderName"],
        expiryDate = values["expiryDate"],
        securityCode = values["cvv"],
        // Required: encryption fails with an EncryptionException without it.
        paymentProductId = cardProduct.id,
    )
    val encryptedRequest = sdk.encryptTokenRequest(tokenRequest)

    // Your backend creates the token with the encrypted data.
    return StoreCardResult.Stored(submitEncryptedToken(encryptedRequest))
}
```

## Google Pay

The SDK has no Google Pay payment flow: it does not show the payment sheet. It only:

- lists Google Pay (product 320) in `getBasicPaymentProducts` when `IsReadyToPay` says the device can
  pay with it (it uses `SdkConfiguration.environmentIsProduction` to choose the Google TEST or
  PRODUCTION environment: set it to `true` in release builds);
- returns the gateway and the card networks in `paymentProduct320SpecificData` for the payment sheet
  request.

Everything else is your code, with the Google Pay API (`com.google.android.gms:play-services-wallet`,
which the SDK does not expose: add it to your app). Follow Google's brand guidelines for the Google
Pay button. A Google Pay payment can also return a redirect URL (for example 3-D Secure for a card
without a network token): handle it exactly as in [10. Handle the result](#10-handle-the-result).

<!-- region: google-pay-configuration -->
```kotlin
// The SDK has no Google Pay payment flow: it does not show the payment sheet. It lists Google Pay
// (product 320) only when the device can pay with it, and returns the gateway and card networks
// for the payment sheet request.
class GooglePayConfiguration(val gateway: String, val networks: List<String>)

fun googlePayConfiguration(product: BasicPaymentProduct): GooglePayConfiguration? {
    val data = product.paymentProduct320SpecificData ?: return null
    val gateway = data.gateway ?: return null
    return GooglePayConfiguration(gateway, data.networks.orEmpty().filterNotNull())
}
```

<!-- region: google-pay-request -->
```kotlin
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
```

<!-- region: google-pay-launcher -->
```kotlin
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
```

The token goes into a `PaymentRequest` for product 320. Encrypt it like a card payment and send the
result to your backend. `gatewayMerchantId` is your merchant id (PSPID); the Worldline Direct
documentation for Google Pay describes the merchant identifiers and the backend call.

<!-- region: google-pay-payment-request -->
```kotlin
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
```
