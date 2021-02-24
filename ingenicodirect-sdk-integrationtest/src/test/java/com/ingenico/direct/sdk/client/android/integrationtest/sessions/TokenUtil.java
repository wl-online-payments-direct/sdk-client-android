package com.ingenico.direct.sdk.client.android.integrationtest.sessions;

import com.ingenico.direct.sdk.client.android.exception.CommunicationException;
import com.ingenico.direct.Client;
import com.ingenico.direct.domain.CreateTokenRequest;
import com.ingenico.direct.domain.CreatedTokenResponse;
import com.ingenico.direct.merchant.tokens.TokensClient;

/**
 * Util class that is capable of creating and retrieving tokens.
 *
 * Copyright 2014 Global Collect Services B.V
 *
 */
public class TokenUtil {

    private final Client client;

    public TokenUtil(Client client) {
        this.client = client;
    }

    public String createToken(String merchantId, CreateTokenRequest body) throws CommunicationException {
        TokensClient tokensClient = client.merchant(merchantId).tokens();

        CreatedTokenResponse response = tokensClient.createToken(body);
        if (response.getIsNewToken()) {
            return response.getToken();
        }
        String tokenId = response.getToken();
        tokensClient.deleteToken(tokenId);

        response = tokensClient.createToken(body);
        return response.getToken();
    }

    public void deleteToken(String merchantId, String tokenId) {
        client.merchant(merchantId).tokens().deleteToken(tokenId);
    }
}
