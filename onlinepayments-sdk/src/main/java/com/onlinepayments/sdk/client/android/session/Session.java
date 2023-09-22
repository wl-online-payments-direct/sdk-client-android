/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.session;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentItemsAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductNetworkAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentProductsAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.IinLookupAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.IinLookupAsyncTask.OnIinLookupCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.IinLookupAsyncTask.IinLookupCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask.OnPaymentProductCallCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentProductsAsyncTask.OnBasicPaymentProductsCallCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.SurchargeCalculationNetworkTask;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicatorConfiguration;
import com.onlinepayments.sdk.client.android.model.AmountOfMoney;
import com.onlinepayments.sdk.client.android.exception.EncryptDataException;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener;
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener;
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener;
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener;
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentItemCacheKey;
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiErrorItem;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.model.surcharge.request.Card;
import com.onlinepayments.sdk.client.android.model.surcharge.request.CardSource;
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session contains all methods needed for making a payment.
 */
public class Session implements
        Serializable,
        OnBasicPaymentProductsCallCompleteListener,
        OnIinLookupCompleteListener,
        OnPaymentProductCallCompleteListener,
        BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener,
        BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener,
        BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener,
        PaymentProductAsyncTask.PaymentProductCallListener,
        IinLookupCompleteListener {

    private static final long serialVersionUID = 686891053207055508L;

    // Cache which contains all payment products that are loaded from the Online Payments gateway
    private Map<PaymentItemCacheKey, BasicPaymentItem> basicPaymentItemMapping = new HashMap<>();
    private Map<PaymentItemCacheKey, PaymentItem> paymentItemMapping = new HashMap<>();

    // Communicator used for communicating with the Online Payments gateway
    private C2sCommunicator communicator;

    // PaymentContext which contains all necessary data for making a call to the Online Payments gateway to retrieve payment products
    private PaymentContext paymentContext;

    // Flag to determine if the iinlookup is being executed,
    // so it won't be fired every time a character is typed in the edittext while another call is being executed
    private Boolean iinLookupPending = false;

    // Used for identifying the customer on the Online Payments gateway
    private String clientSessionId;


    private Session(C2sCommunicator communicator) {
        this.communicator = communicator;
    }

    /**
     * Creates a Session object. Use this object to perform Client API requests such as get Payment Products or get IIN Details.
     *
     * @param clientSessionId used for identifying the session on the Online Payments gateway
     * @param customerId used for identifying the customer on the Online Payments gateway
     * @param clientApiUrl the endpoint baseurl
     * @param assetBaseUrl the asset baseurl
     * @param environmentIsProduction states if the environment is production
     * @param appIdentifier used to create device metadata
     *
     */
    public Session(String clientSessionId, String customerId, String clientApiUrl, String assetBaseUrl, boolean environmentIsProduction, String appIdentifier) {
        initSession(clientSessionId, customerId, clientApiUrl, assetBaseUrl, environmentIsProduction, appIdentifier, false);
    }

    /**
     * Creates a Session object. Use this object to perform Client API requests such as get Payment Products or get IIN Details.
     *
     * @param clientSessionId used for identifying the session on the Online Payments gateway
     * @param customerId used for identifying the customer on the Online Payments gateway
     * @param clientApiUrl the endpoint baseurl
     * @param assetBaseUrl the asset baseurl
     * @param environmentIsProduction states if the environment is production
     * @param appIdentifier used to create device metadata
     * @param loggingEnabled indicates whether requests and responses should be logged to the console; default is false; should be false in production
     *
     */
    public Session(String clientSessionId, String customerId, String clientApiUrl, String assetBaseUrl, boolean environmentIsProduction, String appIdentifier, boolean loggingEnabled) {
        initSession(clientSessionId, customerId, clientApiUrl, assetBaseUrl, environmentIsProduction, appIdentifier, loggingEnabled);
    }

    private void initSession(String clientSessionId, String customerId, String clientApiUrl, String assetBaseUrl, boolean environmentIsProduction, String appIdentifier, boolean loggingEnabled) {
        C2sCommunicatorConfiguration configuration = new C2sCommunicatorConfiguration(clientSessionId, customerId, clientApiUrl, assetBaseUrl, environmentIsProduction, appIdentifier, null, loggingEnabled);

        this.communicator = C2sCommunicator.getInstance(configuration);
        this.setClientSessionId(clientSessionId);
    }

    /**
     * Gets instance of the Session.
     *
     * @param communicator used for communicating with the Online Payments gateway
     *
     * @return {@link Session} singleton instance
     *
     * @deprecated Create a new {@link Session} instead.
     */
    @Deprecated
    public static Session getInstance(C2sCommunicator communicator) {
        if (communicator == null) {
            throw new InvalidParameterException("Error creating Session instance, communicator may not be null");
        }
        return new Session(communicator);
    }


    /**
     * Checks whether the EnvironmentType is set to production or not.
     *
     * @return a Boolean indicating whether the EnvironmentType is set to production or not
     *
     * @deprecated In a future release, this function will become internal to the SDK.
     */
    @Deprecated
    public boolean isEnvironmentTypeProduction() {
        return communicator.isEnvironmentTypeProduction();
    }

    /**
     * Returns the asset base URL that was used to create the Session.
     *
     * @return the asset base URL
     *
     * @deprecated In a future release, this function will become internal to the SDK.
     */
    @Deprecated
    public String getAssetUrl() {
        return communicator.getAssetUrl();
    }

    /**
     * Gets all {@link BasicPaymentItems} for a given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener} that will be called when the {@link BasicPaymentItems} are retrieved
     * @param groupPaymentProducts a Boolean that controls whether the getBasicPaymentItems call will group the retrieved {@link BasicPaymentItems}; true for grouping, false otherwise
     *
     * @deprecated use {@link #getBasicPaymentItems(Context, PaymentContext, boolean, BasicPaymentItemsResponseListener)} instead.
     */
    @Deprecated
    public void getBasicPaymentItems(Context context, PaymentContext paymentContext, BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener listener, boolean groupPaymentProducts) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PaymentItems", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentItemsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentItemsAsyncTask task = new BasicPaymentItemsAsyncTask(context, paymentContext, communicator, listeners, false);
        task.execute();
    }

    /**
     * Gets all {@link BasicPaymentItems} for a given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener} that will be called when the {@link BasicPaymentItems} are retrieved
     * @param groupPaymentProducts a Boolean that controls whether the getBasicPaymentItems call will group the retrieved {@link BasicPaymentItems}; true for grouping, false otherwise
     *
     * @deprecated use {@link #getBasicPaymentItems(Context, PaymentContext, boolean, BasicPaymentItemsResponseListener)} instead.
     */
    @Deprecated
    public void getBasicPaymentItems(Context context, PaymentContext paymentContext, BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener listener, boolean groupPaymentProducts) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentItems", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentItemsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentItemsAsyncTask task = new BasicPaymentItemsAsyncTask(context, paymentContext, communicator, false, listeners);
        task.execute();
    }

    /**
     * Gets all {@link BasicPaymentItems} for a given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param groupPaymentProducts a Boolean that controls whether the getBasicPaymentItems call will group the retrieved {@link BasicPaymentItems}; true for grouping, false otherwise
     * @param listener {@link com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener} that will be called when the {@link BasicPaymentItems} are retrieved
     */
    public void getBasicPaymentItems(Context context, PaymentContext paymentContext, boolean groupPaymentProducts, BasicPaymentItemsResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentItems", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentItemsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentItemsResponseListener> listeners = new ArrayList<>();

        listeners.add(new BasicPaymentItemsResponseListener() {
            @Override
            public void onSuccess(@NonNull BasicPaymentItems response) {
                // Store the loaded basicPaymentItems in the cache
                for (BasicPaymentItem basicPaymentItem : response.getBasicPaymentItems()) {
                    cacheBasicPaymentItem(basicPaymentItem);
                }
            }

            @Override
            public void onApiError(ErrorResponse error) {
                Session.this.onApiError("BasicPaymentItems", error);
            }

            @Override
            public void onException(Throwable t) {
                Session.this.onApiException("BasicPaymentItems", t);
            }
        });

        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentItemsAsyncTask task = new BasicPaymentItemsAsyncTask(context, paymentContext, false, listeners, communicator);
        task.execute();
    }


    /**
     * Gets {@link BasicPaymentProducts} for the given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
     * @param listener {@link OnBasicPaymentProductsCallCompleteListener} which will be called when the {@link BasicPaymentProducts} are retrieved
     *
     * @deprecated Use {@link #getBasicPaymentProducts(Context, PaymentContext, BasicPaymentProductsResponseListener)} instead.
     */
    @Deprecated
    public void getBasicPaymentProducts(Context context, PaymentContext paymentContext, OnBasicPaymentProductsCallCompleteListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentProducts", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentProductsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<OnBasicPaymentProductsCallCompleteListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentProductsAsyncTask task = new BasicPaymentProductsAsyncTask(context, paymentContext, communicator, listeners);
        task.execute();
    }


    /**
     * Gets {@link BasicPaymentProducts} for the given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener} which will be called when the {@link BasicPaymentProducts} are retrieved
     * @deprecated Use {@link #getBasicPaymentProducts(Context, PaymentContext, BasicPaymentProductsResponseListener)} instead.
     */
    @Deprecated
    public void getBasicPaymentProducts(Context context, PaymentContext paymentContext, BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentProducts", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentProductsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentProductsAsyncTask task = new BasicPaymentProductsAsyncTask(context, communicator, paymentContext, listeners);
        task.execute();
    }

    /**
     * Gets {@link BasicPaymentProducts} for the given {@link PaymentContext}.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
     * @param listener {@link com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener} which will be called when the {@link BasicPaymentProducts} are retrieved
     */
    public void getBasicPaymentProducts(Context context, PaymentContext paymentContext, BasicPaymentProductsResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentProducts", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentProductsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentProductsResponseListener> listeners = new ArrayList<>();
        listeners.add(new BasicPaymentProductsResponseListener() {
            @Override
            public void onSuccess(@NonNull BasicPaymentProducts response) {
                // Store the loaded basicPaymentProducts in the cache
                for (BasicPaymentProduct paymentProduct : response.getBasicPaymentProducts()) {
                    cacheBasicPaymentItem(paymentProduct);
                }
            }

            @Override
            public void onApiError(ErrorResponse error) {
                Session.this.onApiError("BasicPaymentItems", error);
            }

            @Override
            public void onException(Throwable t) {
                Session.this.onApiException("BasicPaymentItems", t);
            }
        });
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentProductsAsyncTask task = new BasicPaymentProductsAsyncTask(communicator, context, paymentContext, listeners);
        task.execute();
    }


    /**
     * Gets {@link PaymentProduct} with fields by product id.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param productId the productId of the {@link PaymentProduct} which needs to be retrieved from the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProduct}
     * @param listener {@link OnPaymentProductCallCompleteListener} that will be called when the {@link PaymentProduct} with fields is retrieved
     *
     * @deprecated use {@link #getPaymentProduct(Context, String, PaymentContext, PaymentProductResponseListener)} instead.
     */
    @Deprecated
    public void getPaymentProduct(Context context, String productId, PaymentContext paymentContext, OnPaymentProductCallCompleteListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        objectsToCheck.put("productId", productId);
        nullCheck("PaymentProduct", objectsToCheck);

        this.paymentContext = paymentContext;

        // Create the cache key for this paymentProduct
        PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, productId);

        // If the paymentProduct is already in the cache, call the listener with that paymentproduct
        if (paymentItemMapping.containsKey(key)) {
            PaymentProduct cachedPP = (PaymentProduct) paymentItemMapping.get(key);
            listener.onPaymentProductCallComplete(cachedPP);
        } else {

            // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can store the paymentproduct here
            List<OnPaymentProductCallCompleteListener> listeners = new ArrayList<>();
            listeners.add(this);
            listeners.add(listener);

            // Make the call to the Online Payments gateway
            PaymentProductAsyncTask task = new PaymentProductAsyncTask(context, productId, paymentContext, communicator, listeners);
            task.execute();
        }
    }

    /**
     * Gets {@link PaymentProduct} with fields by product id.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param productId the productId of the {@link PaymentProduct} which needs to be retrieved from the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProduct}
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask.PaymentProductCallListener} that will be called when the {@link PaymentProduct} with fields is retrieved
     *
     * @deprecated use {@link #getPaymentProduct(Context, String, PaymentContext, PaymentProductResponseListener)} instead.
     */
    @Deprecated
    public void getPaymentProduct(Context context, String productId, PaymentContext paymentContext, PaymentProductAsyncTask.PaymentProductCallListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        objectsToCheck.put("productId", productId);
        nullCheck("PaymentProduct", objectsToCheck);

        this.paymentContext = paymentContext;

        // Create the cache key for this paymentProduct
        PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, productId);

        // If the paymentProduct is already in the cache, call the listener with that paymentproduct
        if (paymentItemMapping.containsKey(key)) {
            PaymentProduct cachedPP = (PaymentProduct) paymentItemMapping.get(key);
            listener.onPaymentProductCallComplete(cachedPP);
        } else {

            // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can store the paymentproduct here
            List<PaymentProductAsyncTask.PaymentProductCallListener> listeners = new ArrayList<>();
            listeners.add(this);
            listeners.add(listener);

            // Make the call to the Online Payments gateway
            PaymentProductAsyncTask task = new PaymentProductAsyncTask(context, productId, communicator, paymentContext, listeners);
            task.execute();
        }
    }

    /**
     * Gets {@link PaymentProduct} with fields by product id.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param productId the productId of the {@link PaymentProduct} which needs to be retrieved from the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProduct}
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask.PaymentProductCallListener} that will be called when the {@link PaymentProduct} with fields is retrieved
     */
    public void getPaymentProduct(Context context, String productId, PaymentContext paymentContext, PaymentProductResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        objectsToCheck.put("productId", productId);
        nullCheck("PaymentProduct", objectsToCheck);

        this.paymentContext = paymentContext;

        // Create the cache key for this paymentProduct
        PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, productId);

        // If the paymentProduct is already in the cache, call the listener with that paymentproduct
        if (paymentItemMapping.containsKey(key)) {
            PaymentProduct cachedPP = (PaymentProduct) paymentItemMapping.get(key);
            listener.onSuccess(cachedPP);
        } else {

            // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can store the paymentproduct here
            List<PaymentProductResponseListener> listeners = new ArrayList<>();
            listeners.add(new PaymentProductResponseListener() {
                @Override
                public void onSuccess(@NonNull PaymentProduct response) {
                    // Store the loaded paymentProduct in the cache
                    cachePaymentItem(response);
                }

                @Override
                public void onApiError(ErrorResponse error) {
                    Session.this.onApiError("PaymentProduct", error);
                }

                @Override
                public void onException(Throwable t) {
                    Session.this.onApiException("PaymentProduct", t);
                }
            });

            listeners.add(listener);

            // Make the call to the Online Payments gateway
            PaymentProductAsyncTask task = new PaymentProductAsyncTask(context, communicator, productId, paymentContext, listeners);
            task.execute();
        }
    }

    /**
     * Gets {@link PaymentProductNetworkResponse} from the Online Payments gateway.
     *
     * @param productId the product of the id for which the network must be retrieved
     * @param customerId for which customer the network must be retrieved.
     * @param countryCode for which {@link CountryCode} the network must be retrieved.
     * @param currencyCode for which {@link CurrencyCode} the network must be retrieved.
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link PaymentProductNetworkAsyncTask.PaymentProductNetworkListener} that will be called when the {@link PaymentProductNetworkResponse} is retrieved
     *
     * @deprecated use {@link #getNetworksForPaymentProduct(String, Context, PaymentContext, PaymentProductNetworkResponseListener)} instead.
     */
    @Deprecated
    public void getNetworkForCustomerAndPaymentProductId(String customerId, String productId, CountryCode countryCode, CurrencyCode currencyCode, Context context, PaymentContext paymentContext, PaymentProductNetworkAsyncTask.PaymentProductNetworkListener listener) {
        getNetworkForCustomerAndPaymentProductId(customerId, productId, countryCode.toString(), currencyCode.toString(), context, paymentContext, listener);
    }

    /**
     * Gets {@link PaymentProductNetworkResponse} from the Online Payments gateway.
     *
     * @param productId the product of the id for which the network must be retrieved
     * @param customerId for which customer the network must be retrieved.
     * @param currencyCode for which currencyCode the network must be retrieved.
     * @param countryCode for which countryCode the network must be retrieved.
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link PaymentProductNetworkAsyncTask.PaymentProductNetworkListener} that will be called when the {@link PaymentProductNetworkResponse} is retrieved
     *
     * @deprecated use {@link #getNetworksForPaymentProduct(String, Context, PaymentContext, PaymentProductNetworkResponseListener)} instead.
     */
    @Deprecated
    public void getNetworkForCustomerAndPaymentProductId(String customerId, String productId, String countryCode, String currencyCode, Context context, PaymentContext paymentContext, PaymentProductNetworkAsyncTask.PaymentProductNetworkListener listener) {

        Map<String, Object> objectToCheck = new HashMap();
        objectToCheck.put("context", context);
        objectToCheck.put("listener", listener);
        objectToCheck.put("productId", productId);
        objectToCheck.put("customerId", customerId);
        objectToCheck.put("countryCode", countryCode);
        objectToCheck.put("currencyCode", currencyCode);
        objectToCheck.put("paymentContext", paymentContext);

        PaymentProductNetworkAsyncTask task = new PaymentProductNetworkAsyncTask(productId, customerId, currencyCode, countryCode, communicator, context, paymentContext, listener);
        task.execute();
    }

    /**
     * Gets {@link PaymentProductNetworkResponse} from the Online Payments gateway.
     *
     * @param productId the product of the id for which the network must be retrieved
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for making a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener} that will be called when the {@link PaymentProductNetworkResponse} is retrieved
     */
    public void getNetworksForPaymentProduct(String productId, Context context, PaymentContext paymentContext, PaymentProductNetworkResponseListener listener) {

        Map<String, Object> objectToCheck = new HashMap();
        objectToCheck.put("context", context);
        objectToCheck.put("listener", listener);
        objectToCheck.put("productId", productId);
        objectToCheck.put("countryCode", paymentContext.getCountryCodeString());
        objectToCheck.put("currencyCode", paymentContext.getAmountOfMoney().getCurrencyCodeString());
        objectToCheck.put("paymentContext", paymentContext);

        PaymentProductNetworkAsyncTask task = new PaymentProductNetworkAsyncTask(productId, communicator, context, paymentContext, listener);
        task.execute();
    }

    /**
     * Gets the IinDetails as a {@link IinDetailsResponse} for a given partial credit card number.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param partialCreditCardNumber entered partial credit card number for which the {@link IinDetailsResponse} will be retrieved
     * @param listener {@link OnIinLookupCompleteListener} that will be called when the {@link IinDetailsResponse} is retrieved
     * @param paymentContext {@link PaymentContext} for which the {@link IinDetailsResponse} will be retrieved
     *
     * @deprecated use {@link #getIinDetails(Context, String, IinLookupResponseListener, PaymentContext)} instead.
     */
    @Deprecated
    public void getIinDetails(Context context, String partialCreditCardNumber, OnIinLookupCompleteListener listener, PaymentContext paymentContext) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("partialCreditCardNumber", partialCreditCardNumber);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("IinDetails", objectsToCheck);

        // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can reset the iinLookupPending flag
        List<OnIinLookupCompleteListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        if (!iinLookupPending) {

            IinLookupAsyncTask task = new IinLookupAsyncTask(context, partialCreditCardNumber, communicator, listeners, paymentContext);
            task.execute();

            iinLookupPending = true;
        }
    }

    /**
     * Gets the IinDetails as a {@link IinDetailsResponse} for a given partial credit card number.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param partialCreditCardNumber entered partial credit card number for which the {@link IinDetailsResponse} will be retrieved
     * @param listener {@link IinLookupCompleteListener} that will be called when the {@link IinDetailsResponse} is retrieved
     * @param paymentContext {@link PaymentContext} for which the {@link IinDetailsResponse} will be retrieved
     *
     * @deprecated use {@link #getIinDetails(Context, String, IinLookupResponseListener, PaymentContext)} instead.
     */
    @Deprecated
    public void getIinDetails(Context context, String partialCreditCardNumber, IinLookupCompleteListener listener, PaymentContext paymentContext) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("partialCreditCardNumber", partialCreditCardNumber);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("IinDetails", objectsToCheck);

        // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can reset the iinLookupPending flag
        List<IinLookupCompleteListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        if (!iinLookupPending) {

            IinLookupAsyncTask task = new IinLookupAsyncTask(context, partialCreditCardNumber, communicator, paymentContext, listeners);
            task.execute();

            iinLookupPending = true;
        }
    }

    /**
     * Gets the IinDetails as a {@link IinDetailsResponse} for a given partial credit card number.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param partialCreditCardNumber entered partial credit card number for which the {@link IinDetailsResponse} will be retrieved
     * @param listener {@link com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener} that will be called when the {@link IinDetailsResponse} is retrieved
     * @param paymentContext {@link PaymentContext} for which the {@link IinDetailsResponse} will be retrieved
     */
    public void getIinDetails(Context context, String partialCreditCardNumber, IinLookupResponseListener listener, PaymentContext paymentContext) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("partialCreditCardNumber", partialCreditCardNumber);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("IinDetails", objectsToCheck);

        // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can reset the iinLookupPending flag
        List<IinLookupResponseListener> listeners = new ArrayList<>();
        listeners.add(new IinLookupResponseListener() {
            @Override
            public void onSuccess(@NonNull IinDetailsResponse response) {
                iinLookupPending = false;
            }

            @Override
            public void onApiError(ErrorResponse error) {
                iinLookupPending = false;
                Session.this.onApiError("IinDetails", error);
            }

            @Override
            public void onException(Throwable t) {
                iinLookupPending = false;
                Session.this.onApiException("IinDetails", t);
            }
        });

        listeners.add(listener);

        if (!iinLookupPending) {

            IinLookupAsyncTask task = new IinLookupAsyncTask(context, communicator, partialCreditCardNumber, paymentContext, listeners);
            task.execute();

            iinLookupPending = true;
        }
    }

    /**
     * Retrieves the public key as a {@link com.onlinepayments.sdk.client.android.model.PublicKeyResponse} from the Online Payments gateway.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask.OnPublicKeyLoadedListener} that will be called when the {@link com.onlinepayments.sdk.client.android.model.PublicKeyResponse} is retrieved
     *
     * @deprecated use {@link #getPublicKey(Context, PublicKeyResponseListener)} instead.
     */
    @Deprecated
    public void getPublicKey(Context context, PublicKeyAsyncTask.OnPublicKeyLoadedListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PublicKey", objectsToCheck);

        PublicKeyAsyncTask task = new PublicKeyAsyncTask(context, communicator, listener);
        task.execute();
    }

    /**
     * Retrieves the public key as a {@link com.onlinepayments.sdk.client.android.model.PublicKeyResponse} from the Online Payments gateway.
     *
     * @param context  used for reading device metadata which is sent to the Online Payments gateway
     * @param listener {@link com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask.PublicKeyListener} that will be called when the {@link com.onlinepayments.sdk.client.android.model.PublicKeyResponse} is retrieved
     *
     * @deprecated use {@link #getPublicKey(Context, PublicKeyResponseListener)} instead.
     */
    @Deprecated
    public void getPublicKey(Context context, PublicKeyAsyncTask.PublicKeyListener listener) {

        Map<String, Object> objectsToCheck = new HashMap<>();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PublicKey", objectsToCheck);

        PublicKeyAsyncTask task = new PublicKeyAsyncTask(context, communicator, listener);
        task.execute();
    }

    /**
     * Retrieves the public key as a {@link com.onlinepayments.sdk.client.android.model.PublicKeyResponse} from the the Client API.
     * The key will be returned through the provided listener's onSuccess() method. In case of an error or exception, onError or onException will be invoked.
     *
     * @param context Used for reading device metadata.
     * @param listener The listener interface that will be invoked when the request completes.
     */
    public void getPublicKey(Context context, PublicKeyResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap<>();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PublicKey", objectsToCheck);

        PublicKeyAsyncTask task = new PublicKeyAsyncTask(context, communicator, listener);
        task.execute();
    }


    /**
     * Prepares a {@link com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest} from the supplied {@link PaymentRequest}.
     *
     * @param paymentRequest the {@link PaymentRequest} which contains all values for all fields
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param listener {@link SessionEncryptionHelper.OnPaymentRequestPreparedListener} that will be called when the {@link com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest} is created
     *
     * @deprecated Use {@link #preparePaymentRequest(PaymentRequest, Context, PaymentRequestPreparedListener)} instead.
     */
    @Deprecated
    public void preparePaymentRequest(PaymentRequest paymentRequest, Context context, SessionEncryptionHelper.OnPaymentRequestPreparedListener listener) {

        Map<String, Object> objectsToCheck = new HashMap<>();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentRequest", paymentRequest);
        nullCheck("preparing payment request", objectsToCheck);

        Map<String, String> metaData = communicator.getMetadata(context);
        SessionEncryptionHelper sessionEncryptionHelper = new SessionEncryptionHelper(paymentRequest, clientSessionId, metaData, listener);

        // Execute the getPublicKey, which will trigger the listener in the SessionEncryptionHelper
        getPublicKey(context, new PublicKeyResponseListener() {
            @Override
            public void onSuccess(@NonNull PublicKeyResponse response) {
                sessionEncryptionHelper.onPublicKeyReceived(response);
            }

            @Override
            public void onApiError(ErrorResponse error) {
                Session.this.onApiError("PublicKey", error);
            }

            @Override
            public void onException(Throwable t) {
                Session.this.onApiException("PublicKey", t);
            }
        });
    }


    /**
     * Prepares a {@link com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest} from the supplied {@link PaymentRequest}.
     *
     * @param paymentRequest the {@link PaymentRequest} which contains all values for all fields
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param listener {@link PaymentRequestPreparedListener} that will be called when the {@link com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest} is created
     */
    public void preparePaymentRequest(PaymentRequest paymentRequest, Context context, PaymentRequestPreparedListener listener) {

        Map<String, Object> objectsToCheck = new HashMap<>();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentRequest", paymentRequest);
        nullCheck("preparing payment request", objectsToCheck);

        Map<String, String> metaData = communicator.getMetadata(context);
        SessionEncryptionHelper sessionEncryptionHelper = new SessionEncryptionHelper(paymentRequest, clientSessionId, metaData, listener);

        // Execute the getPublicKey, which will trigger the listener in the SessionEncryptionHelper
        getPublicKey(context, new PublicKeyResponseListener() {
            @Override
            public void onSuccess(@NonNull PublicKeyResponse response) {
                sessionEncryptionHelper.onPublicKeyReceived(response);
            }

            @Override
            public void onApiError(ErrorResponse error) {
                Session.this.onApiError("PublicKey", error);
                listener.onFailure(new EncryptDataException(error.message));
            }

            @Override
            public void onException(Throwable t) {
                Session.this.onApiException("PublicKey", t);
                listener.onFailure(new EncryptDataException("Exception while retrieving Public Key", t));
            }
        });
    }

    /**
     * Retrieves the Surcharge Calculation as a {@link SurchargeCalculationResponse} for the provided amount of money, partial credit card number and payment product ID.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
     * @param partialCreditCardNumber the partial credit card number for which the Surcharge should be calculated
     * @param paymentProductId the id of the product for which the Surcharge product type should be determined, can be null
     * @param listener {@link SurchargeCalculationResponseListener} that will be called when the {@link SurchargeCalculationResponse} is retrieved
     */
    public void getSurchargeCalculation(Context context, AmountOfMoney amountOfMoney, String partialCreditCardNumber, Integer paymentProductId, SurchargeCalculationResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("amountOfMoney", amountOfMoney);
        objectsToCheck.put("partialCreditCardNumber", partialCreditCardNumber);
        nullCheck("SurchargeCalculation", objectsToCheck);

        Card card = new Card(partialCreditCardNumber, paymentProductId);
        CardSource cardSource = new CardSource(card);

        SurchargeCalculationNetworkTask task = new SurchargeCalculationNetworkTask(context, amountOfMoney, cardSource, communicator, listener);
        task.getSurchargeCalculation();
    }

    /**
     * Retrieves the Surcharge Calculation as a {@link SurchargeCalculationResponse} for the provided amount of money and token.
     *
     * @param context used for reading device metadata which is sent to the Online Payments gateway
     * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
     * @param token the token for which the Surcharge should be calculated
     * @param listener {@link SurchargeCalculationResponseListener} that will be called when the {@link SurchargeCalculationResponse} is retrieved
     */
    public void getSurchargeCalculation(Context context, AmountOfMoney amountOfMoney, String token, SurchargeCalculationResponseListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("amountOfMoney", amountOfMoney);
        objectsToCheck.put("token", token);
        nullCheck("SurchargeCalculation", objectsToCheck);

        CardSource cardSource = new CardSource(token);

        SurchargeCalculationNetworkTask task = new SurchargeCalculationNetworkTask(context, amountOfMoney, cardSource, communicator, listener);
        task.getSurchargeCalculation();
    }

    /**
     * Utility method for setting clientSessionId.
     *
     * @param clientSessionId the client session id which should be set
     *
     * @deprecated In a future release, this function will become internal to the SDK. To start a new payment, create a new Session object with new client session details.
     */
    @Deprecated
    public void setClientSessionId(String clientSessionId) {

        if (clientSessionId == null) {
            throw new InvalidParameterException("Error setting clientSessionId, clientSessionId may not be null");
        }

        this.clientSessionId = clientSessionId;
    }

    /**
     * Utility method for getting clientSessionId.
     *
     * @return the client session id of the current session
     *
     * @deprecated In a future release, this function will become internal to the SDK.
     */
    @Deprecated
    public String getClientSessionId() {
        return clientSessionId;
    }

    /**
     * Utility method for setting whether request/response logging should be enabled or not.
     *
     * @param enableLogging boolean indicating whether request/response logging should be enabled or not
     */
    public void setLoggingEnabled(boolean enableLogging) {
        communicator.setLoggingEnabled(enableLogging);
    }

    /**
     * Checks whether request/response logging is enabled or not.
     *
     * @return a boolean indicating whether request/response logging is enabled or not
     */
    public boolean getLoggingEnabled() {
        return communicator.getLoggingEnabled();
    }

    private PaymentItemCacheKey createPaymentItemCacheKey(PaymentContext paymentContext, String paymentItemId) {
        // Create the cache key for this retrieved BasicPaymentitem
        return new PaymentItemCacheKey(paymentContext.getAmountOfMoney().getAmount(),
                paymentContext.getCountryCodeString(),
                paymentContext.getAmountOfMoney().getCurrencyCodeString(),
                paymentContext.isRecurring(),
                paymentItemId);
    }

    private void cacheBasicPaymentItem(BasicPaymentItem basicPaymentItem) {
        // Add basicPaymentItem to the basicPaymentItemMapping cache
        if (basicPaymentItem != null) {

            // Create the cache key for and put it in the cache
            PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, basicPaymentItem.getId());
            basicPaymentItemMapping.put(key, basicPaymentItem);
        }
    }

    private void cachePaymentItem(PaymentItem paymentItem) {
        // Add paymentItem to the paymentItemMapping cache
        if (paymentItem != null) {

            // Create the cache key for this retrieved PaymentItem
            PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, paymentItem.getId());
            paymentItemMapping.put(key, paymentItem);
        }
    }

    private void nullCheck(String methodName, Map<String, Object> objectsToCheck) {
        for (Map.Entry<String, Object> objectToCheck : objectsToCheck.entrySet()) {
            if (objectToCheck.getValue() == null) {
                throw new InvalidParameterException("Error getting " + methodName + ", " + objectToCheck.getKey() + " may not be null");
            }
        }
    }

    /**
     * Listener for retrieved {@link BasicPaymentProducts} from the Online Payments gateway.
     */
    @Override
    public void onBasicPaymentProductsCallComplete(BasicPaymentProducts basicPaymentProducts) {
        cacheBasicPaymentProducts(basicPaymentProducts);
    }

    @Override
    public void onBasicPaymentProductsCallError(ErrorResponse error) {
        // Not needed for the Session object, leave it to the external listener to act upon
    }

    /**
     * Listener for retrieved {@link PaymentProduct} from the Online Payments gateway.
     */
    @Override
    public void onPaymentProductCallComplete(PaymentProduct paymentProduct) {
        cachePaymentItem(paymentProduct);
    }

    @Override
    public void onPaymentProductCallError(ErrorResponse error) {
        // Not needed for the Session object, leave it to the external listener to act upon
        // Unless we want to invalidate the cache?
    }

    /**
     * Listener for retrieved {@link BasicPaymentItems} from the Online Payments gateway.
     */
    @Override
    public void onBasicPaymentItemsCallComplete(BasicPaymentItems basicPaymentItems) {
        cacheBasicPaymentItems(basicPaymentItems);
    }

    /**
     * Listener for when retrieving {@link BasicPaymentItems} failed.
     */
    @Override
    public void onBasicPaymentItemsCallError(ErrorResponse error) {
        // No need for caching errors so let it slip...
    }

    /**
     * Listener for retrieved {@link IinDetailsResponse} from the Online Payments gateway.
     */
    @Override
    public void onIinLookupComplete(IinDetailsResponse response) {
        // Store the loaded basicPaymentProducts in the cache
        iinLookupPending = false;
    }

    @Override
    public void onIinLookupError(ErrorResponse error) {
        // Not needed for the Session object, leave it to the external listener to act upon
    }

    public void cacheBasicPaymentItems(BasicPaymentItems basicPaymentItems) {
        // Store the loaded basicPaymentItems in the cache
        for (BasicPaymentItem basicPaymentItem : basicPaymentItems.getBasicPaymentItems()) {
            cacheBasicPaymentItem(basicPaymentItem);
        }
    }

    public void cacheBasicPaymentProducts(BasicPaymentProducts basicPaymentProducts) {
        // Store the loaded basicPaymentProducts in the cache
        for (BasicPaymentProduct paymentProduct : basicPaymentProducts.getBasicPaymentProducts()) {
            cacheBasicPaymentItem(paymentProduct);
        }
    }

    private void onApiError(String logTag, ErrorResponse error) {
        if (getLoggingEnabled()) {
            String apiErrorId = (error.apiError == null) ? "" : error.apiError.errorId;
            String apiErrorList = (error.apiError == null) ? "" : getApiErrorItemList(error.apiError.errors);
            Log.e("LocalResponseListener", "API Error while performing API call for : " + logTag + "\n ErrorResponse \nmessage : " + error.message + "\napiError id: " + apiErrorId + "\nerrorList: " + apiErrorList);
        } else {
            Log.e("LocalResponseListener", "API Error while performing API call for : " + logTag + "\n ErrorResponse message : " + error.message);
        }
    }

    private void onApiException(String logTag, Throwable t) {
        if (getLoggingEnabled()) {
            Log.e("LocalResponseListener", "Exception while performing API call for : " + logTag + "\nException: " + t.getMessage(), t);
        } else {
            Log.e("LocalResponseListener", "Exception while performing API call for : " + logTag);
        }
    }

    private String getApiErrorItemList(List<ApiErrorItem> apiErrorItems) {
        StringBuilder errorList = new StringBuilder();
        if (apiErrorItems.isEmpty()) {
            return "";
        }
        for (int i = 0; i < apiErrorItems.size(); i++) {
            ApiErrorItem errorItem = apiErrorItems.get(i);
            errorList.append("ApiErrorItem code: ").append(errorItem.code).append("\nmessage: ").append(errorItem.message);
        }
        return errorList.toString();
    }
}
