/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.encryption;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import android.util.Log;

import com.onlinepayments.sdk.client.android.exception.EncryptDataException;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Handles all Encryption related functionality.
 * Uses the JOSE web encryption standard.
 *
 * @see <a href="http://tools.ietf.org/html/draft-ietf-jose-json-web-encryption-29">JOSE web encryption standard</a>
 *
 * @deprecated In a future release, this class and its functions will become internal to the SDK.
 */
@Deprecated
public class Encryptor {

	// Tag used for logging
	private static final String TAG = Encryptor.class.getName();

	// ContentEncryptionKey byte[] size = 512 bits
	private static final int CONTENT_ENCRYPTION_KEY_SIZE = 64;

	// Initialization Vector byte[] size = 128 bits
	private static final int INITIALIZATION_VECTOR = 16;

	// Protected Header settings
	private static final String PROTECTED_HEADER_ALG = "RSA-OAEP";
	private static final String PROTECTED_HEADER_ENC = "A256CBC-HS512";

    // PublicKeyResponse which holds the Online Payments Gateway public key
 	private PublicKeyResponse publicKeyResponse;

 	// Helper class for Encryption
 	private EncryptUtil encryptUtil = new EncryptUtil();


	/**
	 * Create Encryptor
	 *
	 * @param publicKeyResponse contains the Online Payments gateway public key
	 */
	public Encryptor(PublicKeyResponse publicKeyResponse) {
		this.publicKeyResponse = publicKeyResponse;
	}


	/**
	 * Encrypts all payment product field values for the given payment request as {@link EncryptData}.
	 *
	 * @param encryptData contains all field values and variables required for making a payment request
	 *
	 * @return encrypted String
	 */
	public String encrypt(EncryptData encryptData) {

		// Convert EncryptData to JSON format
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EncryptData.class, new EncryptDataJsonSerializer());
		Gson gson = gsonBuilder.create();
		String payload = gson.toJson(encryptData);

		try {

			// Create protected header and encode it with Base64 encoding
			String protectedHeader = createProtectedHeader();
			String encodededProtectedHeader = encryptUtil.base64UrlEncode(protectedHeader.getBytes(StandardCharsets.UTF_8));

			// Create ContentEncryptionKey, is a random byte[]
			byte[] contentEncryptionKey = encryptUtil.generateSecureRandomBytes(CONTENT_ENCRYPTION_KEY_SIZE);

			// Encrypt the contentEncryptionKey with the Online Payments gateway publickey and encode it with Base64 encoding
			byte[] encryptedContentEncryptionKey = encryptUtil.encryptContentEncryptionKey(contentEncryptionKey, publicKeyResponse.getPublicKey());
			String encodedEncryptedContentEncryptionKey = encryptUtil.base64UrlEncode(encryptedContentEncryptionKey);

			// Split the contentEncryptionKey in ENC_KEY and MAC_KEY for using hmac
			byte[] macKey = Arrays.copyOf(contentEncryptionKey, CONTENT_ENCRYPTION_KEY_SIZE/ 2);
			byte[] encKey = Arrays.copyOfRange(contentEncryptionKey, CONTENT_ENCRYPTION_KEY_SIZE /2, CONTENT_ENCRYPTION_KEY_SIZE);

			// Create Initialization Vector
			byte[] initializationVector = encryptUtil.generateSecureRandomBytes(INITIALIZATION_VECTOR);
			String encodededinitializationVector = encryptUtil.base64UrlEncode(initializationVector);

			// Encrypt content with ContentEncryptionKey and Initialization Vector
			byte[] cipherText = encryptUtil.encryptPayload(payload, encKey, initializationVector);
			String encodedCipherText = encryptUtil.base64UrlEncode(cipherText);

			// Create Additional Authenticated Data  and Additional Authenticated Data Length
			byte[] additionalAuthenticatedData = new byte[0];
			additionalAuthenticatedData = encodededProtectedHeader.getBytes(StandardCharsets.UTF_8);
			byte[] al = calculateAdditionalAuthenticatedDataLength(additionalAuthenticatedData);

			// Calculates HMAC
			byte[] calculatedHmac = calculateHMAC(macKey, additionalAuthenticatedData, initializationVector, cipherText, al);

			// Truncate HMAC Value to Create Authentication Tag
			byte[] authenticationTag = Arrays.copyOf(calculatedHmac, calculatedHmac.length /2);
			String encodedAuthenticationTag = encryptUtil.base64UrlEncode(authenticationTag);


			return buildCompactRespresentation(encodededProtectedHeader, encodedEncryptedContentEncryptionKey,
											   encodededinitializationVector,
											   encodedCipherText, encodedAuthenticationTag);

		} catch (EncryptDataException | IOException e) {
			Log.i(TAG, "Error while encrypting fields" + e.getMessage());
		}

		return null;
	}



	/**
	 * Calculates HMAC over the data.
	 *
	 * @param macKey unique random key
	 * @param additionalAuthenticatedData Additional Authenticated Data
	 * @param initializationVector Initialization Vector
	 * @param cipherText encrypted data
	 * @param al Additional Authenticated Data Length
	 *
	 * @return HMAC value
	 */
	private byte[] calculateHMAC(byte[] macKey, byte[] additionalAuthenticatedData, byte[] initializationVector, byte[] cipherText, byte[] al) throws IOException, EncryptDataException {
		// Create HMAC Computation input
		byte[] hmacInput = encryptUtil.concatenateByteArrays(additionalAuthenticatedData, initializationVector, cipherText, al);

		// And calculate HMAC over those byte[]
		return encryptUtil.calculateHmac(macKey, hmacInput);
	}



	/**
	 * Creates Protected header string which determines the Algorithm and Encryption with which the payload will be encrypted.
	 *
	 * @return protected header String
	 */
	private String createProtectedHeader() {

		StringBuilder header = new StringBuilder();

		header.append("{\"alg\":\"").append(PROTECTED_HEADER_ALG).append("\",");
		header.append("\"enc\":\"").append(PROTECTED_HEADER_ENC).append("\",");
		header.append("\"kid\":\"").append(publicKeyResponse.getKeyId()).append("\"}");

		return header.toString();
	}


	/**
	 * Creates the CompactRepresentation of all the encrypted components.
	 *
	 * @param components list of all components
	 *
	 * @return CompactRepresentation of all the encrypted components
	 */
	private String buildCompactRespresentation(String ... components) {

		// Loop over all components to add them to the StringBuilder
		StringBuilder builder = new StringBuilder();
		for (int componentCount = 0; componentCount < components.length; componentCount++) {

			builder.append(components[componentCount]);

			// Append . between the different components
			if (componentCount != components.length -1) {
				builder.append(".");
			}
		}
		return builder.toString();
	}


	/**
	 * Calculate Additional Authenticated Data Length.
	 *
	 * @return byte respresentation of the Additional Authenticated Data Length
	 */
	private byte[] calculateAdditionalAuthenticatedDataLength(byte[] additionalAuthenticatedData) {
		long lengthInBits = Long.valueOf(additionalAuthenticatedData.length) * 8;
		byte[] al = ByteBuffer.allocate(8).putLong(lengthInBits).array();

		return al;
	}


}
