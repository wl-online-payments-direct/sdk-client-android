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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import android.content.Context;

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
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse;
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto;
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory;
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider;
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.stream.Collectors;

import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class OnlinePaymentsSdkJavaTest {

    private MockWebServer mockWebServer;
    private static final Context mockContext = ApplicationProvider.getApplicationContext();
    private final Logger mockLogger = mock(Logger.class);

    private static final List<Integer> filteredProductIds = Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS;

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
    public void testGetPaymentProducts() {
        setMockServerResponse("paymentProducts.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        BasicPaymentProducts paymentProducts = getSdk().getBasicPaymentProductsSync(paymentContext);

        assertEquals(29, paymentProducts.getPaymentProducts().size());

        List<Integer> actualIds = paymentProducts.getPaymentProducts()
            .stream()
            .map(BasicPaymentProduct::getId)
            .collect(Collectors.toList());

        for (Integer filteredId : filteredProductIds) {
            assertFalse("Product ID " + filteredId + " should have been filtered out.",
                actualIds.contains(filteredId));
        }
    }

    @Test
    public void testGetPaymentProductsWithError() {
        setMockServerResponse("apiError400.json", 400);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        ResponseException exception = assertThrows(
            ResponseException.class,
            () -> getSdk().getBasicPaymentProductsSync(paymentContext)
        );
        assertNotNull(exception);
    }

    @Test
    public void testGetPaymentProduct() {
        setMockServerResponse("paymentProductVisa.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct paymentProduct = getSdk().getPaymentProductSync(1, paymentContext);

        assertNotNull(paymentProduct);
    }

    @Test
    public void testGetPaymentProductWithMoreFields() {
        setMockServerResponse("paymentProductDinersClub.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct paymentProduct = getSdk().getPaymentProductSync(132, paymentContext);

        assertNotNull(paymentProduct);
    }

    @Test
    public void testGetPaymentProductNetworks() {
        setMockServerResponse("paymentProductNetworks.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProductNetworksResponse networks = getSdk().getNetworksForPaymentProductSync(
            Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
            paymentContext
        );

        assertNotNull(networks.getNetworks());
        assertEquals(3, networks.getNetworks().size());
    }

    @Test
    public void testGetIinDetails() {
        setMockServerResponse("normalIINResponseVisa.json", 200);

        var amountOfMoney = new AmountOfMoneyWithAmount(1298L, "EUR");
        var paymentContext = new PaymentContextWithAmount(amountOfMoney, "NL", false);

        OnlinePaymentsSdk sdk = getSdk();

        IinDetailsResponse iinDetails = sdk.getIinDetailsSync("4141", paymentContext);
        assertEquals(IinDetailStatus.NOT_ENOUGH_DIGITS, iinDetails.getStatus());
        assertNull(iinDetails.getPaymentProductId());

        iinDetails = sdk.getIinDetailsSync("414141", paymentContext);
        assertEquals(IinDetailStatus.SUPPORTED, iinDetails.getStatus());
        assertEquals("1", iinDetails.getPaymentProductId());
    }

    @Test
    public void testGetIinDetailsNotFound() {
        setMockServerResponse("iinDetailsNotFound.json", 404);

        var amountOfMoney = new AmountOfMoneyWithAmount(1298L, "EUR");
        var paymentContext = new PaymentContextWithAmount(amountOfMoney, "NL", false);

        IinDetailsResponse details = getSdk().getIinDetailsSync("411111", paymentContext);
        assertEquals(IinDetailStatus.UNKNOWN, details.getStatus());
    }

    @Test
    public void testGetPublicKey() {
        setMockServerResponse("publicKeyResponse.json", 200);

        PublicKeyResponse publicKey = getSdk().getPublicKeySync();

        assertNotNull(publicKey);
        assertNotNull(publicKey.getPublicKey());
        assertEquals("X.509", publicKey.getPublicKey().getFormat());
        assertEquals("12345678-aaaa-bbbb-cccc-876543218765", publicKey.getKeyId());
    }

    @Test
    public void testGetPublicKeyBadRequestReturnsNullKey() {
        mockWebServer.enqueue(new MockResponse().setBody("{}").setResponseCode(200));

        PublicKeyResponse publicKey = getSdk().getPublicKeySync();

        assertNull(publicKey.getPublicKey());
    }

    @Test
    public void testCreatePaymentRequest() {
        PaymentProductDto paymentProductDto = GsonHelperJava.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto.class
        );

        PaymentProduct paymentProduct = new PaymentProductFactory().createPaymentProduct(paymentProductDto);

        PaymentRequest request = new PaymentRequest(paymentProduct, null, false);

        request.setValue("cardNumber", "7822551678890142249");
        request.setValue("expiryDate", "122030");
        request.setValue("cvv", "123");
        request.setValue("cardholderName", "Test");

        setMockServerResponse("publicKeyResponse.json", 200);

        EncryptedRequest prepared = getSdk().encryptPaymentRequestSync(request);

        assertNotNull(prepared);
        assertNotNull(prepared.getEncryptedCustomerInput());
        assertNotNull(prepared.getEncodedClientMetaInfo());
    }

    @Test
    public void testCreateTokenPaymentRequest() {
        CreditCardTokenRequest tokenRequest = GsonHelperJava.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest.class
        );

        setMockServerResponse("publicKeyResponse.json", 200);

        EncryptedRequest prepared = getSdk().encryptTokenRequestSync(tokenRequest);

        assertNotNull(prepared.getEncryptedCustomerInput());
        assertNotNull(prepared.getEncodedClientMetaInfo());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCard() {
        setMockServerResponse("currencyConversionSuccess.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        CurrencyConversionResponse quote = getSdk().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1");

        assertEquals(ConversionResultType.ALLOWED, quote.getResult().getResult());
        assertNotNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCardNoRate() {
        setMockServerResponse("currencyConversionNoRate.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        CurrencyConversionResponse quote = getSdk().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1");

        assertEquals(ConversionResultType.NO_RATE, quote.getResult().getResult());
    }

    @Test
    public void testGetCurrencyConversionQuoteForCardNotFound() {
        setMockServerResponse("currencyConversionNotFound.json", 400);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        assertThrows(
            ResponseException.class,
            () -> getSdk().getCurrencyConversionQuoteSync(amountOfMoney, "411111", "1")
        );
    }

    @Test
    public void testGetCurrencyConversionQuoteForToken() {
        setMockServerResponse("currencyConversionSuccess.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        CurrencyConversionResponse quote = getSdk().getCurrencyConversionQuoteSync(amountOfMoney, "411111");

        assertEquals(ConversionResultType.ALLOWED, quote.getResult().getResult());
        assertNotNull(quote.getProposal().getRate());
    }

    @Test
    public void testGetCurrencyConversionQuoteForTokenNoRate() {
        setMockServerResponse("currencyConversionNoRate.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        CurrencyConversionResponse quote = getSdk().getCurrencyConversionQuoteSync(amountOfMoney, "411111");

        assertEquals(ConversionResultType.NO_RATE, quote.getResult().getResult());
    }

    @Test
    public void testGetSurchargeCalculationForCard() {
        setMockServerResponse("scWithSurcharge.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        SurchargeCalculationResponse resp = getSdk().getSurchargeCalculationSync(amountOfMoney, "411111", "1");

        assertEquals(1, resp.getSurcharges().size());
    }

    @Test
    public void testGetSurchargeCalculationForToken() {
        setMockServerResponse("scWithSurcharge.json", 200);

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        SurchargeCalculationResponse resp = getSdk().getSurchargeCalculationSync(amountOfMoney, "411111");

        assertEquals(1, resp.getSurcharges().size());
    }

    @Test
    public void testAllFilteredPaymentProductsThrowException() {
        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        OnlinePaymentsSdk sdk = getSdk();

        for (Integer filteredId : filteredProductIds) {
            ResponseException ex = assertThrows(
                ResponseException.class,
                () -> sdk.getPaymentProductSync(filteredId, paymentContext)
            );
            assertNotNull(ex);
        }
    }

    private OnlinePaymentsSdk getSdk() {
        SessionData sessionData = new SessionData(
            "sessionId",
            "clientId",
            mockWebServer.url("/").toString(),
            "https://example.com"
        );

        SdkConfiguration configuration = new SdkConfiguration(
            false,
            "Test",
            "TestSDK",
            false
        );

        return new OnlinePaymentsSdk(sessionData, mockContext, configuration);
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
