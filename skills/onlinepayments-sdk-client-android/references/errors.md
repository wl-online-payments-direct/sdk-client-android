# Error handling

The `suspend` and `...Sync` methods throw, and the listener variants pass to `onFailure`, a subclass
of `SdkException` (package `com.onlinepayments.sdk.client.android.domain.exceptions`). Each has a
`message`, an optional `cause`, a `code` (`SdkExceptionType`) and `metadata`. The listener variants
wrap any other exception in a plain `SdkException`.

| Exception | When | What to do |
|---|---|---|
| `InvalidArgumentException` | Invalid input: `encryptPaymentRequest` with a request that does not validate (`metadata["data"]` is the `ValidationResult`), `setValue` on a field that is not writable for the account on file, `getField` with an id the product does not have | Show validation errors; fix the calling code for the other cases |
| `CommunicationException` | No response, a network error, or a response that is not JSON. `cause` holds the underlying exception | Let the customer retry |
| `ResponseException` | The Client API returned an error response. `httpStatusCode` is set; `apiError` has `errorId` and `errors` (each with `errorCode`). Also thrown with `httpStatusCode` 404 when no payment product is available, or for a product the SDK cannot process | Get new session data, create a new `OnlinePaymentsSdk` and restart the checkout |
| `EncryptionException` | Encryption failed, or `encryptTokenRequest` without a payment product id | Restart the checkout |
| `ConfigurationException` | `OnlinePaymentsSdk(...)` with a blank session value or a `clientApiUrl` that is not a valid URL | Fix the integration |
| `IllegalStateSdkException` | `getIinDetails` while another IIN lookup is running | Wait for the first lookup, then look up the latest input (see `CardDetector`) |

`getIinDetails` needs different handling for two cases, because it does not throw for them: below 6
digits it returns the status `NOT_ENOUGH_DIGITS`, and for a card number that matches no payment
product of your account (the API answers HTTP 404) it returns `UNKNOWN`. Do not block the form on
`UNKNOWN`. Every other API error, for example an expired session, is thrown as a `ResponseException`
(see [flows.md](flows.md#5-detect-the-card-brand)).

A card that cannot be converted is not an error: `getCurrencyConversionQuote` returns a result other
than `ConversionResultType.ALLOWED` (see [flows.md](flows.md#currency-conversion)).

The facade turns `paymentProductId` of `getSurchargeCalculation` and `getCurrencyConversionQuote`
into an `Int`. Pass the digits of a payment product id (for example `"1"`), not a name: a text that
is not a number fails with a `NumberFormatException` from the `suspend` variant.

<!-- region: handle-sdk-error -->
```kotlin
enum class ErrorAction {
    SHOW_VALIDATION_ERRORS, // the customer can fix the input
    RETRY, // a network problem, or a call that overlapped another: the same call can be tried again
    RESTART_CHECKOUT, // get new session data from your backend and create a new OnlinePaymentsSdk
    INTEGRATION_ERROR, // a bug in the integration: fix the code
}

// Call from the catch block around an SDK call. A CancellationException is rethrown: it is not an error.
fun classifySdkError(error: Throwable): ErrorAction {
    if (error is CancellationException) {
        throw error
    }
    return when (error) {
        // Invalid input: encryptPaymentRequest with a request that does not validate (metadata["data"]
        // is the ValidationResult), setValue on a READ_ONLY field, getField with an unknown id.
        is InvalidArgumentException -> ErrorAction.SHOW_VALIDATION_ERRORS

        // No response, a network error, or a response that is not JSON. error.cause has details.
        is CommunicationException -> ErrorAction.RETRY

        // getIinDetails while another IIN lookup is running.
        is IllegalStateSdkException -> ErrorAction.RETRY

        // The Client API returned an error, for example because the session is no longer valid, or
        // httpStatusCode 404 when no payment product is available.
        is ResponseException -> {
            // Log only the status code, error id and error codes: never payment data.
            Log.w(
                "Checkout",
                "Client API error ${error.httpStatusCode} ${error.apiError.errorId} " +
                    error.apiError.errors?.joinToString(",") { it.errorCode.orEmpty() },
            )
            ErrorAction.RESTART_CHECKOUT
        }

        // Encryption failed, or encryptTokenRequest without a payment product id.
        is EncryptionException -> ErrorAction.RESTART_CHECKOUT

        // A blank session value or an invalid clientApiUrl when the SDK is created.
        is ConfigurationException -> ErrorAction.INTEGRATION_ERROR

        else -> ErrorAction.INTEGRATION_ERROR
    }
}
```

Rules:

- Log only `errorId`, error codes and HTTP status codes. Never log `PaymentRequest` values, form
  values, the `metadata` of an `InvalidArgumentException` or the `cause` of a `CommunicationException`.
  This includes crash reporting tools and loggers that capture Logcat output or uncaught exceptions.
- In a coroutine, rethrow `CancellationException`: do not catch it as an error.
- A failed SDK call never means a payment failed or succeeded. The payment result comes only from
  your backend.
