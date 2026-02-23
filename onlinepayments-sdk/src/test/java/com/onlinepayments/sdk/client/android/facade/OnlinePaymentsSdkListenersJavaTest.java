/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import com.google.gson.JsonElement;
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney;
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount;
import com.onlinepayments.sdk.client.android.domain.Constants;
import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount;
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration;
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.ConversionResultType;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse;
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse;
import com.onlinepayments.sdk.client.android.facade.listeners.BasicPaymentProductsResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.CurrencyConversionResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.IinLookupResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductNetworkResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentRequestPreparedListener;
import com.onlinepayments.sdk.client.android.facade.listeners.PublicKeyResponseListener;
import com.onlinepayments.sdk.client.android.facade.listeners.SurchargeCalculationResponseListener;
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider;
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest({Base64.class})
public class OnlinePaymentsSdkListenersJavaTest {

    private static MockWebServer mockWebServer;
    private static final Context appContext = ApplicationProvider.getApplicationContext();
    private final Logger mockLogger = mock(Logger.class);

    private final CoroutineDispatcher testDispatcher = Dispatchers.getUnconfined();

    @Before
    public void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        LoggerProvider.INSTANCE.setLogger(mockLogger);

        OnlinePaymentsSdk.Companion.setMainDispatcher(testDispatcher);
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        LoggerProvider.INSTANCE.reset();
    }

    @Test
    public void testGetBasicPaymentProducts() throws InterruptedException {
        executeTest(
            "paymentProducts.json",
            200,
            latch -> new BasicPaymentProductsResponseListener() {
                @Override
                public void onSuccess(BasicPaymentProducts response) {
                    assertNotNull(response);
                    assertEquals(30, response.getPaymentProducts().size());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getBasicPaymentProducts(paymentContext, (BasicPaymentProductsResponseListener) listener)
        );
    }

    @Test
    public void testGetBasicPaymentProductsWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            latch -> new BasicPaymentProductsResponseListener() {
                @Override
                public void onSuccess(BasicPaymentProducts ignored) {
                    fail("Should not trigger success handler.");
                }

                @Override
                public void onFailure(@NonNull SdkException error) {
                    assertNotNull(error);
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getBasicPaymentProducts(paymentContext, (BasicPaymentProductsResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProduct() throws InterruptedException {
        executeTest(
            "paymentProductVisa.json",
            200,
            latch -> new PaymentProductResponseListener() {
                @Override
                public void onSuccess(@Nullable PaymentProduct response) {
                    assertNotNull(response);
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getPaymentProduct(1, paymentContext, (PaymentProductResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            latch -> new PaymentProductResponseListener() {
                @Override
                public void onSuccess(@Nullable PaymentProduct ignored) {
                    fail("Should not trigger success handler.");
                }

                @Override
                public void onFailure(@NonNull SdkException error) {
                    assertNotNull(error);
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getPaymentProduct(1, paymentContext, (PaymentProductResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductNetworks() throws InterruptedException {
        executeTest(
            "paymentProductNetworks.json",
            200,
            latch -> new PaymentProductNetworkResponseListener() {
                @Override
                public void onSuccess(PaymentProductNetworksResponse response) {
                    assertNotNull(response.getNetworks());
                    assertEquals(3, response.getNetworks().size());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getNetworksForPaymentProduct(
                    Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
                    paymentContext,
                    (PaymentProductNetworkResponseListener) listener
                )
        );
    }

    @Test
    public void testGetIinDetails() throws InterruptedException {
        executeTest(
            "normalIINResponseVisa.json",
            200,
            latch -> new IinLookupResponseListener() {
                @Override
                public void onSuccess(IinDetailsResponse response) {
                    assertEquals(IinDetailStatus.SUPPORTED, response.getStatus());
                    assertEquals("1", response.getPaymentProductId());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getIinDetails("411111", (IinLookupResponseListener) listener, paymentContext)
        );
    }

    @Test
    public void testGetIinDetailsWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            latch -> new IinLookupResponseListener() {
                @Override
                public void onSuccess(IinDetailsResponse ignored) {
                    fail("Should not receive onSuccess.");
                }

                @Override
                public void onFailure(@NonNull SdkException error) {
                    assertNotNull(((ResponseException) error).getApiError());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getIinDetails("411111", (IinLookupResponseListener) listener, paymentContext)
        );
    }

    @Test
    public void testGetPublicKey() throws InterruptedException {
        executeTest(
            "publicKeyResponse.json",
            200,
            latch -> new PublicKeyResponseListener() {
                @Override
                public void onSuccess(PublicKeyResponse response) {
                    assertNotNull(response);
                    assertNotNull(response.getPublicKey());
                    assertEquals("X.509", response.getPublicKey().getFormat());
                    assertEquals("12345678-aaaa-bbbb-cccc-876543218765", response.getKeyId());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getPublicKey((PublicKeyResponseListener) listener)
        );
    }

    @Test
    public void testGetPublicKeyWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            latch -> new PublicKeyResponseListener() {
                @Override
                public void onSuccess(PublicKeyResponse ignored) {
                    fail("Should not receive onSuccess.");
                }

                @Override
                public void onFailure(@NonNull SdkException error) {
                    assertNotNull(error);
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getPublicKey((PublicKeyResponseListener) listener)
        );
    }

    @Test
    public void testPreparePayment() throws InterruptedException {
        setMockServerResponse("cardPaymentProduct.json", 200);

        PaymentContext paymentContext = createPaymentContext();

        PaymentProduct product = getSdk().getPaymentProductSync(1, paymentContext);
        assertNotNull(product);

        PaymentRequest request = new PaymentRequest(product, null, false);
        request.setValue("cardNumber", "7822551678890142249");
        request.setValue("expiryDate", "122030");
        request.setValue("cvv", "123");
        request.setValue("cardholderName", "John Doe");

        setMockServerResponse("publicKeyResponse.json", 200);

        CountDownLatch latch = new CountDownLatch(1);

        PaymentRequestPreparedListener listener = new PaymentRequestPreparedListener() {
            @Override
            public void onSuccess(@Nullable EncryptedRequest preparedRequest) {
                assertNotNull(preparedRequest);
                assertNotNull(preparedRequest.getEncryptedCustomerInput());
                assertNotNull(preparedRequest.getEncodedClientMetaInfo());
                latch.countDown();
            }

            @Override
            public void onFailure(@NotNull SdkException error) {
                fail("Should not fail. " + error.getMessage());
                latch.countDown();
            }
        };

        getSdk().encryptPaymentRequest(request, listener);
        shadowOf(Looper.getMainLooper()).idle();

        boolean awaitSuccess = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Listener callback should have been called within 2 seconds.", awaitSuccess);
    }

    @Test
    public void testPrepareTokenPayment() throws InterruptedException {
        CreditCardTokenRequest tokenRequest = GsonHelperJava.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest.class
        );

        setMockServerResponse("publicKeyResponse.json", 200);

        CountDownLatch latch = new CountDownLatch(1);

        PaymentRequestPreparedListener listener = new PaymentRequestPreparedListener() {
            @Override
            public void onSuccess(@Nullable EncryptedRequest preparedRequest) {
                assertNotNull(preparedRequest);
                assertNotNull(preparedRequest.getEncryptedCustomerInput());
                assertNotNull(preparedRequest.getEncodedClientMetaInfo());
                latch.countDown();
            }

            @Override
            public void onFailure(@NotNull SdkException error) {
                fail("Should not fail. " + error.getMessage());
                latch.countDown();
            }
        };

        getSdk().encryptTokenRequest(tokenRequest, listener);

        shadowOf(Looper.getMainLooper()).idle();

        boolean awaitSuccess = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Listener callback should have been called within 2 seconds.", awaitSuccess);
    }

    @Test
    public void testGetCurrencyConversionQuoteForCard() throws InterruptedException {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        executeTest(
            "currencyConversionSuccess.json",
            200,
            latch -> new CurrencyConversionResponseListener() {
                @Override
                public void onSuccess(CurrencyConversionResponse response) {
                    assertEquals(ConversionResultType.ALLOWED, response.getResult().getResult());
                    assertNotNull(response.getProposal().getRate());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getCurrencyConversionQuote(
                    amountOfMoney,
                    "411111",
                    "1",
                    (CurrencyConversionResponseListener) listener
                )
        );
    }

    @Test
    public void testGetCurrencyConversionQuoteForToken() throws InterruptedException {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        executeTest(
            "currencyConversionSuccess.json",
            200,
            latch -> new CurrencyConversionResponseListener() {
                @Override
                public void onSuccess(CurrencyConversionResponse response) {
                    assertEquals(ConversionResultType.ALLOWED, response.getResult().getResult());
                    assertNotNull(response.getProposal().getRate());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getCurrencyConversionQuote(
                    amountOfMoney,
                    "token-123",
                    (CurrencyConversionResponseListener) listener
                )
        );
    }

    @Test
    public void testGetSurchargeCalculationForCard() throws InterruptedException {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        executeTest(
            "scWithSurcharge.json",
            200,
            latch -> new SurchargeCalculationResponseListener() {
                @Override
                public void onSuccess(SurchargeCalculationResponse response) {
                    assertEquals(1, response.getSurcharges().size());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getSurchargeCalculation(
                    amountOfMoney,
                    "411111",
                    "1",
                    (SurchargeCalculationResponseListener) listener
                )
        );
    }

    @Test
    public void testGetSurchargeCalculationForToken() throws InterruptedException {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        executeTest(
            "scWithSurcharge.json",
            200,
            latch -> new SurchargeCalculationResponseListener() {
                @Override
                public void onSuccess(SurchargeCalculationResponse response) {
                    assertEquals(1, response.getSurcharges().size());
                    latch.countDown();
                }

                @Override
                public void onFailure(@NotNull SdkException error) {
                    fail("Should not fail. " + error.getMessage());
                    latch.countDown();
                }
            },
            (paymentContext, listener) ->
                getSdk().getSurchargeCalculation(
                    amountOfMoney,
                    "token-789",
                    (SurchargeCalculationResponseListener) listener
                )
        );
    }

    private PaymentContextWithAmount createPaymentContext() {
        return new PaymentContextWithAmount(new AmountOfMoneyWithAmount(1298L, "EUR"), "NL", false);
    }

    private <T> void executeTest(
        String responseFile,
        int responseCode,
        Function<CountDownLatch, T> listenerCreator,
        BiConsumer<PaymentContextWithAmount, Object> testExecution
    ) throws InterruptedException {

        setMockServerResponse(responseFile, responseCode);

        var paymentContext = createPaymentContext();
        CountDownLatch latch = new CountDownLatch(1);

        Object listener = listenerCreator.apply(latch);

        testExecution.accept(paymentContext, listener);

        shadowOf(Looper.getMainLooper()).idle();

        boolean awaitSuccess = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Listener callback should have been called within " + 2 + " seconds.", awaitSuccess);
    }

    private OnlinePaymentsSdk getSdk() {
        SessionData sessionData = new SessionData(
            "sessionId",
            "clientId",
            mockWebServer.url("/").toString(),
            "https://example.com"
        );

        SdkConfiguration config = new SdkConfiguration(
            false,
            "SDKTestApp",
            "AndroidSDK",
            true
        );

        return new OnlinePaymentsSdk(sessionData, appContext, config);
    }

    private void setMockServerResponse(String jsonFile, int responseCode) {
        JsonElement json = GsonHelperJava.fromResourceJson(jsonFile, JsonElement.class);

        mockWebServer.enqueue(
            new MockResponse()
                .setBody(json.toString())
                .setResponseCode(responseCode)
        );
    }
}
