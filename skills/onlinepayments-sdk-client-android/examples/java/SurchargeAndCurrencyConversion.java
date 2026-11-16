package com.example.checkoutjava;

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.ConversionResultType;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.DccProposal;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.Surcharge;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeResult;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.CurrencyConversionResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.SurchargeCalculationResponseListener;

import java.util.List;

public final class SurchargeAndCurrencyConversion {
    private SurchargeAndCurrencyConversion() {
    }

    // [START card-source-java]
    // The card for a surcharge or currency conversion request. These requests are not encrypted: send
    // only the first 8 digits of a card number being entered (at least 6) with the product id from the
    // IIN details, never the complete number. The SDK sends what you pass. For a stored card, send the
    // account on file id (the token) instead.
    public static final class QuoteCard {
        final String firstDigits; // null for a stored card
        final int paymentProductId;
        final String accountOnFileId; // null for a card being entered

        private QuoteCard(String firstDigits, int paymentProductId, String accountOnFileId) {
            this.firstDigits = firstDigits;
            this.paymentProductId = paymentProductId;
            this.accountOnFileId = accountOnFileId;
        }

        public static QuoteCard entered(String cardNumberDigits, int paymentProductId) {
            String firstDigits = cardNumberDigits.length() > 8 ? cardNumberDigits.substring(0, 8) : cardNumberDigits;
            return new QuoteCard(firstDigits, paymentProductId, null);
        }

        public static QuoteCard stored(String accountOnFileId) {
            return new QuoteCard(null, 0, accountOnFileId);
        }
    }

    public interface OfferListener<T> {
        void onOffer(T offer); // offer is null when there is nothing to offer

        void onError(SdkException error); // see error handling
    }
    // [END card-source-java]

    // [START surcharge-calculation-java]
    public static final class SurchargeOffer {
        public final AmountOfMoney surchargeAmount;
        public final AmountOfMoney totalAmount;

        SurchargeOffer(AmountOfMoney surchargeAmount, AmountOfMoney totalAmount) {
            this.surchargeAmount = surchargeAmount;
            this.totalAmount = totalAmount;
        }
    }

    // Call once the card is known. onOffer receives null when no surcharge applies.
    // netAmount is the order amount from the payment context: it does not include the surcharge. The
    // platform adds the surcharge when your backend creates the payment with
    // order.surchargeSpecificInput.mode set to "on-behalf-of".
    public static void getSurcharge(
            OnlinePaymentsSdk sdk, AmountOfMoney netAmount, QuoteCard card, OfferListener<SurchargeOffer> listener) {
        SurchargeCalculationResponseListener responseListener = new SurchargeCalculationResponseListener() {
            @Override
            public void onSuccess(SurchargeCalculationResponse response) {
                List<Surcharge> surcharges = response.getSurcharges();
                if (surcharges == null || surcharges.isEmpty()) {
                    listener.onOffer(null);
                    return;
                }
                Surcharge surcharge = surcharges.get(0);

                // Check the result before reading the amounts: the API leaves them out when no
                // surcharge applies, and they are then null.
                if (surcharge.getResult() != SurchargeResult.OK) {
                    listener.onOffer(null);
                    return;
                }

                // Show both amounts to the customer before they pay.
                listener.onOffer(new SurchargeOffer(surcharge.getSurchargeAmount(), surcharge.getTotalAmount()));
            }

            @Override
            public void onFailure(SdkException exception) {
                listener.onError(exception);
            }
        };

        if (card.accountOnFileId != null) {
            sdk.getSurchargeCalculation(netAmount, card.accountOnFileId, responseListener);
        } else {
            sdk.getSurchargeCalculation(
                    netAmount, card.firstDigits, String.valueOf(card.paymentProductId), responseListener);
        }
    }
    // [END surcharge-calculation-java]

    // [START currency-conversion-java]
    public static final class CurrencyConversionOffer {
        public final String dccSessionId;
        public final DccProposal proposal;

        CurrencyConversionOffer(String dccSessionId, DccProposal proposal) {
            this.dccSessionId = dccSessionId;
            this.proposal = proposal;
        }
    }

    // Call once the card is known. onOffer receives null when there is no offer: then pay in your
    // currency and send no currency conversion data.
    // amountToPay is the order amount, or the surcharge's totalAmount when a surcharge applies.
    public static void getCurrencyConversionOffer(
            OnlinePaymentsSdk sdk,
            AmountOfMoney amountToPay,
            QuoteCard card,
            OfferListener<CurrencyConversionOffer> listener) {
        CurrencyConversionResponseListener responseListener = new CurrencyConversionResponseListener() {
            @Override
            public void onSuccess(CurrencyConversionResponse response) {
                // INVALID_CARD, INVALID_MERCHANT, NO_RATE and NOT_AVAILABLE mean: no offer.
                // dccSessionId is then empty and the response has no proposal (it is null): check
                // the result before reading response.getProposal().
                if (response.getResult().getResult() != ConversionResultType.ALLOWED) {
                    listener.onOffer(null);
                    return;
                }

                // Show two options with the same size, colour and font: proposal.getBaseAmount()
                // (your currency) and proposal.getTargetAmount() (the card's currency), with
                // proposal.getRate().getExchangeRate(), proposal.getRate().getMarkUpRate() (a
                // percentage: 3.12 means 3.12 %) and proposal.getDisclaimerDisplay(). Preselect neither
                // option and do not ask a yes/no question: the customer must actively choose.
                listener.onOffer(new CurrencyConversionOffer(response.getDccSessionId(), response.getProposal()));
            }

            @Override
            public void onFailure(SdkException exception) {
                listener.onError(exception);
            }
        };

        if (card.accountOnFileId != null) {
            sdk.getCurrencyConversionQuote(amountToPay, card.accountOnFileId, responseListener);
        } else {
            sdk.getCurrencyConversionQuote(
                    amountToPay, card.firstDigits, String.valueOf(card.paymentProductId), responseListener);
        }
    }
    // [END currency-conversion-java]

    // [START currency-conversion-choice-java]
    // Call when the customer actively selects one of the two options. Pass the result to
    // MerchantBackend.submitEncryptedPayment, also when the customer chose your currency. Once the
    // customer chose your currency, do not offer the conversion again in this checkout.
    public static MerchantBackend.CurrencyConversionChoice chooseCurrency(
            CurrencyConversionOffer offer, boolean payInCardCurrency) {
        return new MerchantBackend.CurrencyConversionChoice(offer.dccSessionId, payInCardCurrency);
    }
    // [END currency-conversion-choice-java]
}
