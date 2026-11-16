package com.example.checkoutjava;

import android.content.Context;

import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount;
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount;
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration;
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;

public final class Initialisation {
    private Initialisation() {
    }

    // [START initialise-sdk-java]
    public static final class CheckoutSession {
        public final OnlinePaymentsSdk sdk;
        public final String assetUrl; // from the session data: needed to build logo URLs

        CheckoutSession(OnlinePaymentsSdk sdk, String assetUrl) {
            this.sdk = sdk;
            this.assetUrl = assetUrl;
        }
    }

    // Call once per checkout. Your backend creates new session data with Create Session; it holds no
    // secret. Never put the API key or secret in the app.
    public static void createCheckoutSession(
            Context context, boolean isProduction, MerchantBackend.Callback<CheckoutSession> callback) {
        MerchantBackend.fetchSessionData(new MerchantBackend.Callback<SessionData>() {
            @Override
            public void onSuccess(SessionData sessionData) {
                // Throws a ConfigurationException when a session value is blank or clientApiUrl is not
                // a valid URL.
                OnlinePaymentsSdk sdk = new OnlinePaymentsSdk(
                        sessionData,
                        // The SDK keeps the context: pass the application context, never an Activity.
                        context.getApplicationContext(),
                        // From Java, pass all four values: environmentIsProduction (true in release
                        // builds: it selects the Google Pay PRODUCTION environment), your app's name and
                        // version, sdkIdentifier (null) and loggingEnabled, which must be false in production.
                        new SdkConfiguration(isProduction, "MyShopApp/1.0.0", null, false));
                callback.onSuccess(new CheckoutSession(sdk, sessionData.getAssetUrl()));
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }
    // [END initialise-sdk-java]

    // [START payment-context-java]
    // The payment context is passed to most SDK calls. Use the real values of the order being paid: the
    // payment products, surcharge and currency conversion depend on them. A PaymentContextWithAmount is
    // a PaymentContext that also works for getIinDetails.
    public static PaymentContextWithAmount createPaymentContext(
            long totalInMinorUnits, // smallest unit of the currency: 1298 = EUR 12.98
            String currencyCode, // ISO 4217, for example "EUR"
            String countryCode) { // ISO 3166-1 alpha-2, for example "NL"
        return new PaymentContextWithAmount(
                new AmountOfMoneyWithAmount(totalInMinorUnits, currencyCode),
                countryCode,
                false); // isRecurring
    }
    // [END payment-context-java]
}
