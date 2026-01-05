/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.testUtil;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class GsonHelperJava {
    private static final Gson gson = new Gson();

    public static <T> T fromResourceJson(String resource, Class<T> classOfT) {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(GsonHelperJava.class.getClassLoader())
            .getResourceAsStream(resource))
        ) {
            return gson.fromJson(reader, classOfT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
