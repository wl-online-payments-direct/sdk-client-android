package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// [START iin-details]
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
// [END iin-details]
