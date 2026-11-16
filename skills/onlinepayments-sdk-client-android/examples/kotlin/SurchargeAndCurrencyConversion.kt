package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.currencyConversion.ConversionResultType
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.currencyConversion.DccProposal
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeResult
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START card-source]
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
// [END card-source]

// [START surcharge-calculation]
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
// [END surcharge-calculation]

// [START currency-conversion]
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
// [END currency-conversion]

// [START currency-conversion-choice]
// Call when the customer actively selects one of the two options. Pass the result to
// submitEncryptedPayment, also when the customer chose your currency. Once the customer chose
// your currency, do not offer the conversion again in this checkout.
fun chooseCurrency(offer: CurrencyConversionOffer, payInCardCurrency: Boolean): CurrencyConversionChoice =
    CurrencyConversionChoice(dccSessionId = offer.dccSessionId, acceptedByUser = payInCardCurrency)
// [END currency-conversion-choice]
