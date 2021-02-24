package com.ingenico.direct.sdk.client.android.integrationtest;

import com.ingenico.direct.domain.Card;
import com.ingenico.direct.domain.CardPaymentMethodSpecificInput;
import com.ingenico.direct.sdk.client.android.integrationtest.TestUtil.GsonHelper;
import com.ingenico.direct.sdk.client.android.exception.CommunicationException;
import com.ingenico.direct.sdk.client.android.model.PaymentRequest;
import com.ingenico.direct.sdk.client.android.model.PreparedPaymentRequest;
import com.ingenico.direct.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.ingenico.direct.sdk.client.android.session.SessionEncryptionHelper;
import com.ingenico.direct.domain.Address;
import com.ingenico.direct.domain.AmountOfMoney;
import com.ingenico.direct.domain.CreatePaymentRequest;
import com.ingenico.direct.domain.CreatePaymentResponse;
import com.ingenico.direct.domain.Customer;
import com.ingenico.direct.domain.Order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class that tests the AsyncTask of creating an Encrypted blob
 * Also tests whether a payment can be done with the created blob
 *
 * Copyright 2014 Global Collect Services B.V
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionPreparePaymentRequestTest extends BaseAsyncTaskTest {

    @Test
    public void testGetPreparedPaymentRequestWithValidRequest() throws InterruptedException, CommunicationException {

        initializeValidGcSessionMocksAndSession();

        CountDownLatch waitForAsyncCallBack = new CountDownLatch(1);

        PaymentRequest paymentRequest = constructValidPaymentRequest(false, false);

        Listener listener = new Listener(waitForAsyncCallBack);

        getSession().preparePaymentRequest(paymentRequest, getContext(), listener);

        // Test that the request is prepared within 'PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC' seconds
        assertTrue(waitForAsyncCallBack.await(PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC, TimeUnit.SECONDS));

        PreparedPaymentRequest preparedPaymentRequest = listener.getPreparedPaymentRequest();

        // Test that the encrypted blob that will be used for the payment has been created successfully
        validateValidPreparedPaymentRequest(preparedPaymentRequest);
    }

    @Test
    public void testGetPreparedPaymentRequestWithAccountOnFileAndValidRequest() throws InterruptedException {
        try {
            initializeValidGcSessionMocksAndSessionWithToken();

            CountDownLatch waitForAsyncCallBack = new CountDownLatch(1);

            PaymentRequest paymentRequest = constructValidPaymentRequest(true, false);

            Listener listener = new Listener(waitForAsyncCallBack);

            getSession().preparePaymentRequest(paymentRequest, getContext(), listener);

            // Test that the request is prepared within 'PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC' seconds
            assertTrue(waitForAsyncCallBack.await(PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC, TimeUnit.SECONDS));

            PreparedPaymentRequest preparedPaymentRequest = listener.getPreparedPaymentRequest();

            // Test that the encrypted blob that will be used for the payment has been created successfully
            validateValidPreparedPaymentRequest(preparedPaymentRequest);

        } catch (CommunicationException e) {
            e.printStackTrace();
        } finally {
            deleteToken();
        }
    }

    @Test
    public void testGetPreparedPaymentRequestWithCreatingTokenAndValidRequest() throws InterruptedException, CommunicationException {

        initializeValidGcSessionMocksAndSession();

        CountDownLatch waitForAsyncCallBack = new CountDownLatch(1);

        PaymentRequest paymentRequest = constructValidPaymentRequest(false, true);

        Listener listener = new Listener(waitForAsyncCallBack);

        getSession().preparePaymentRequest(paymentRequest, getContext(), listener);

        // Test that the request is prepared within 'PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC' seconds
        assertTrue(waitForAsyncCallBack.await(PREPAREPAYMENTREQUEST_CALLBACK_TEST_TIMEOUT_SEC, TimeUnit.SECONDS));

        PreparedPaymentRequest preparedPaymentRequest = listener.getPreparedPaymentRequest();

        // Test that the encrypted blob that will be used for the payment has been created successfully
        validateValidPreparedPaymentRequestTokenization(preparedPaymentRequest);
    }

    /**
     * Test that the encrypted blob that will be used for the payment has been created successfully
     */
    private void validateValidPreparedPaymentRequestTokenization(PreparedPaymentRequest preparedPaymentRequest) {
        assertNotNull(preparedPaymentRequest);
        assertNotNull(preparedPaymentRequest.getEncodedClientMetaInfo());
        assertNotNull(preparedPaymentRequest.getEncryptedFields());

        CreatePaymentRequest createPaymentRequest = constructCreatePaymentRequest(preparedPaymentRequest);
        CreatePaymentResponse response = createPayment(createPaymentRequest);

        assertNotNull(response);
        assertNotNull(response.getCreationOutput());
        assertNotNull(response.getCreationOutput().getToken());

        deleteToken(response.getCreationOutput().getToken());
    }

    /**
     * Test that the encrypted blob that will be used for the payment has been created successfully
     */
    private void validateValidPreparedPaymentRequest(PreparedPaymentRequest preparedPaymentRequest) {
        assertNotNull(preparedPaymentRequest);
        assertNotNull(preparedPaymentRequest.getEncodedClientMetaInfo());
        assertNotNull(preparedPaymentRequest.getEncryptedFields());

        CreatePaymentRequest createPaymentRequest = constructCreatePaymentRequest(preparedPaymentRequest);
        createPayment(createPaymentRequest);
    }

    /**
     * Creates a minimal CreatePaymentRequest object that can still be payed with
     */
    private CreatePaymentRequest constructCreatePaymentRequest(PreparedPaymentRequest preparedPaymentRequest) {
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest();
        AmountOfMoney amountOfMoney = new AmountOfMoney();
        amountOfMoney.setAmount(minimalValidPaymentContext.getAmountOfMoney().getAmount());
        amountOfMoney.setCurrencyCode("EUR");
        Address billingAddress = new Address();
        billingAddress.setCountryCode("NL");
        Customer customer = new Customer();
        customer.setBillingAddress(billingAddress);
        Order order = new Order();
        order.setAmountOfMoney(amountOfMoney);
        order.setCustomer(customer);
        createPaymentRequest.setOrder(order);
        Card card = new Card();
        card.setCardholderName("Voornaam Achternaam");
        CardPaymentMethodSpecificInput input = new CardPaymentMethodSpecificInput();
        input.setCard(card);
        createPaymentRequest.setCardPaymentMethodSpecificInput(input);

        // Set the encrypted blob in the request
        createPaymentRequest.setEncryptedCustomerInput(preparedPaymentRequest.getEncryptedFields());

        return createPaymentRequest;
    }

    private PaymentRequest constructValidPaymentRequest(boolean shouldHaveAccountOnFile, boolean tokenize) {
        PaymentProduct paymentProductVisa = GsonHelper.fromResourceJson("paymentProductVisa.json", PaymentProduct.class);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentProduct(paymentProductVisa);
        if (shouldHaveAccountOnFile) {
            paymentRequest.setAccountOnFile(paymentProductVisa.getAccountOnFileById("0"));
        } else {
            paymentRequest.setValue("cardNumber", "4567350000427977");
        }
        paymentRequest.setValue("cardholderName", "Bob");
        paymentRequest.setValue("expiryDate", "0824");
        paymentRequest.setValue("cvv", "123");

        paymentRequest.setTokenize(tokenize);

        assertTrue(paymentRequest.validate().isEmpty());

        return paymentRequest;
    }

    private class Listener implements SessionEncryptionHelper.OnPaymentRequestPreparedListener {

        private CountDownLatch signal;
        private PreparedPaymentRequest preparedPaymentRequest;

        Listener(CountDownLatch signal) {
            this.signal = signal;
        }

        public void onPaymentRequestPrepared(PreparedPaymentRequest preparedPaymentRequest) {
            this.preparedPaymentRequest = preparedPaymentRequest;
            signal.countDown();
        }

        PreparedPaymentRequest getPreparedPaymentRequest() {
            return preparedPaymentRequest;
        }
    }
}
