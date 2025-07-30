/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.exception.EncryptDataException;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener;
import com.onlinepayments.sdk.client.android.listener.CurrencyConversionResponseListener;
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener;
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener;
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener;
import com.onlinepayments.sdk.client.android.model.AmountOfMoney;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.currencyconversion.ConversionResultType;
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinStatus;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse;
import com.onlinepayments.sdk.client.android.providers.LoggerProvider;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;
import com.onlinepayments.sdk.client.android.util.Logger;

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

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.test.TestCoroutineDispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest({Base64.class})
public class SessionListenersJavaTest {
    private static MockWebServer mockWebServer;
    private static final Context mockContext = ApplicationProvider.getApplicationContext();
    private final Logger mockLogger = mock(Logger.class);

    /**
     * @noinspection deprecation
     */
    private final TestCoroutineDispatcher testDispatcher = new TestCoroutineDispatcher();

    @Before
    public void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        LoggerProvider.INSTANCE.setLogger(mockLogger);
        Session.Companion.setMainDispatcher(testDispatcher);
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        LoggerProvider.INSTANCE.reset();
        testDispatcher.cleanupTestCoroutines();
    }

    @Test
    public void testGetPaymentProducts() throws InterruptedException {
        executeTest(
            "paymentProducts.json",
            200,
            countDownLatch -> new BasicPaymentProductsResponseListener() {
                public void onSuccess(BasicPaymentProducts response) {
                    // After filtering we get 29 products
                    assertEquals(29, response.getBasicPaymentProducts().size());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getBasicPaymentProducts(paymentContext, (BasicPaymentProductsResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductsWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new BasicPaymentProductsResponseListener() {
                public void onSuccess(BasicPaymentProducts ignoredResponse) {
                    fail("Should not trigger success handler.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error);
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getBasicPaymentProducts(paymentContext, (BasicPaymentProductsResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentItems() throws InterruptedException {
        executeTest(
            "paymentProducts.json",
            200,
            countDownLatch -> new BasicPaymentItemsResponseListener() {
                public void onSuccess(BasicPaymentItems response) {
                    // After filtering we get 29 products
                    assertEquals(29, response.getBasicPaymentItems().size());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getBasicPaymentItems(paymentContext, (BasicPaymentItemsResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentItemsWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new BasicPaymentItemsResponseListener() {
                public void onSuccess(BasicPaymentItems ignoredResponse) {
                    fail("Should not trigger success handler.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error);
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getBasicPaymentItems(paymentContext, (BasicPaymentItemsResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProduct() throws InterruptedException {
        executeTest(
            "paymentProductVisa.json",
            200,
            countDownLatch -> new PaymentProductResponseListener() {
                public void onSuccess(PaymentProduct response) {
                    assertNotNull(response);
                    assertFalse(response.getDisplayHintsList().isEmpty());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getPaymentProduct("1", paymentContext, (PaymentProductResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new PaymentProductResponseListener() {
                public void onSuccess(PaymentProduct response) {
                    fail("Should not trigger success handler.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error);
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getPaymentProduct("1", paymentContext, (PaymentProductResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductNetworks() throws InterruptedException {
        executeTest(
            "paymentProductNetworks.json",
            200,
            countDownLatch -> new PaymentProductNetworkResponseListener() {
                public void onSuccess(PaymentProductNetworkResponse response) {
                    assertNotNull(response.getNetworks());
                    assertEquals(3, response.getNetworks().size());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext, (PaymentProductNetworkResponseListener) listener)
        );
    }

    @Test
    public void testGetPaymentProductNetworksWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new PaymentProductNetworkResponseListener() {
                public void onSuccess(PaymentProductNetworkResponse response) {
                    fail("Should not trigger success handler.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error);
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext, (PaymentProductNetworkResponseListener) listener)
        );
    }

    @Test
    public void testGetIinDetails() throws InterruptedException {
        executeTest(
            "normalIINResponseVisa.json",
            200,
            countDownLatch -> new IinLookupResponseListener() {
                public void onSuccess(IinDetailsResponse response) {
                    assertEquals(IinStatus.SUPPORTED, response.getStatus());
                    assertEquals("1", response.getPaymentProductId());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getIinDetails("411111", (IinLookupResponseListener) listener, paymentContext)
        );
    }

    @Test
    public void testGetIinDetailsWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new IinLookupResponseListener() {
                public void onSuccess(IinDetailsResponse response) {
                    fail("Should not receive onSuccess.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error.getApiError());
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getIinDetails("411111", (IinLookupResponseListener) listener, paymentContext)
        );
    }

    @Test
    public void testGetPublicKey() throws InterruptedException {
        executeTest(
            "publicKeyResponse.json",
            200,
            countDownLatch -> new PublicKeyResponseListener() {
                public void onSuccess(PublicKeyResponse response) {
                    assertNotNull(response);
                    assertNotNull(response.getPublicKey());
                    assertEquals("X.509", response.getPublicKey().getFormat());
                    assertEquals("12345678-aaaa-bbbb-cccc-876543218765", response.getKeyId());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getPublicKey((PublicKeyResponseListener) listener)
        );
    }

    @Test
    public void testGetPublicKeyWithError() throws InterruptedException {
        executeTest(
            "apiError400.json",
            400,
            countDownLatch -> new PublicKeyResponseListener() {
                public void onSuccess(PublicKeyResponse response) {
                    fail("Should not receive onSuccess.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertEquals("Request failed with status: 400", error.getMessage());
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getPublicKey((PublicKeyResponseListener) listener)
        );
    }

    @Test
    public void testPreparePayment() throws InterruptedException {
        PaymentRequest paymentRequest = GsonHelperJava.fromResourceJson(
            "paymentRequest.json",
            PaymentRequest.class
        );
        setMockServerResponse("publicKeyResponse.json", 200);

        var countDownLatch = new CountDownLatch(1);

        var listener = new PaymentRequestPreparedListener() {
            @Override
            public void onPaymentRequestPrepared(@Nullable PreparedPaymentRequest preparedRequest) {
                assertNotNull(preparedRequest);
                assertNotNull(preparedRequest.getEncryptedFields());
                assertNotNull(preparedRequest.getEncodedClientMetaInfo());
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(@Nullable EncryptDataException e) {
                fail("Should not fail in a success scenario.");
            }
        };

        getSession().preparePaymentRequest(paymentRequest, listener);

        shadowOf(Looper.getMainLooper()).idle();

        boolean awaitSuccess = countDownLatch.await(2, TimeUnit.SECONDS);
        assertTrue("Listener callback should have been called within 2 seconds.", awaitSuccess);
    }

    @Test
    public void testGetCurrencyConversionQuoteForCard() throws InterruptedException {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        executeTest(
            "currencyConversionSuccess.json",
            200,
            countDownLatch -> new CurrencyConversionResponseListener() {
                public void onSuccess(CurrencyConversionResponse response) {
                    assertEquals(ConversionResultType.ALLOWED, response.getResult().getResult());
                    assertNotNull(response.getProposal().getRate());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getCurrencyConversionQuote(amountOfMoney, "411111", "1", (CurrencyConversionResponseListener) listener)
        );
    }

    @Test
    public void testGetCurrencyConversionQuoteForToken() throws InterruptedException {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        executeTest(
            "currencyConversionSuccess.json",
            200,
            countDownLatch -> new CurrencyConversionResponseListener() {
                public void onSuccess(CurrencyConversionResponse response) {
                    assertEquals(ConversionResultType.ALLOWED, response.getResult().getResult());
                    assertNotNull(response.getProposal().getRate());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getCurrencyConversionQuote(amountOfMoney, "411111", (CurrencyConversionResponseListener) listener)
        );
    }

    @Test
    public void testGetCurrencyConversionQuoteForCardNotFound() throws InterruptedException {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        executeTest(
            "currencyConversionNotFound.json",
            400,
            countDownLatch -> new CurrencyConversionResponseListener() {
                public void onSuccess(CurrencyConversionResponse response) {
                    fail("Should not trigger success handler.");
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    assertNotNull(error);
                    countDownLatch.countDown();
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getCurrencyConversionQuote(amountOfMoney, "411111", "1", (CurrencyConversionResponseListener) listener)
        );
    }

    @Test
    public void testGetSurchargeCalculationForCard() throws InterruptedException {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        executeTest(
            "scWithSurcharge.json",
            200,
            countDownLatch -> new SurchargeCalculationResponseListener() {
                public void onSuccess(SurchargeCalculationResponse response) {
                    assertEquals(1, response.getSurcharges().size());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getSurchargeCalculation(amountOfMoney, "411111", "1", (SurchargeCalculationResponseListener) listener)
        );
    }

    @Test
    public void testGetSurchargeCalculationForToken() throws InterruptedException {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        executeTest(
            "scWithSurcharge.json",
            200,
            countDownLatch -> new SurchargeCalculationResponseListener() {
                public void onSuccess(SurchargeCalculationResponse response) {
                    assertEquals(1, response.getSurcharges().size());
                    countDownLatch.countDown();
                }

                public void onApiError(@NonNull ErrorResponse error) {
                    fail("Should not receive onApiError.");
                }

                public void onException(@NonNull Throwable e) {
                    fail("Should not receive onException.");
                }
            },
            (paymentContext, listener) -> getSession().getSurchargeCalculation(amountOfMoney, "411111", (SurchargeCalculationResponseListener) listener)
        );
    }

    private PaymentContext createPaymentContext() {
        var amountOfMoney = new AmountOfMoney(1298L, "EUR");
        return new PaymentContext(amountOfMoney, "NL", false);
    }

    private <T> void executeTest(
        String responseFile,
        int responseCode,
        Function<CountDownLatch, T> listenerCreator,
        BiConsumer<PaymentContext, Object> testExecution
    ) throws InterruptedException {
        setMockServerResponse(responseFile, responseCode);

        var paymentContext = createPaymentContext();
        var latch = new CountDownLatch(1);

        var listener = listenerCreator.apply(latch);

        testExecution.accept(paymentContext, listener);

        shadowOf(Looper.getMainLooper()).idle();

        boolean awaitSuccess = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Listener callback should have been called within " + 2 + " seconds.", awaitSuccess);
    }

    private Session getSession() {
        kotlinx.coroutines.Job supervisorJob = kotlinx.coroutines.SupervisorKt.SupervisorJob(null);
        CoroutineContext testContext = supervisorJob.plus(Dispatchers.getUnconfined());
        CoroutineScope testScope = CoroutineScopeKt.CoroutineScope(testContext);

        return new Session(
            "sessionId",
            "clientId",
            mockWebServer.url("/").toString(),
            "https://example.com",
            false,
            "SDKTestApp",
            true,
            "SDKTest",
            mockContext,
            testScope
        );
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
