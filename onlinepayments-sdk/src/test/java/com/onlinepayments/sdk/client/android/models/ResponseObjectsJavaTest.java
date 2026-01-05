/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.JsonElement;
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney;
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount;
import com.onlinepayments.sdk.client.android.domain.Constants;
import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount;
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile;
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration;
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.ConversionResultType;
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus;
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.Surcharge;
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider;
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger;
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Tests that response objects returned by the SDK work correctly in Java.
 * Verifies getters, null handling, and nested object access.
 */
@RunWith(RobolectricTestRunner.class)
public class ResponseObjectsJavaTest {

    private MockWebServer mockWebServer;
    private static final Context mockContext = ApplicationProvider.getApplicationContext();
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
    public void testBasicPaymentProductsResponse() {
        setMockServerResponse("paymentProducts.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        BasicPaymentProducts response = getSdk().getBasicPaymentProductsSync(paymentContext);

        assertNotNull(response);
        assertNotNull(response.getPaymentProducts());
        assertFalse(response.getPaymentProducts().isEmpty());

        BasicPaymentProduct firstProduct = response.getPaymentProducts().get(0);
        assertNotNull(firstProduct);
        assertNotNull(firstProduct.getId());
        assertNotNull(firstProduct.getLabel());
    }

    @Test
    public void testBasicPaymentProductsAccountsOnFile() {
        setMockServerResponse("paymentProducts.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        BasicPaymentProducts response = getSdk().getBasicPaymentProductsSync(paymentContext);

        assertNotNull(response);

        List<AccountOnFile> accountsOnFile = response.getAccountsOnFile();
        assertNotNull(accountsOnFile);

        if (!accountsOnFile.isEmpty()) {
            AccountOnFile account = accountsOnFile.get(0);
            assertNotNull(account.getId());
        }
    }

    @Test
    public void testPaymentProductResponse() {
        setMockServerResponse("paymentProductVisa.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct response = getSdk().getPaymentProductSync(1, paymentContext);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getFields());
        assertFalse(response.getFields().isEmpty());
    }

    @Test
    public void testPaymentProductFields() {
        setMockServerResponse("paymentProductVisa.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct response = getSdk().getPaymentProductSync(1, paymentContext);

        assertNotNull(response);

        List<PaymentProductField> fields = response.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());

        PaymentProductField firstField = fields.get(0);
        assertNotNull(firstField.getId());
        assertNotNull(firstField.getLabel());
        assertNotNull(firstField.getDisplayHints());
    }

    @Test
    public void testPaymentProductGetField() {
        setMockServerResponse("paymentProductVisa.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct response = getSdk().getPaymentProductSync(1, paymentContext);

        assertNotNull(response);

        PaymentProductField cardNumberField = response.getField("cardNumber");
        assertNotNull(cardNumberField);
        assertEquals("cardNumber", cardNumberField.getId());
    }

    @Test
    public void testPaymentProductNetworkResponse() {
        setMockServerResponse("paymentProductNetworks.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProductNetworksResponse response = getSdk().getNetworksForPaymentProductSync(
            Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
            paymentContext
        );

        assertNotNull(response);
        assertNotNull(response.getNetworks());
        assertFalse(response.getNetworks().isEmpty());
        assertEquals(3, response.getNetworks().size());
    }

    @Test
    public void testIinDetailsResponse() {
        setMockServerResponse("normalIINResponseVisa.json");

        var amountOfMoney = new AmountOfMoneyWithAmount(1298L, "EUR");
        var paymentContext = new PaymentContextWithAmount(amountOfMoney, "NL", false);

        IinDetailsResponse response = getSdk().getIinDetailsSync("414141", paymentContext);

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(IinDetailStatus.SUPPORTED, response.getStatus());
        assertNotNull(response.getPaymentProductId());
        assertEquals("1", response.getPaymentProductId());
    }

    @Test
    public void testIinDetailsResponseNotEnoughDigits() {
        setMockServerResponse("normalIINResponseVisa.json");

        var amountOfMoney = new AmountOfMoneyWithAmount(1298L, "EUR");
        var paymentContext = new PaymentContextWithAmount(amountOfMoney, "NL", false);

        IinDetailsResponse response = getSdk().getIinDetailsSync("4141", paymentContext);

        assertNotNull(response);
        assertEquals(IinDetailStatus.NOT_ENOUGH_DIGITS, response.getStatus());
        assertNull(response.getPaymentProductId());
    }

    @Test
    public void testPublicKeyResponse() {
        setMockServerResponse("publicKeyResponse.json");

        PublicKeyResponse response = getSdk().getPublicKeySync();

        assertNotNull(response);
        assertNotNull(response.getKeyId());
        assertNotNull(response.getPublicKey());
        assertEquals("12345678-aaaa-bbbb-cccc-876543218765", response.getKeyId());
        assertEquals("X.509", response.getPublicKey().getFormat());
    }

    @Test
    public void testEncryptedRequestResponse() {
        setMockServerResponse("publicKeyResponse.json");

        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest(
            "4111111111111111",
            "John Doe",
            "1225",
            "123",
            1
        );

        EncryptedRequest response = getSdk().encryptTokenRequestSync(tokenRequest);

        assertNotNull(response);
        assertNotNull(response.getEncryptedCustomerInput());
        assertNotNull(response.getEncodedClientMetaInfo());
        assertFalse(response.getEncryptedCustomerInput().isEmpty());
        assertFalse(response.getEncodedClientMetaInfo().isEmpty());
    }

    @Test
    public void testCurrencyConversionResponse() {
        setMockServerResponse("currencyConversionSuccess.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        CurrencyConversionResponse response = getSdk().getCurrencyConversionQuoteSync(
            amountOfMoney,
            "411111",
            "1"
        );

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(ConversionResultType.ALLOWED, response.getResult().getResult());
        assertNotNull(response.getProposal());
        assertNotNull(response.getProposal().getRate());
    }

    @Test
    public void testCurrencyConversionResponseNoRate() {
        setMockServerResponse("currencyConversionNoRate.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        CurrencyConversionResponse response = getSdk().getCurrencyConversionQuoteSync(
            amountOfMoney,
            "411111",
            "1"
        );

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(ConversionResultType.NO_RATE, response.getResult().getResult());
    }

    @Test
    public void testSurchargeCalculationResponse() {
        setMockServerResponse("scWithSurcharge.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");

        SurchargeCalculationResponse response = getSdk().getSurchargeCalculationSync(
            amountOfMoney,
            "411111",
            "1"
        );

        assertNotNull(response);
        assertNotNull(response.getSurcharges());
        assertEquals(1, response.getSurcharges().size());

        Surcharge surcharge = response.getSurcharges().get(0);
        assertNotNull(surcharge);
    }

    @Test
    public void testPaymentRequestWithResponse() {
        setMockServerResponse("paymentProductVisa.json");

        AmountOfMoney amountOfMoney = new AmountOfMoney(1298L, "EUR");
        PaymentContext paymentContext = new PaymentContext(amountOfMoney, "NL", false);

        PaymentProduct product = getSdk().getPaymentProductSync(1, paymentContext);

        PaymentRequest request = new PaymentRequest(product, null, false);
        request.setValue("cardNumber", "4111111111111111");
        request.setValue("expiryDate", "122030");
        request.setValue("cvv", "123");

        assertNotNull(request.getPaymentProduct());
        assertEquals(product, request.getPaymentProduct());
        assertEquals("4111111111111111", request.getValue("cardNumber"));
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

    private void setMockServerResponse(String jsonFile) {
        JsonElement json = GsonHelperJava.fromResourceJson(jsonFile, JsonElement.class);
        mockWebServer.enqueue(
            new MockResponse()
                .setBody(json.toString())
                .setResponseCode(200)
        );
    }
}
