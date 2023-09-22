/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.communicate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.onlinepayments.sdk.client.android.model.AmountOfMoney;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiError;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.Util;
import com.onlinepayments.sdk.client.android.exception.CommunicationException;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsRequest;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.google.gson.Gson;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.model.paymentproduct.Tooltip;
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields;
import com.onlinepayments.sdk.client.android.model.surcharge.request.CardSource;
import com.onlinepayments.sdk.client.android.model.surcharge.request.SurchargeCalculationRequest;
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Handles all communication with the Online Payments Client API.
 *
 * @deprecated In a future release, this class, its functions and its properties will become internal to the SDK.
 */
@Deprecated
public class C2sCommunicator implements Serializable {


	private static final long serialVersionUID = 1780234270110278059L;

	// Tag for logging
	private static final String TAG = C2sCommunicator.class.getName();

	private static final Gson gson = new Gson();

	// Strings used for adding headers to requests
	private static final String HTTP_HEADER_SESSION_ID = "Authorization";
	private static final String HTTP_HEADER_METADATA = "X-GCS-ClientMetaInfo";

	// Maximum amount of chars which is used for getting PaymentProductId by CreditCardNumber
	private static final int MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 8;
	private static final int MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP = 6;

	// Configuration needed for communicating with the Online Payments gateway
	private C2sCommunicatorConfiguration configuration;

	/**
	 * Creates the Communicator object which handles the communication with the Online Payments Client API.
	 */
	private C2sCommunicator(C2sCommunicatorConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Get C2sCommunicator instance.
	 *
	 * @param configuration configuration which is used to establish a connection with the Online Payments gateway
	 *
	 * @return the instance of this class
	 */
	public static C2sCommunicator getInstance(C2sCommunicatorConfiguration configuration) {

		if (configuration == null ) {
			throw new InvalidParameterException("Error creating C2sCommunicator instance, configuration may not be null");
		}
		return new C2sCommunicator(configuration);
	}


	/**
	 * Checks whether the EnvironmentType is set to production or not.
	 *
	 * @return a Boolean indicating whether the EnvironmentType is set to production or not
	 */
	public boolean isEnvironmentTypeProduction() {
		return configuration.environmentIsProduction();
	}

	/**
	 * @return The asset base URL
	 */
	public String getAssetUrl() {
		return configuration.getAssetUrl();
	}

	/**
	 * Utility method for setting whether request/response logging should be enabled or not.
	 *
	 * @param enableLogging boolean indicating whether request/response logging should be enabled or not
	 */
	public void setLoggingEnabled(boolean enableLogging) {
		configuration.setLoggingEnabled(enableLogging);
	}

	/**
	 * Checks whether request/response logging is enabled or not.
	 *
	 * @return a boolean indicating whether request/response logging is enabled or not
	 */
	public boolean getLoggingEnabled() {
		return configuration.getLoggingEnabled();
	}

	/**
	 * Retrieves {@link BasicPaymentProducts} from the Online Payments gateway without any fields.
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve the correct {@link BasicPaymentProducts}
	 *
	 * @return {@link BasicPaymentProducts}, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<BasicPaymentProducts> getBasicPaymentProducts(PaymentContext paymentContext, Context context) {
		if (paymentContext == null) {
			throw new InvalidParameterException("Error getting BasicPaymentProducts, request may not be null");
		}

		HttpURLConnection connection = null;
		ApiResponse<BasicPaymentProducts> response = new ApiResponse();

		try {

			// Build the complete url which is called
			String clientApiUrl = configuration.getBaseUrl();
			String paymentProductPath = Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCTS_PATH.replace("[cid]", configuration.getCustomerId());
			String completePath = clientApiUrl + paymentProductPath;

			// Add query parameters
			StringBuilder queryString = new StringBuilder();
			queryString.append("?countryCode=").append(paymentContext.getCountryCodeString());
			queryString.append("&amount=").append(paymentContext.getAmountOfMoney().getAmount());
			queryString.append("&isRecurring=").append(paymentContext.isRecurring());
			queryString.append("&currencyCode=").append(paymentContext.getAmountOfMoney().getCurrencyCodeString());
			queryString.append("&hide=fields");
			queryString.append("&").append(createCacheBusterParameter());

			// Add query string to complete path
			completePath += queryString.toString();

			// Do the call and deserialise the result to BasicPaymentProducts
			connection = doHTTPGetRequest(completePath, configuration.getClientSessionId(), configuration.getMetadata(context));
			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

	        BasicPaymentProducts basicPaymentProducts = gson.fromJson(responseBody, BasicPaymentProducts.class);

			for(BasicPaymentProduct paymentProduct : basicPaymentProducts.getBasicPaymentProducts()) {
				setLogoForDisplayHints(paymentProduct.getDisplayHints(), context);
				setLogoForDisplayHintsList(paymentProduct, context);
			}
			response.data = basicPaymentProducts;
			return response;

		} catch (CommunicationException e) {
			if (e.errorResponse != null) {
				Log.i(TAG, "API Error when getting BasicPaymentItems Response:" + e.getMessage());
				response.error =  e.errorResponse;
			} else {
				Log.i(TAG, "API Exception when getting BasicPaymentItems Response:" + e.getMessage());
				response.error = new ErrorResponse("Exception when getting BasicPaymentItems Response");
				response.error.throwable = e;
			}
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Exception when getting BasicPaymentItems Response:" + e.getMessage());
			response.error = new ErrorResponse("Exception when getting BasicPaymentItems Response:" + e.getMessage());
			response.error.throwable = e;
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error while getting paymentproducts:" + e.getMessage());
			}
		}
	}


	/**
	 * Retrieves a single {@link PaymentProduct} from the Online Payments gateway including all its fields.
	 *
	 * @param productId used to retrieve the {@link PaymentProduct} that is associated with this id
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve a {@link PaymentProduct}
	 *
	 * @return {@link PaymentProduct}, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<PaymentProduct> getPaymentProduct(String productId, Context context, PaymentContext paymentContext) {
		if (productId == null) {
			throw new InvalidParameterException("Error getting PaymentProduct, productId may not be null");
		}

		HttpURLConnection connection = null;
		ApiResponse<PaymentProduct> response = new ApiResponse();

		try {

			// Build the complete url which is called
			String clientApiUrl = configuration.getBaseUrl();
			String paymentProductPath = Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_PATH.replace("[cid]", configuration.getCustomerId()).replace("[pid]", productId);
			String completePath = clientApiUrl + paymentProductPath;

			// Add query parameters
			StringBuilder queryString = new StringBuilder();
			queryString.append("?countryCode=").append(paymentContext.getCountryCodeString());
			queryString.append("&amount=").append(paymentContext.getAmountOfMoney().getAmount());
			queryString.append("&isRecurring=").append(paymentContext.isRecurring());
			queryString.append("&currencyCode=").append(paymentContext.getAmountOfMoney().getCurrencyCodeString());
			queryString.append("&").append(createCacheBusterParameter());
			completePath += queryString.toString();

			// Do the call and deserialise the result to PaymentProduct
			connection = doHTTPGetRequest(completePath, configuration.getClientSessionId(), configuration.getMetadata(context));

			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PaymentProduct.class);

			setLogoForDisplayHints(response.data.getDisplayHints(), context);
			setLogoForDisplayHintsList(response.data, context);

			for(PaymentProductField paymentProductField : response.data.getPaymentProductFields()) {
				setImageForTooltip(paymentProductField.getDisplayHints(), context);
			}

			return response;
		} catch (CommunicationException e) {
			if (e.errorResponse != null) {
				Log.i(TAG, "API Error while getting Payment Product Response:" + e.getMessage());
				response.error = e.errorResponse;
			} else {
				Log.i(TAG, "API Exception when getting PaymentProduct Response:" + e.getMessage());
				response.error = new ErrorResponse("Exception when getting PaymentProduct Response");
				response.error.throwable = e;
			}
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error while getting PaymentProduct:" + e.getMessage());
			response.error = new ErrorResponse("Error while getting PaymentProduct:" + e.getMessage());
			response.error.throwable = e;
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error while getting paymentproduct:" + e.getMessage());
			}
		}
	}

	/**
	 * Retrieves a single Payment Product Network from the Online Payments gateway.
	 *
	 * @param customerId for which customer the network must be retrieved
	 * @param productId the product of the id for which the network must be retrieved
	 * @param currencyCode for which {@link CurrencyCode} the network must be retrieved
	 * @param countryCode for which {@link CountryCode} the network must be retrieved
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve a {@link PaymentProduct}
	 *
	 * @return {@link PaymentProduct}, wrapped as {@link ApiResponse}, with errors if those occurred
	 *
	 * @deprecated use {@link #getPaymentProductNetwork(String, String, String, String, Context, PaymentContext)} instead
	 */
	@Deprecated
	public ApiResponse<PaymentProductNetworkResponse> getPaymentProductNetwork(String customerId, String productId, CurrencyCode currencyCode, CountryCode countryCode, Context context, PaymentContext paymentContext) {
		return getPaymentProductNetwork(productId, context, paymentContext);
	}

	/**
	 * Retrieves a single Payment Product Network from the Online Payments gateway.
	 *
	 * @param customerId for which customer the network must be retrieved
	 * @param productId the product of the id for which the network must be retrieved
	 * @param currencyCode for which the network must be retrieved
	 * @param countryCode for which the network must be retrieved
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve a {@link PaymentProduct}
	 *
	 * @return {@link PaymentProduct}, wrapped as {@link ApiResponse}, with errors if those occurred
	 *
	 * @deprecated use {@link #getPaymentProductNetwork(String, Context, PaymentContext)} instead
	 */
	@Deprecated
	public ApiResponse<PaymentProductNetworkResponse> getPaymentProductNetwork(String customerId, String productId, String currencyCode, String countryCode, Context context, PaymentContext paymentContext) {
		return getPaymentProductNetwork(productId, context, paymentContext);
	}

	/**
	 * Retrieves a single Payment Product Network from the Online Payments gateway.
	 *
	 * @param productId the product of the id for which the network must be retrieved
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve a {@link PaymentProduct}
	 *
	 * @return {@link PaymentProduct}, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<PaymentProductNetworkResponse> getPaymentProductNetwork(String productId, Context context, PaymentContext paymentContext) {

		if (productId == null) {
			throw new InvalidParameterException("Error getting Payment Product Network, productId may not be null");
		}
		if (context == null) {
			throw new InvalidParameterException("Error getting Payment Product Network, context may not be null");
		}
		if (paymentContext == null) {
			throw new InvalidParameterException("Error getting Payment Product Network, paymentContext may not be null");
		}

		HttpURLConnection connection = null;
		ApiResponse<PaymentProductNetworkResponse> response = new ApiResponse<>();

		try {

			// Build the correct URL
			String clientApiUrl = configuration.getBaseUrl();
			String paymentProductNetworkPath = Constants.OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_NETWORKS_PATH.replace("[cid]", configuration.getCustomerId()).replace("[pid]", productId);
			String completePath = clientApiUrl + paymentProductNetworkPath;

			// Add query parameters
			StringBuilder queryString = new StringBuilder();
			queryString.append("?countryCode=").append(paymentContext.getCountryCodeString());
			queryString.append("&amount=").append(paymentContext.getAmountOfMoney().getAmount());
			queryString.append("&isRecurring=").append(paymentContext.isRecurring());
			queryString.append("&currencyCode=").append(paymentContext.getAmountOfMoney().getCurrencyCodeString());
			queryString.append("&").append(createCacheBusterParameter());
			completePath += queryString.toString();

			//Do call and deserialize the response
			connection = doHTTPGetRequest(completePath, configuration.getClientSessionId(), configuration.getMetadata(context));
			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PaymentProductNetworkResponse.class);
			return response;
		} catch (CommunicationException e) {
			if (e.errorResponse != null) {
				Log.i(TAG, "API Error while getting paymentproduct directory" + e.getMessage());
				response.error =  e.errorResponse;
			} else {
				Log.i(TAG, "Error while getting paymentproduct directory" + e.getMessage());
				response.error = new ErrorResponse("Error while getting paymentproduct directory");
				response.error.throwable = e;
			}
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error while getting paymentproduct network:" + e.getMessage());
			response.error = new ErrorResponse("Error while getting paymentproduct network:" + e.getMessage());
			response.error.throwable = e;
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error while getting paymentproduct network:" + e.getMessage());
			}
		}
	}

	/**
	 * Retrieves the IIN details as a {@link IinDetailsResponse} for the entered partial credit card number.
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param partialCreditCardNumber entered partial credit card number for which the {@link IinDetailsResponse} should be retrieved
	 * @param paymentContext {@link PaymentContext} which contains all necessary data to retrieve the {@link IinDetailsResponse}
	 *
	 * @return {@link IinDetailsResponse} which contains the result of the IIN lookup, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<IinDetailsResponse> getPaymentProductIdByCreditCardNumber(String partialCreditCardNumber, Context context, PaymentContext paymentContext) {

		if (partialCreditCardNumber == null ) {
			throw new InvalidParameterException("Error getting IinDetails, partialCreditCardNumber may not be null");
		}

		// Trim partialCreditCardNumber to MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP digits
		if (partialCreditCardNumber.length() >= MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
			partialCreditCardNumber = partialCreditCardNumber.substring(0, MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP);
		} else if (partialCreditCardNumber.length() > MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
			partialCreditCardNumber = partialCreditCardNumber.substring(0, MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP);
		}

		HttpURLConnection connection = null;
		ApiResponse<IinDetailsResponse> response = new ApiResponse();

		try {

			// Construct the url for the IIN details call
			String paymentProductPath = Constants.OP_GATEWAY_IIN_LOOKUP_PATH.replace("[cid]", configuration.getCustomerId());
			String url = configuration.getBaseUrl() + paymentProductPath;

			// Serialise the IinDetailsRequest to json, so it can be added to the postbody
			IinDetailsRequest iinRequest = new IinDetailsRequest(partialCreditCardNumber, paymentContext);
			String iinRequestJson = gson.toJson(iinRequest);

			// Do the call and deserialise the result to IinDetailsResponse
			connection = doHTTPPostRequest(url, configuration.getClientSessionId(), configuration.getMetadata(context), iinRequestJson);
			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, IinDetailsResponse.class);

			return response;

		} catch (CommunicationException e) {
			if (e.errorResponse != null) {
				Log.i(TAG, "API Error when getting PaymentProductIdByCreditCardNumber Response:" + e.getMessage());
				response.error =  e.errorResponse;
			} else {
				Log.i(TAG, "Error getting PaymentProductIdByCreditCardNumber response:" + e.getMessage());
				response.error = new ErrorResponse("Exception when getting PaymentProductIdByCreditCardNumber Response");
				response.error.throwable = e;
			}
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error getting PaymentProductIdByCreditCardNumber response:" + e.getMessage());
			response.error = new ErrorResponse("Exception when retrieving IinDetails: " + e.getMessage());
			response.error.throwable = e;
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error while getting PaymentProductIdByCreditCardNumber response:" + e.getMessage());
			}
		}
	}


	/**
	 * Retrieves the public key as a {@link PublicKeyResponse} from the Online Payments gateway.
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 *
	 * @return {@link PublicKeyResponse}, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<PublicKeyResponse> getPublicKey(Context context) {

		HttpURLConnection connection = null;
		ApiResponse<PublicKeyResponse> response = new ApiResponse();

		try {

			// Construct the url for the PublicKey call
			String paymentProductPath = Constants.OP_GATEWAY_PUBLIC_KEY_PATH.replace("[cid]", configuration.getCustomerId());
			String url = configuration.getBaseUrl() + paymentProductPath;

			// Do the call and deserialise the result to PublicKeyResponse
			connection = doHTTPGetRequest(url, configuration.getClientSessionId(), configuration.getMetadata(context));
			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PublicKeyResponse.class);
			return response;

		} catch (CommunicationException e) {
			if (e.errorResponse != null) {
				Log.i(TAG, "API Error getting Public key response:" + e.getMessage());
				response.error = e.errorResponse;
			} else {
				Log.i(TAG, "Exception when getting Public key response:" + e.getMessage());
				response.error = new ErrorResponse("Exception when getting Public key response:" + e.getMessage());
				response.error.throwable = e;
			}
			return response;
		}  catch (Exception e) {
			Log.i(TAG, "Exception when getting Public key response:" + e.getMessage());
			response.error = new ErrorResponse("Exception when getting Public key response:" + e.getMessage());
			response.error.throwable = e;
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error getting Public key response:" + e.getMessage());
			}
		}
	}

	/**
	 * Retrieves the Surcharge Calculation as a {@link SurchargeCalculationResponse} for the provided amount of money, partial credit card number and payment product ID.
	 *
	 * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
	 * @param cardSource contains the card or token for which the Surcharge should be calculated
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 *
	 * @return {@link SurchargeCalculationResponse} which contains the result of the Surcharge Calculation, wrapped as {@link ApiResponse}, with errors if those occurred
	 */
	public ApiResponse<SurchargeCalculationResponse> getSurchargeCalculation(AmountOfMoney amountOfMoney, CardSource cardSource, Context context) {

		if (amountOfMoney == null) {
			throw new InvalidParameterException("Error getting surcharge calculation, amountOfMoney may not be null");
		}
		if (cardSource == null) {
			throw new InvalidParameterException("Error getting surcharge calculation, cardSource may not be null");
		}

		HttpURLConnection connection = null;
		ApiResponse<SurchargeCalculationResponse> response = new ApiResponse();

		try {

			// Construct the url for the Surcharge Calculation call
			String surchargeCalculationPath = Constants.OP_GATEWAY_SURCHARGE_CALCULATION_PATH.replace("[cid]", configuration.getCustomerId());
			String url = configuration.getBaseUrl() + surchargeCalculationPath;

			// Serialise the SurchargeCalculationRequest to json, so it can be added to the postbody
			SurchargeCalculationRequest surchargeCalculationRequest = new SurchargeCalculationRequest(amountOfMoney, cardSource);
			String surchargeCalculationRequestJson = gson.toJson(surchargeCalculationRequest);

			// Do the call and deserialise the result to SurchargeCalculationResponse
			connection = doHTTPPostRequest(url, configuration.getClientSessionId(), configuration.getMetadata(context), surchargeCalculationRequestJson);
			String responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			// Log the response
			if (getLoggingEnabled()) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, SurchargeCalculationResponse.class);

			return response;

		} catch (Exception e) {
			Log.i(TAG, "Error getting SurchargeCalculation response:" + e.getMessage());
			response.error = new ErrorResponse("Exception when retrieving SurchargeCalculation: " + e.getMessage());
			return response;
		} finally {
			try {
				if (connection != null) {
					connection.getInputStream().close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.i(TAG, "Error while getting SurchargeCalculation response:" + e.getMessage());
			}
		}
	}

	/**
	 * Returns a map of metadata of the device this SDK is running on.
	 * The map contains the SDK version, OS, OS version and screen size.
	 *
	 * @param context used for retrieving device metadata
	 *
	 * @return a Map containing key/values of metadata
	 */
	public Map<String, String> getMetadata(Context context) {
		return configuration.getMetadata(context);
	}

	/**
	 * Does a GET request with HttpURLConnection.
	 *
	 * @param location url where the request is sent to
	 * @param clientSessionId used for session identification on the Online Payments gateway
	 * @param metadata map filled with metadata, which is added to the request
	 *
	 * @return HttpURLConnection, which contains the response of the request
	 *
	 * @throws CommunicationException
	 */
	private HttpURLConnection doHTTPGetRequest(String location, String clientSessionId, Map<String, String> metadata) throws CommunicationException {

		// Initialize the connection
		try {
			URL url = new URL(location);

			HttpURLConnection connection = openConnection(url);

			// Add sessionId header
			if (clientSessionId != null) {
				connection.addRequestProperty(HTTP_HEADER_SESSION_ID, "GCS v1Client:" + clientSessionId);
			}

			// Add metadata entries header
			if (metadata != null) {
				connection.addRequestProperty(HTTP_HEADER_METADATA, Util.getBase64EncodedMetadata(metadata));
			}

			// Log the request
			if (getLoggingEnabled()) {
				logRequest(connection, null);
			}

			// Check if the response code is HTTP_OK
			if (connection.getResponseCode() != 200) {
				ErrorResponse errorResponse = createErrorResponse(connection.getResponseCode(), connection.getErrorStream());
				throw new CommunicationException("No status 200 received, status is :" + connection.getResponseCode(), errorResponse);
			}

			return connection;

		} catch (MalformedURLException e) {
			Log.e(TAG, "doHTTPGetRequest, Unable to parse url " + location);
			throw new CommunicationException("Unable to parse url " + location);
		}  catch (IOException e) {
			Log.e(TAG, "doHTTPGetRequest, IOException while opening connection " + e.getMessage());
			throw new CommunicationException("IOException while opening connection " + e.getMessage(), e);
		} catch (KeyManagementException e) {
			Log.e(TAG, "doHTTPPostRequest, KeyManagementException while opening connection " + e.getMessage());
			throw new CommunicationException("KeyManagementException while opening connection " + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "doHTTPPostRequest, NoSuchAlgorithmException while opening connection " + e.getMessage());
			throw new CommunicationException("NoSuchAlgorithmException while opening connection " + e.getMessage(), e);
		}
	}


	/**
	 * Does a POST request with HttpClient.
	 *
	 * @param location url where the request is sent to
	 * @param clientSessionId used for identification on the Online Payments gateway
	 * @param metadata map filled with metadata, which is added to the request
	 * @param postBody the content of the post body
	 *
	 * @return HttpURLConnection, which contains the response of the request
	 *
	 * @throws CommunicationException
	 */
	private HttpURLConnection doHTTPPostRequest(String location, String clientSessionId, Map<String, String> metadata, String postBody) throws CommunicationException {

		// Initialize the connection
		OutputStreamWriter writer = null;
		try {
			URL url = new URL(location);

			HttpURLConnection connection = openConnection(url);

			// Set request method to POST
			connection.setRequestMethod("POST");

			// Add json header
			connection.addRequestProperty("Content-Type", "application/json");

			// Add sessionId header
			if (clientSessionId != null) {
				connection.addRequestProperty(HTTP_HEADER_SESSION_ID, "GCS v1Client:" + clientSessionId);
			}

			// Add metadata header
			if (metadata != null) {
				connection.addRequestProperty(HTTP_HEADER_METADATA, Util.getBase64EncodedMetadata(metadata));
			}

			// Log the request
			if (getLoggingEnabled()) {
				logRequest(connection, postBody);
			}

			// Add post body
			connection.setDoOutput(true);
			writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8.name());
			writer.write(postBody);
			writer.flush();

			// Check if the response code is HTTP_OK
			if (connection.getResponseCode() != 200) {
				ErrorResponse errorResponse = createErrorResponse(connection.getResponseCode(), connection.getErrorStream());
				throw new CommunicationException("No status 200 received, status is :" + connection.getResponseCode(), errorResponse);
			}

			return connection;

		} catch (MalformedURLException e) {
			Log.e(TAG, "doHTTPPostRequest, Unable to parse url " + location);
			throw new CommunicationException("Unable to parse url " + location);
		} catch (IOException e) {
			Log.e(TAG, "doHTTPPostRequest, IOException while opening connection " + e.getMessage());
			throw new CommunicationException("IOException while opening connection " + e.getMessage(), e);
		} catch (KeyManagementException e) {
			Log.e(TAG, "doHTTPPostRequest, KeyManagementException while opening connection " + e.getMessage());
			throw new CommunicationException("KeyManagementException while opening connection " + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "doHTTPPostRequest, NoSuchAlgorithmException while opening connection " + e.getMessage());
			throw new CommunicationException("NoSuchAlgorithmException while opening connection " + e.getMessage(), e);
		}  finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					Log.i(TAG, "doHTTPPostRequest, IOException while closing connection " + e.getMessage());
				}
			}
		}
	}

	private HttpURLConnection openConnection(URL url) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		HttpURLConnection connection;
		if ("https".equalsIgnoreCase(url.getProtocol())) {
			connection = (HttpsURLConnection) url.openConnection();
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, null, null);
			SSLSocketFactory noSSLv3Factory;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
				noSSLv3Factory = new TLSSocketFactory(sslContext.getSocketFactory());
			} else {
				noSSLv3Factory = sslContext.getSocketFactory();
			}
			((HttpsURLConnection) connection).setSSLSocketFactory(noSSLv3Factory);
		} else {
			connection = (HttpURLConnection) url.openConnection();
		}

		return connection;
	}

	private String createCacheBusterParameter() {
		String cacheBuster = "cacheBuster=" + new Date().getTime();
		return cacheBuster;
	}

	private ErrorResponse createErrorResponse(int responseCode, InputStream errorStream) {
		ErrorResponse errorResponse = new ErrorResponse("No status 200 received, status is :" + responseCode);
		try {
			String errorBody = new Scanner(errorStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
			errorResponse.apiError = gson.fromJson(errorBody, ApiError.class);
		} catch (Exception e) {
			Log.i(TAG,"doHTTPPostRequest, Unable to parse json for errors " + e.getMessage());
		}

		return errorResponse;
	}


	/**
	 * Logs all request headers, url and body.
	 */
	private void logRequest(HttpURLConnection connection, String postBody) {

		String requestLog = "Request URL : " + connection.getURL() + "\n";
		requestLog += "Request Method : " + connection.getRequestMethod() + "\n";
		requestLog += "Request Headers : " + "\n";

		for (Map.Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
			for (String value : header.getValue()) {
				requestLog += "\t\t" + header.getKey() + ":" + value + "\n";
			}
		}

		if(connection.getRequestMethod().equalsIgnoreCase("post")) {
			requestLog += "Body : " + postBody + "\n";
		}
		Log.i(TAG, requestLog);
	}

	/**
	 * Logs all response headers, status code and body.
	 *
	 * @throws IOException when an error occurs while retrieving response code
     */
	private void logResponse(HttpURLConnection connection, String responseBody) throws IOException {

		String responseLog = "Response URL : " + connection.getURL() + "\n";
		responseLog += "Response Code : " + connection.getResponseCode() + "\n";

		responseLog += "Response Headers : " + "\n";
		for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
			for (String value : header.getValue()) {
				if (header.getKey() == null) {
					responseLog += "\t\t" + value + "\n";
				} else {
					responseLog += "\t\t" + header.getKey() + ":" + value + "\n";
				}
			}
		}

		responseLog += "Response Body : " + responseBody  + "\n";
		Log.i(TAG, responseLog);

		// Calculate duration and log it
		if (connection.getHeaderField("X-Android-Sent-Millis") != null && connection.getHeaderField("X-Android-Received-Millis") != null) {
			long messageSentMillis 	   = Long.parseLong(connection.getHeaderField("X-Android-Sent-Millis"));
			long messageReceivedMillis = Long.parseLong(connection.getHeaderField("X-Android-Received-Millis"));
			Log.i(TAG, "Request Duration : " + (messageReceivedMillis - messageSentMillis) + " millisecs \n");
		}
	}

	private void setLogoForDisplayHintsList(BasicPaymentProduct basicPaymentProduct, Context context) {
		for (DisplayHintsPaymentItem displayHints : basicPaymentProduct.getDisplayHintsList()){
			this.setLogoForDisplayHints(displayHints, context);
		}
	}

	private void setLogoForDisplayHints(DisplayHintsPaymentItem displayHints, Context context) {
		Drawable logo = this.getImageFromStringUrl(displayHints.getLogoUrl(), context);

		displayHints.setLogo(logo);
	}

	private void setImageForTooltip(DisplayHintsProductFields displayHintsProductFields, Context context) {
		Tooltip tooltip = displayHintsProductFields.getTooltip();
		Drawable tooltipImage = this.getImageFromStringUrl(tooltip.getImageURL(), context);
		displayHintsProductFields.getTooltip().setImageDrawable(tooltipImage);
	}

	private Drawable getImageFromStringUrl(String url, Context context) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent(), null, null);
			return new BitmapDrawable(context.getResources(), bitmap);
		} catch (IOException e) {
			return null;
		}
	}
}
