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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.util.Base64;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.JsonElement;
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.exception.ApiException;
import com.onlinepayments.sdk.client.android.model.AmountOfMoney;
import com.onlinepayments.sdk.client.android.model.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.currencyconversion.ConversionResultType;
import com.onlinepayments.sdk.client.android.model.iin.IinStatus;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.providers.LoggerProvider;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;
import com.onlinepayments.sdk.client.android.util.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.test.TestCoroutineDispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest({Base64.class})
public class SessionJavaTest {
    private static MockWebServer mockWebServer;
    private static final Context mockContext = ApplicationProvider.getApplicationContext();
    private final Logger mockLogger = mock(Logger.class);

    private static final List<String> filteredProductIds = Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS;

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
    public void testGetPaymentProducts() {
        setMockServerResponse("paymentProducts.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        var paymentProducts = getSession().getBasicPaymentProductsSync(paymentContext);

        // After filtering we get 29 products
        assertEquals(29, paymentProducts.getBasicPaymentProducts().size());

        List<String> actualIds = paymentProducts.getBasicPaymentProducts()
            .stream()
            .map(BasicPaymentProduct::getId)
            .collect(Collectors.toList());

        for (String filteredId : filteredProductIds) {
            assertFalse("Product ID " + filteredId + " should have been filtered out.",
                actualIds.contains(filteredId));
        }
    }

    @Test
    public void testGetPaymentProductsWithError() {
        setMockServerResponse("apiError400.json", 400);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        ApiException exception = assertThrows(
            ApiException.class,
            () -> getSession().getBasicPaymentProductsSync(paymentContext)
        );
        assertNotNull(exception);
    }

    @Test
    public void testGetPaymentItems() {
        setMockServerResponse("paymentProducts.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);
        var paymentItems = getSession().getBasicPaymentItemsSync(paymentContext);

        // After filtering we get 29 products
        assertEquals(29, paymentItems.getBasicPaymentItems().size());
    }

    @Test
    public void testGetPaymentItemsWithError() {
        setMockServerResponse("apiError400.json", 400);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        ApiException exception = assertThrows(
            ApiException.class,
            () -> getSession().getBasicPaymentItemsSync(paymentContext)
        );
        assertNotNull(exception);
    }

    @Test
    public void testGetPaymentProduct() {
        setMockServerResponse("paymentProductVisa.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        var paymentProduct = getSession().getPaymentProductSync("1", paymentContext);

        assertNotNull(paymentProduct);
        assertFalse(paymentProduct.getDisplayHintsList().isEmpty());
    }

    @Test
    public void testGetPaymentProductWithMoreFields() {
        setMockServerResponse("paymentProductDinersClub.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        var paymentProduct = getSession().getPaymentProductSync("132", paymentContext);

        assertNotNull(paymentProduct);
        assertFalse(paymentProduct.getDisplayHintsList().isEmpty());
    }

    @Test
    public void testGetPaymentProductFromCache() {
        setMockServerResponse("paymentProductVisa.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        Session session = getSession();
        var paymentProduct = session.getPaymentProductSync("1", paymentContext);

        // set bad request response, which should not be called if cached
        setMockServerResponse("apiErrorItemComplete.json", 404);

        var cachedProduct = session.getPaymentProductSync("1", paymentContext);
        assertEquals(paymentProduct, cachedProduct);

        // different product should invoke API call
        assertThrows(
            ApiException.class,
            () -> session.getPaymentProductSync("2", paymentContext)
        );
    }

    @Test
    public void testGetPaymentProductNetworks() {
        setMockServerResponse("paymentProductNetworks.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        Session session = getSession();
        var networks = session.getNetworksForPaymentProductSync(
            Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
            paymentContext
        );

        assertNotNull(networks.getNetworks());
        assertEquals(3, networks.getNetworks().size());
    }

    @Test
    public void testGetIinDetails() {
        setMockServerResponse("normalIINResponseVisa.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        Session session = getSession();

        // test min digits validation
        var iinDetails = session.getIinDetailsSync("4141", paymentContext);
        assertEquals(IinStatus.NOT_ENOUGH_DIGITS, iinDetails.getStatus());
        assertNull(iinDetails.getPaymentProductId());

        // test valid response
        iinDetails = session.getIinDetailsSync("414141", paymentContext);
        assertEquals(IinStatus.SUPPORTED, iinDetails.getStatus());
        assertEquals("1", iinDetails.getPaymentProductId());
    }

    @Test
    public void testGetIinDetailsNotFound() {
        setMockServerResponse("iinDetailsNotFound.json", 404);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        Session session = getSession();
        var details = session.getIinDetailsSync("411111", paymentContext);

        assertEquals(IinStatus.UNKNOWN, details.getStatus());
    }

    @Test
    public void testGetIinDetailsAlreadyInProgress() throws Exception {
        setMockServerResponse("normalIINResponseVisa.json", 200, 1000);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        Session session = getSession();

        // Start the first call (delays for 1000ms set above)
        Thread firstCall = new Thread(() -> {
            try {
                session.getIinDetailsSync("411111", paymentContext);
            } catch (Exception ignored) {
            }
        });
        firstCall.start();

        // Give the thread some time to enter the method
        Thread.sleep(50);

        // Second call should throw the exception
        ApiException exception = assertThrows(
            ApiException.class,
            () -> session.getIinDetailsSync("411111", paymentContext)
        );
        assertEquals("IIN lookup is already in progress", exception.getMessage());

        firstCall.interrupt();
    }

    @Test
    public void testGetPublicKey() {
        setMockServerResponse("publicKeyResponse.json", 200);

        var publicKey = getSession().getPublicKeySync();

        assertNotNull(publicKey);
        assertNotNull(publicKey.getPublicKey());
        assertEquals("X.509", publicKey.getPublicKey().getFormat());
        assertEquals("12345678-aaaa-bbbb-cccc-876543218765", publicKey.getKeyId());
    }

    @Test
    public void testGetPublicKeyBadRequest() {
        mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));

        var publicKey = getSession().getPublicKeySync();

        assertNull(publicKey.getPublicKey());
    }

    @Test
    public void testPreparePayment() {
        PaymentRequest paymentRequest = GsonHelperJava.fromResourceJson(
            "paymentRequest.json",
            PaymentRequest.class
        );

        setMockServerResponse("publicKeyResponse.json", 200);

        var preparedRequest = getSession().preparePaymentRequestSync(paymentRequest);

        assertNotNull(preparedRequest.getEncryptedFields());
        assertNotNull(preparedRequest.getEncodedClientMetaInfo());
    }

    @Test
    public void testPrepareTokenPayment() {
        CreditCardTokenRequest tokenRequest = GsonHelperJava.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest.class
        );

        setMockServerResponse("publicKeyResponse.json", 200);

        var preparedRequest = getSession().prepareTokenPaymentRequestSync(tokenRequest);

        assertNotNull(preparedRequest.getEncryptedFields());
        assertNotNull(preparedRequest.getEncodedClientMetaInfo());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCard() {
        setMockServerResponse("currencyConversionSuccess.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1");

        assertEquals(ConversionResultType.ALLOWED, quote.getResult().getResult());
        assertNotNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCardNoRate() {
        setMockServerResponse("currencyConversionNoRate.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1");

        assertEquals(ConversionResultType.NO_RATE, quote.getResult().getResult());
        //noinspection DataFlowIssue
        assertNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCardNotFound() {
        setMockServerResponse("currencyConversionNotFound.json", 400);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        assertThrows(
            ApiException.class,
            () -> getSession().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1")
        );
    }

    @Test
    public void testGetCurrencyConversionQuoteForToken() {
        setMockServerResponse("currencyConversionSuccess.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getCurrencyConversionQuoteSync(amountOfMoney, "411111");

        assertEquals(ConversionResultType.ALLOWED, quote.getResult().getResult());
        assertNotNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetCurrencyConversionQuoteForTokenNoRate() {
        setMockServerResponse("currencyConversionNoRate.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getCurrencyConversionQuoteSync(amountOfMoney, "411111");

        assertEquals(ConversionResultType.NO_RATE, quote.getResult().getResult());
        //noinspection DataFlowIssue
        assertNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetSurchargeCalculationForCard() {
        setMockServerResponse("scWithSurcharge.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getSurchargeCalculationSync(amountOfMoney, "411111", "1");

        assertEquals(1, quote.getSurcharges().size());
    }

    @Test
    public void testGetSurchargeCalculationForToken() {
        setMockServerResponse("scWithSurcharge.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        var quote = getSession().getSurchargeCalculationSync(amountOfMoney, "411111");

        assertEquals(1, quote.getSurcharges().size());
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
        setMockServerResponse(jsonFile, responseCode, 0L);
    }

    private void setMockServerResponse(String jsonFile, int responseCode, long delayMillis) {
        JsonElement json = GsonHelperJava.fromResourceJson(jsonFile, JsonElement.class);
        mockWebServer.enqueue(
            new MockResponse()
                .setBody(json.toString())
                .setResponseCode(responseCode)
                .setBodyDelay(delayMillis, TimeUnit.MILLISECONDS)
        );
    }

    @Test
    public void testAllFilteredPaymentProductsThrowException() {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        for (String filteredId : filteredProductIds) {
            ApiException exception = assertThrows(
                ApiException.class,
                () -> getSession().getPaymentProductSync(filteredId, paymentContext)
            );

            assertNotNull(exception);
        }
    }
}
