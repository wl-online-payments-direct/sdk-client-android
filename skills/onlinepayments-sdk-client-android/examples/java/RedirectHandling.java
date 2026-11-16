package com.example.checkoutjava;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.ComponentActivity;

public final class RedirectHandling {
    private RedirectHandling() {
    }

    // [START pending-payment-store-java]
    // Remembers the order that waits for its result while the customer is on the 3-D Secure page.
    // Persist it, because the system can stop the app meanwhile. Store only the order id: never card
    // data or session data.
    public interface PendingPaymentStore {
        String readOrderId(); // null when no payment waits

        void saveOrderId(String orderId);

        void clear();
    }

    // commit() writes before it returns (apply() writes later): the app can be stopped as soon as the
    // browser opens.
    @SuppressLint("ApplySharedPref")
    public static final class SharedPreferencesPendingPaymentStore implements PendingPaymentStore {
        private final SharedPreferences preferences;

        public SharedPreferencesPendingPaymentStore(Context context) {
            preferences = context.getApplicationContext().getSharedPreferences("pending_payment", Context.MODE_PRIVATE);
        }

        @Override
        public String readOrderId() {
            return preferences.getString("orderId", null);
        }

        @Override
        public void saveOrderId(String orderId) {
            preferences.edit().putString("orderId", orderId).commit();
        }

        @Override
        public void clear() {
            preferences.edit().remove("orderId").commit();
        }
    }
    // [END pending-payment-store-java]

    // [START handle-payment-result-java]
    public static void handlePaymentResult(
            Activity activity,
            MerchantBackend.MerchantPaymentResponse result,
            String orderId,
            PendingPaymentStore pendingPayments) {
        if (result.redirectUrl != null) {
            // For example 3-D Secure or a third-party payment page. Open the URL exactly as received:
            // do not build or change it. Your backend sets the payment's returnUrl to a deep link of
            // this app, so the customer comes back to the app afterwards.
            pendingPayments.saveOrderId(orderId);
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result.redirectUrl)));
            } catch (ActivityNotFoundException e) {
                pendingPayments.clear();
                UiPlaceholders.showStatusUnknown(); // the payment exists; let the customer check the order later
            }
            return;
        }

        // The status comes from your backend. The app never decides that a payment succeeded.
        UiPlaceholders.showPaymentOutcome(result.status);
    }
    // [END handle-payment-result-java]

    // [START handle-return-deep-link-java]
    // The start of the returnUrl your backend sets in Create Payment. Your manifest has an intent filter
    // for it (see references/android.md).
    public static final String RETURN_URL_PREFIX = "myshop://payment-return";

    // Runs when the customer comes back. Acts only when a payment waits for its result: the same link
    // can be delivered again, for example after the Activity is recreated.
    public static void onPaymentReturn(PendingPaymentStore pendingPayments) {
        String orderId = pendingPayments.readOrderId();
        if (orderId == null) {
            return;
        }
        pendingPayments.clear();

        // Do not read the outcome from the link: ask your backend for the status.
        MerchantBackend.fetchPaymentStatus(orderId, new MerchantBackend.Callback<MerchantBackend.MerchantPaymentResponse>() {
            @Override
            public void onSuccess(MerchantBackend.MerchantPaymentResponse result) {
                UiPlaceholders.showPaymentOutcome(result.status);
            }

            @Override
            public void onError(Exception error) {
                // The outcome is unknown, not failed: let the customer check the order later.
                UiPlaceholders.showStatusUnknown();
            }
        });
    }

    // Handle the link in the Activity that is the root of the app (the one with the launcher intent
    // filter and android:launchMode="singleTask" or "singleTop"), not in the checkout screen: the
    // customer can come back while another screen is shown.
    public static class MainActivity extends ComponentActivity {
        private PendingPaymentStore pendingPayments;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pendingPayments = new SharedPreferencesPendingPaymentStore(this);
            // If the system stopped the app while the customer was on the 3-D Secure page, the deep link
            // starts the app again: the launch intent holds it. onPaymentReturn ignores an old link
            // because no payment waits any more.
            handleReturnLink(getIntent());
        }

        // The app is running: the link arrives here.
        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            setIntent(intent);
            handleReturnLink(intent);
        }

        private void handleReturnLink(Intent intent) {
            Uri link = intent == null ? null : intent.getData();
            if (link != null && link.toString().startsWith(RETURN_URL_PREFIX)) {
                onPaymentReturn(pendingPayments);
            }
        }
    }
    // [END handle-return-deep-link-java]
}
