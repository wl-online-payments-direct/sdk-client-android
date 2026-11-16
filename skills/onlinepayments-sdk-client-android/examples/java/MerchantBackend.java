package com.example.checkoutjava;

import android.os.Handler;
import android.os.Looper;

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// [START merchant-backend-java]
// The URL, paths and JSON shapes are placeholders for your own backend; the SDK never calls them.
public final class MerchantBackend {
    private static final String BACKEND_URL = "https://shop.example.com";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** The payment status as your backend reports it. */
    public enum PaymentStatus { SUCCESS, PENDING, FAILED }

    /** What your backend returns after it created the payment, or when the app asks for the status. */
    public static final class MerchantPaymentResponse {
        public final PaymentStatus status;
        /** Not null when the customer must be redirected, for example for 3-D Secure. */
        public final String redirectUrl;

        MerchantPaymentResponse(PaymentStatus status, String redirectUrl) {
            this.status = status;
            this.redirectUrl = redirectUrl;
        }
    }

    /**
     * The customer's currency conversion choice. Your backend passes both fields to Create Payment in
     * cardPaymentMethodSpecificInput.currencyConversion. Both are mandatory once a quote was shown.
     */
    public static final class CurrencyConversionChoice {
        public final String dccSessionId;
        /** true: pay in the card's currency; false: pay in your currency. */
        public final boolean acceptedByUser;

        public CurrencyConversionChoice(String dccSessionId, boolean acceptedByUser) {
            this.dccSessionId = dccSessionId;
            this.acceptedByUser = acceptedByUser;
        }
    }

    /** The result is delivered on the main thread. */
    public interface Callback<T> {
        void onSuccess(T result);

        void onError(Exception error);
    }

    // Never log the body you send: it holds the encrypted customer input.
    private static JSONObject postJson(String path, JSONObject body) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BACKEND_URL + path).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            if (code < 200 || code > 299) {
                throw new IOException(path + " failed with HTTP " + code);
            }
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    bytes.write(buffer, 0, read);
                }
                return new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private static <T> void run(Callable<T> work, Callback<T> callback) {
        EXECUTOR.execute(() -> {
            try {
                T result = work.call();
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception error) {
                MAIN.post(() -> callback.onError(error));
            }
        });
    }

    private static MerchantPaymentResponse toPaymentResponse(JSONObject json) throws JSONException {
        return new MerchantPaymentResponse(
                PaymentStatus.valueOf(json.getString("status").toUpperCase(Locale.ROOT)),
                json.isNull("redirectUrl") ? null : json.getString("redirectUrl"));
    }

    /** Your backend creates a new client session for this checkout (Create Session) and returns its data. */
    public static void fetchSessionData(Callback<SessionData> callback) {
        run(() -> {
            JSONObject json = postJson("/api/checkout/session", new JSONObject());
            return new SessionData(
                    json.getString("clientSessionId"),
                    json.getString("customerId"),
                    json.getString("clientApiUrl"),
                    json.getString("assetUrl"));
        }, callback);
    }

    /** Your backend creates the payment (Create Payment) with the encrypted customer input. */
    public static void submitEncryptedPayment(
            String orderId,
            EncryptedRequest encryptedRequest,
            CurrencyConversionChoice currencyConversion, // null unless a currency conversion offer was shown
            Callback<MerchantPaymentResponse> callback) {
        run(() -> {
            JSONObject body = new JSONObject()
                    .put("orderId", orderId)
                    .put("encryptedCustomerInput", encryptedRequest.getEncryptedCustomerInput())
                    .put("encodedClientMetaInfo", encryptedRequest.getEncodedClientMetaInfo());
            if (currencyConversion != null) {
                body.put("currencyConversion", new JSONObject()
                        .put("dccSessionId", currencyConversion.dccSessionId)
                        .put("acceptedByUser", currencyConversion.acceptedByUser));
            }
            return toPaymentResponse(postJson("/api/checkout/payment", body));
        }, callback);
    }

    /** Your backend reads the payment status, for example after the customer returned from 3-D Secure. */
    public static void fetchPaymentStatus(String orderId, Callback<MerchantPaymentResponse> callback) {
        run(() -> toPaymentResponse(postJson("/api/checkout/status", new JSONObject().put("orderId", orderId))),
                callback);
    }

    /** Your backend creates a payment token (Create Token) from the encrypted card data. Returns the token id. */
    public static void submitEncryptedToken(EncryptedRequest encryptedRequest, Callback<String> callback) {
        run(() -> {
            JSONObject body = new JSONObject()
                    .put("encryptedCustomerInput", encryptedRequest.getEncryptedCustomerInput())
                    .put("encodedClientMetaInfo", encryptedRequest.getEncodedClientMetaInfo());
            return postJson("/api/checkout/token", body).getString("token");
        }, callback);
    }
}
// [END merchant-backend-java]
