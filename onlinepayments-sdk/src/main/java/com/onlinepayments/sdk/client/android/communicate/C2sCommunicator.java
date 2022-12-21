package com.onlinepayments.sdk.client.android.communicate;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.onlinepayments.sdk.client.android.manager.AssetManager;
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
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
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
 * Handles all communication with the Client API.
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
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

	// Configuration needed for communicating with the GC gateway
	private C2sCommunicatorConfiguration configuration;

	/**
	 * Creates the Communicator object which handles the communication with the Online Payments Client API
	 */
	private C2sCommunicator(C2sCommunicatorConfiguration configuration) {
		this.configuration = configuration;
	}


	/**
	 * Get C2sCommunicator instance
	 * @param configuration configuration which is used to establish a connection with the Online Payments gateway
	 * @return the instance of this class
	 */
	public static C2sCommunicator getInstance(C2sCommunicatorConfiguration configuration) {

		if (configuration == null ) {
			throw new InvalidParameterException("Error creating C2sCommunicator instance, configuration may not be null");
		}
		return new C2sCommunicator(configuration);
	}


	/**
	 * Returns true if the EnvironmentType is set to production; otherwise false is returned.
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
	 * Retrieves a list of basicpaymentproducts from the GC gateway without any fields
	 *
	 * @param context, used for reading device metadata which is send to the GC gateway
	 * @param paymentContext, payment information that is used to retrieve the correct payment products
	 *
	 * @return list of BasicPaymentProducts, wrapped as ApiResponse, with errors if those occurred
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
			String responseBody = new Scanner(connection.getInputStream(),"UTF-8").useDelimiter("\\A").next();

			// Log the response
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logResponse(connection, responseBody);
			}

	        BasicPaymentProducts basicPaymentProducts = gson.fromJson(responseBody, BasicPaymentProducts.class);

			for(BasicPaymentProduct paymentProduct : basicPaymentProducts.getBasicPaymentProducts()) {
				setLogoForDisplayHints(paymentProduct, context);
				setLogoForDisplayHintsList(paymentProduct, context);
			}
			response.data = basicPaymentProducts;
			return response;

		} catch (CommunicationException e) {
			Log.i(TAG, "Error while getting paymentproducts:" + e.getMessage());
			response.error = e.errorResponse;
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error while getting paymentproducts:" + e.getMessage());
			response.error = new ErrorResponse("Error while getting paymentproducts:" + e.getMessage());
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
	 * Retrieves a single paymentproduct from the GC gateway including all its fields
	 *
	 * @param productId, used to retrieve the PaymentProduct that is associated with this id
	 * @param context, used for reading device metada which is send to the GC gateway
	 * @param paymentContext, PaymentContext which contains all neccesary data to retrieve a paymentproduct
	 *
	 * @return PaymentProduct, or null when an error has occured
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
			if (paymentContext.isForceBasicFlow()!= null) {
				queryString.append("&forceBasicFlow=").append(paymentContext.isForceBasicFlow());
			}
			queryString.append("&").append(createCacheBusterParameter());
			completePath += queryString.toString();

			// Do the call and deserialise the result to PaymentProduct
			connection = doHTTPGetRequest(completePath, configuration.getClientSessionId(), configuration.getMetadata(context));

			String responseBody = new Scanner(connection.getInputStream(),"UTF-8").useDelimiter("\\A").next();

			// Log the response
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PaymentProduct.class);
			setLogoForDisplayHintsList(response.data, context);
			setLogoForDisplayHints(response.data, context);

			for(PaymentProductField paymentProductField : response.data.getPaymentProductFields()) {
				setImageForTooltip(paymentProductField.getDisplayHints(), context);
			}

			return response;

		} catch (CommunicationException e) {
			Log.i(TAG, "Error while getting paymentproduct:" + e.getMessage());
			response.error = e.errorResponse;
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error while getting paymentproduct:" + e.getMessage());
			response.error = new ErrorResponse("Error while getting paymentproduct:" + e.getMessage());
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
	 * @deprecated use {@link #getPaymentProductNetwork(String, String, String, String, Context, PaymentContext)} instead
	 */
	@Deprecated
	public ApiResponse<PaymentProductNetworkResponse> getPaymentProductNetwork(String customerId, String productId, CurrencyCode currencyCode, CountryCode countryCode, Context context, PaymentContext paymentContext) {
		return getPaymentProductNetwork(customerId, productId, currencyCode.toString(), countryCode.toString(), context, paymentContext);
	}

	public ApiResponse<PaymentProductNetworkResponse> getPaymentProductNetwork(String customerId, String productId, String currencyCode, String countryCode, Context context, PaymentContext paymentContext) {

		if (customerId == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, customerId may not be null");
		}
		if (productId == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, productId may not be null");
		}
		if (countryCode == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, countryCode may not be null");
		}
		if (currencyCode == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, currencyCode may not be null");
		}
		if (context == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, context may not be null");
		}
		if (paymentContext == null) {
			throw new InvalidParameterException("Error getting PaymentProduct network, paymentContext may not be null");
		}

		HttpURLConnection connection = null;
		ApiResponse<PaymentProductNetworkResponse> response = new ApiResponse<PaymentProductNetworkResponse>();

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
			String responseBody = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A").next();

			// Log the response
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PaymentProductNetworkResponse.class);
			return response;
		} catch (CommunicationException e) {
			Log.i(TAG, "Error while getting paymentproduct directory:" + e.getMessage());
			response.error = e.errorResponse;
			return response;
		} catch (Exception e) {
			Log.i(TAG, "Error while getting paymentproduct network:" + e.getMessage());
			response.error = new ErrorResponse("Error while getting paymentproduct network:" + e.getMessage());
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
	 * Get the IIN details for the entered partial creditcardnumber
	 *
	 * @param context, used for reading device metada which is send to the GC gateway
	 * @param partialCreditCardNumber, entered partial creditcardnumber
	 * @param paymentContext, meta data for the payment that is used to get contextual information from the GC gateway
	 *
	 * @return IinDetailsResponse which contains the result of the IIN lookup, or null when an error has occured
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
			String responseBody = new Scanner(connection.getInputStream(),"UTF-8").useDelimiter("\\A").next();

			// Log the response
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, IinDetailsResponse.class);

			return response;

		} catch (Exception e) {
			Log.i(TAG, "Error getting PaymentProductIdByCreditCardNumber response:" + e.getMessage());
			response.error = new ErrorResponse("Exception when retrieving IinDetails: " + e.getMessage());
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
	 * Retrieves the publickey from the GC gateway
	 *
	 * @param context, used for reading device metada which is send to the GC gateway
	 *
	 * @return PublicKeyResponse response , or null when an error has occured
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
			String responseBody = new Scanner(connection.getInputStream(),"UTF-8").useDelimiter("\\A").next();

			// Log the response
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logResponse(connection, responseBody);
			}

			response.data = gson.fromJson(responseBody, PublicKeyResponse.class);
			return response;

		} catch (CommunicationException e) {
			Log.i(TAG, "Error getting Public key response:" + e.getMessage());
			response.error = e.errorResponse;
			return response;
		}  catch (Exception e) {
			Log.i(TAG, "Error getting Public key response:" + e.getMessage());
			response.error = new ErrorResponse("Error getting Public key response:" + e.getMessage());
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
	 * Returns map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 * @param context, used for reading device metada which is send to the GC gateway
	 *
	 * @return Map<String, String> containing key/values of metadata
	 */
	public Map<String, String> getMetadata(Context context) {
		return configuration.getMetadata(context);
	}

	/**
	 * Does a GET request with HttpURLConnection
	 *
	 * @param location, url where the request is sent to
	 * @param clientSessionId, used for session identification on the GC gateway
	 * @param metadata, map filled with metadata, which is added to the request
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
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logRequest(connection, null);
			}

			// Check if the response code is HTTP_OK
			if (connection.getResponseCode() != 200) {
				ErrorResponse errorResponse = new ErrorResponse("No status 200 received, status is :" + connection.getResponseCode());
				try {
					ApiError apiError = gson.fromJson(connection.getResponseMessage(), ApiError.class);
					errorResponse.apiError = apiError;
				} catch (Exception e) {
					Log.i(TAG,"doHTTPGetRequest, Unable to parse json for errors " + e.getMessage());
				}
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
	 * Does a POST request with HttpClient
	 *
	 * @param location, url where the request is sent to
	 * @param clientSessionId, used for identification on the GC gateway
	 * @param metadata, map filled with metadata, which is added to the request
	 * @param postBody, the content of the postbody
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
			if (Constants.ENABLE_REQUEST_LOGGING) {
				logRequest(connection, postBody);
			}

			// Add post body
			connection.setDoOutput(true);
			writer = new OutputStreamWriter(connection.getOutputStream(), Charset.forName("UTF-8"));
			writer.write(postBody);
			writer.flush();

			// Check if the response code is HTTP_OK
			if (connection.getResponseCode() != 200) {
				ErrorResponse errorResponse = new ErrorResponse("No status 200 received, status is :" + connection.getResponseCode());
				try {
					ApiError apiError = gson.fromJson(connection.getResponseMessage(), ApiError.class);
					errorResponse.apiError = apiError;
				} catch (Exception e) {
					Log.i(TAG,"doHTTPPostRequest, Unable to parse json for errors " + e.getMessage());
				}
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


	/**
	 * Logs all request headers, url and body
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
	 * Logs all response headers, statuscode and body
	 *
	 * @throws IOException
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
		AssetManager assetManager = AssetManager.getInstance(context);
		for (DisplayHintsPaymentItem displayHints : basicPaymentProduct.getDisplayHintsList()){
			displayHints.setLogo(assetManager.getImageFromStringUrl(displayHints.getLogoUrl(), basicPaymentProduct.getId()));
		}
	}

	private void setLogoForDisplayHints(BasicPaymentProduct basicPaymentProduct, Context context) {
		AssetManager assetManager = AssetManager.getInstance(context);
		DisplayHintsPaymentItem displayHints = basicPaymentProduct.getDisplayHints();
		displayHints.setLogo(assetManager.getImageFromStringUrl(displayHints.getLogoUrl(), basicPaymentProduct.getId()));
	}

	private void setImageForTooltip(DisplayHintsProductFields displayHintsProductFields, Context context) {
		AssetManager assetManager = AssetManager.getInstance(context);
		displayHintsProductFields.getTooltip().setImageDrawable(assetManager.getImageFromStringUrl(displayHintsProductFields.getTooltip().getImageURL()));
	}
}
