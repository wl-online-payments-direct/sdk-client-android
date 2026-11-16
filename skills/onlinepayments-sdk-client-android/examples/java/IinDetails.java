package com.example.checkoutjava;

import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetail;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.IinLookupResponseListener;

import java.util.ArrayList;
import java.util.List;

public final class IinDetails {
    private IinDetails() {
    }

    // [START iin-details-java]
    public static final class CardDetection {
        public enum Kind {
            NUMBER_TOO_SHORT,
            SUPPORTED,
            NOT_ALLOWED, // a known card, but not accepted for this payment
            UNKNOWN // see the comment in CardDetector
        }

        public final Kind kind;
        public final Integer paymentProductId; // only for SUPPORTED
        public final List<Integer> coBrandProductIds; // co-branded card: let the customer choose the brand

        CardDetection(Kind kind, Integer paymentProductId, List<Integer> coBrandProductIds) {
            this.kind = kind;
            this.paymentProductId = paymentProductId;
            this.coBrandProductIds = coBrandProductIds;
        }
    }

    // Create one CardDetector per checkout and call detect() while the customer types the card
    // number. Call it on the main thread: the callbacks of the SDK run there.
    public static final class CardDetector {
        public interface Listener {
            void onDetected(CardDetection detection);

            void onError(SdkException error);
        }

        private final OnlinePaymentsSdk sdk;
        private final PaymentContextWithAmount paymentContext; // getIinDetails needs the amount
        private final Listener listener;
        private boolean lookupRunning;
        private String queuedDigits;

        public CardDetector(OnlinePaymentsSdk sdk, PaymentContextWithAmount paymentContext, Listener listener) {
            this.sdk = sdk;
            this.paymentContext = paymentContext;
            this.listener = listener;
        }

        public void detect(String cardNumberInput) {
            String digits = cardNumberInput.replaceAll("\\D", "");
            if (digits.length() < 6) {
                // The SDK would answer NOT_ENOUGH_DIGITS without a request.
                listener.onDetected(new CardDetection(CardDetection.Kind.NUMBER_TOO_SHORT, null, null));
                return;
            }
            // The SDK allows one IIN lookup at a time: a second call while one is running fails with
            // an IllegalStateSdkException. Keep the latest input and look it up afterwards.
            if (lookupRunning) {
                queuedDigits = digits;
                return;
            }
            lookup(digits);
        }

        private void lookup(String digits) {
            lookupRunning = true;
            // The SDK sends only the first 8 digits.
            sdk.getIinDetails(digits, new IinLookupResponseListener() {
                @Override
                public void onSuccess(IinDetailsResponse response) {
                    lookupRunning = false;
                    listener.onDetected(toDetection(response));
                    runQueuedLookup();
                }

                @Override
                public void onFailure(SdkException exception) {
                    lookupRunning = false;
                    // A ResponseException (for example an expired session) ends up here: see error handling.
                    listener.onError(exception);
                    runQueuedLookup();
                }
            }, paymentContext);
        }

        private void runQueuedLookup() {
            if (queuedDigits != null) {
                String digits = queuedDigits;
                queuedDigits = null;
                lookup(digits);
            }
        }

        private static CardDetection toDetection(IinDetailsResponse response) {
            IinDetailStatus status = response.getStatus();
            if (status == IinDetailStatus.SUPPORTED && response.getPaymentProductId() != null) {
                List<Integer> coBrandProductIds = new ArrayList<>();
                if (response.getCoBrands() != null) {
                    for (IinDetail coBrand : response.getCoBrands()) {
                        if (coBrand != null
                                && Boolean.TRUE.equals(coBrand.isAllowedInContext())
                                && coBrand.getPaymentProductId() != null) {
                            coBrandProductIds.add(Integer.valueOf(coBrand.getPaymentProductId()));
                        }
                    }
                }
                return new CardDetection(
                        CardDetection.Kind.SUPPORTED,
                        Integer.valueOf(response.getPaymentProductId()),
                        coBrandProductIds);
            }
            if (status == IinDetailStatus.EXISTING_BUT_NOT_ALLOWED) {
                return new CardDetection(CardDetection.Kind.NOT_ALLOWED, null, null);
            }
            if (status == IinDetailStatus.NOT_ENOUGH_DIGITS) {
                return new CardDetection(CardDetection.Kind.NUMBER_TOO_SHORT, null, null);
            }
            // UNKNOWN: the card number matches no payment product of your account (the API answered
            // HTTP 404). Do not block the form. UNSUPPORTED is declared but never returned.
            return new CardDetection(CardDetection.Kind.UNKNOWN, null, null);
        }
    }
    // [END iin-details-java]
}
