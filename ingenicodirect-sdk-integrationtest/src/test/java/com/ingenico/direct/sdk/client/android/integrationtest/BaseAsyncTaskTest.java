package com.ingenico.direct.sdk.client.android.integrationtest;

import android.content.Context;
import android.content.res.Resources;

import com.ingenico.direct.sdk.client.android.integrationtest.TestUtil.GsonHelper;
import com.ingenico.direct.sdk.client.android.integrationtest.sessions.SessionUtil;
import com.ingenico.direct.sdk.client.android.integrationtest.sessions.TokenUtil;
import com.ingenico.direct.sdk.client.android.communicate.C2sCommunicator;
import com.ingenico.direct.sdk.client.android.communicate.C2sCommunicatorConfiguration;
import com.ingenico.direct.sdk.client.android.exception.CommunicationException;
import com.ingenico.direct.sdk.client.android.model.AmountOfMoney;
import com.ingenico.direct.sdk.client.android.model.CountryCode;
import com.ingenico.direct.sdk.client.android.model.CurrencyCode;
import com.ingenico.direct.sdk.client.android.model.PaymentContext;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.AccountOnFile;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsAccountOnFile;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields;
import com.ingenico.direct.sdk.client.android.session.Session;
import com.ingenico.direct.ApiException;
import com.ingenico.direct.Client;
import com.ingenico.direct.CommunicatorConfiguration;
import com.ingenico.direct.Factory;
import com.ingenico.direct.domain.CreatePaymentRequest;
import com.ingenico.direct.domain.CreatePaymentResponse;
import com.ingenico.direct.domain.SessionRequest;
import com.ingenico.direct.domain.SessionResponse;
import com.ingenico.direct.domain.CreateTokenRequest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Base class for testing the retrieval of all sorts of (basic) payment product(s)/group(s)/item(s).
 * This base class is capable of setting up a connection, initializing the required mocks for the tests and validating
 * the returned objects
 *
 * Copyright 2014 Global Collect Services B.V
 *
 */
public class BaseAsyncTaskTest {

    static final PaymentContext minimalValidPaymentContext = new PaymentContext(
            new AmountOfMoney(1000L, CurrencyCode.EUR),
            CountryCode.NL,
            true
    );

    static final int ASYNCTASK_CALLBACK_TEST_TIMEOUT_SEC = 5;
    static final int PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC = 10;

    private static Client client;
    private static String clientBaseUrl;

    private static String testMerchantId;

    private static TokenUtil tokenUtil;
    private static SessionUtil sessionUtil;

    private String token;

    private Session session;

    @Mock
    private C2sCommunicatorConfiguration mockConfiguration;

    @Mock
    private Context mockContext;

    @Mock
    private Resources resources;

    private C2sCommunicator communicator;

    @BeforeClass
    public static void initializeClass() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = BaseAsyncTaskTest.class.getResourceAsStream("/itconfiguration.properties");
        try {
            properties.load(inputStream);
        } finally {
            inputStream.close();
        }

        testMerchantId = properties.getProperty("direct.api.merchantId");
        clientBaseUrl = properties.getProperty("direct.api.endpoint");

        CommunicatorConfiguration communicatorConfiguration = new CommunicatorConfiguration(properties)
                .withApiKeyId(properties.getProperty("direct.api.apiKeyId"))
                .withSecretApiKey(properties.getProperty("direct.api.secretApiKey"));
        client = (Client) Factory.createClient(communicatorConfiguration);

        tokenUtil = new TokenUtil(client);
        sessionUtil = new SessionUtil(client);
    }

    @AfterClass
    public static void cleanupClass() throws IOException {
        //client.close();
    }

    Context getContext() {
        return mockContext;
    }

    C2sCommunicator getCommunicator() {
        return communicator;
    }

    Session getSession() {
        return session;
    }

    /**
     * Create a mocked configuration that produces invalid requests
     */
    void initializeInValidMocksAndSession() {
        // It is not necessary to actually create a session, since the point of the invalid
        // mocks is that setting up a session fails.
        initializeInvalidMocks();
    }

    /**
     * Create a mocked configuration that produces a Session object that payment tests can run on
     */
    // TODO find a better name for this method
    void initializeValidGcSessionMocksAndSession() throws CommunicationException {
        initializeValidMocksAndSession();
        initializeValidGcSessionMocks();
    }

    /**
     * Create a mocked configuration that produces a Session object that payment tests can run on
     */
    void initializeValidGcSessionMocksAndSessionWithToken() throws CommunicationException {
        initializeValidMocksAndSessionWithToken();
        initializeValidGcSessionMocks();
    }

    /**
     * Create a mocked configuration that produces valid requests and contains an AccountOnFile
     */
    void initializeValidMocksAndSessionWithToken() throws CommunicationException {
        CreateTokenRequest body = GsonHelper.fromResourceJson("getTokenJSONCard.json", CreateTokenRequest.class);
        try {
            token = tokenUtil.createToken(testMerchantId, body);
        } catch (ApiException e) {
            throw new CommunicationException("ApiException while creating token. Token is: " + token, e);
        }
        initializeValidMocksAndSession();
    }

    /**
     * Creates a mocked configuration that produces valid requests
     */
    void initializeValidMocksAndSession() throws CommunicationException {
        SessionRequest request = new SessionRequest();
        try {
            SessionResponse response = sessionUtil.createSession(testMerchantId, request);
            initializeValidMocks(response);
        } catch (ApiException e) {
            throw new CommunicationException("ApiException while creating session", e);
        }
    }

    /**
     * Delete a token that may have been created for account on file testing
     */
    void deleteToken() {
        if (token != null) {
            tokenUtil.deleteToken(testMerchantId, token);
        }
    }

    /**
     * Delete a token that may have been created for account on file testing
     */
    void deleteToken(String token) {
        this.token = token;
        deleteToken();
    }

    /**
     * Creates a payment using the given request.
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return sessionUtil.createPayment(testMerchantId, createPaymentRequest);
    }

    private void initializeValidGcSessionMocks() {
        C2sCommunicator communicatorSpy = spy(communicator);
        Map<String, String> dummyMap = new HashMap<>();
        dummyMap.put("dummy", "dummy");
        dummyMap.put("mock", "mock");
        when(communicatorSpy.getMetadata(mockContext)).thenReturn(dummyMap);
        session = Session.getInstance(communicator);
        session.setClientSessionId(mockConfiguration.getClientSessionId());
    }

    private void initializeValidMocks(SessionResponse response) {
        createMockContext();
        createMockConfiguration(response);
        communicator = C2sCommunicator.getInstance(mockConfiguration);
    }

    private void initializeInvalidMocks() {
        createMockContext();
        createInvalidMockConfiguration();
        communicator = C2sCommunicator.getInstance(mockConfiguration);
    }

    private void createMockContext() {
        when(mockContext.getResources()).thenReturn(resources);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
    }

    private void createMockConfiguration(SessionResponse response) {
        String clientSessionId = response.getClientSessionId();
        String customerId = response.getCustomerId();

        when(mockConfiguration.getClientSessionId()).thenReturn(clientSessionId);
        when(mockConfiguration.getCustomerId()).thenReturn(customerId);
        when(mockConfiguration.getBaseUrl()).thenReturn(clientBaseUrl);
        when(mockConfiguration.getMetadata(mockContext)).thenReturn(null);
    }

    /**
     * Create an invalid Uri that will return an http response code other than 200.
     */
    private void createInvalidMockConfiguration() {
        when(mockConfiguration.getClientSessionId()).thenReturn("Invalid");
        when(mockConfiguration.getCustomerId()).thenReturn("Invalid");
        when(mockConfiguration.getBaseUrl()).thenReturn(clientBaseUrl);
        when(mockConfiguration.getMetadata(mockContext)).thenReturn(null);
    }

    /**
     * Validates that a BasicPaymentProduct has at least the minimal possible fields
     */
    void validateBasicPaymentProduct(BasicPaymentProduct bpp) {
        assertNotNull(bpp);
        assertNotNull(bpp.allowsRecurring());
        assertNotNull(bpp.allowsTokenization());
        assertNotNull(bpp.getId());
        assertNotNull(bpp.getDisplayHints());
        validateDisplayHintsPaymentItem(bpp.getDisplayHints());
    }

    /**
     * Validates that a PaymentProduct has at least the minimal possible fields
     */
    void validatePaymentProduct(PaymentProduct pp) {
        assertNotNull("PaymentProduct is null!" , pp);
        assertNotNull(pp.allowsRecurring());
        assertNotNull(pp.allowsTokenization());
        assertNotNull(pp.getId());
        assertNotNull(pp.getDisplayHints());
        validateDisplayHintsPaymentItem(pp.getDisplayHints());
//        validatePaymentProductFields(pp.getPaymentProductFields());
    }

    /*
     * Validates that a list of PaymentProductFields is not empty and that the first
     * element of the list at least contains the minimal possible fields
     */
    private void validatePaymentProductFields(List<PaymentProductField> ppfs) {
        assertNotNull(ppfs);
        assertNotNull(ppfs.get(0));
        PaymentProductField ppf = ppfs.get(0);
        assertNotNull(ppf.getId());
        assertNotNull(ppf.getDataRestrictions());
        assertNotNull(ppf.getDisplayHints());
        validateDisplayHintsProductFields(ppf.getDisplayHints());
    }

    /**
     * Validates that an AccountOnFile has at leas the minimal possible fields
     */
    void validateAccountOnFile(AccountOnFile aof) {
        assertNotNull(aof.getId());
        assertNotNull(aof.getPaymentProductId());
        assertNotNull(aof.getAttributes());
        assertNotNull(aof.getDisplayHints());
        validateDisplayHintsAccountOnFile(aof.getDisplayHints());
    }

    /**
     * Validates that a DisplayHintsProductFields object has at leas the minimal possible fields
     */
    private void validateDisplayHintsProductFields(DisplayHintsProductFields dhpf) {
        assertNotNull(dhpf.getDisplayOrder());
        assertNotNull(dhpf.getLabel());
    }

    /**
     * Validates that a DisplayHintsPaymentItem object has at least the minimal possible fields
     */
    private void validateDisplayHintsPaymentItem(DisplayHintsPaymentItem dhpi) {
        assertNotNull(dhpi.getLogoUrl());
        assertNotNull(dhpi.getDisplayOrder());
    }

    /**
     * Validates that a DisplayHintsAccountOnFile object has at least the minimal possible fields
     */
    private void validateDisplayHintsAccountOnFile(DisplayHintsAccountOnFile dhaof) {
        assertNotNull(dhaof.getLabelTemplate());
    }
}
