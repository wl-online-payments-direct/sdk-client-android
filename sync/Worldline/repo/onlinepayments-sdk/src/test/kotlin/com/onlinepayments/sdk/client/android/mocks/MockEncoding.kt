/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.mocks

import android.util.Base64
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkStatic

class MockEncoding {
    companion object {
        fun setup() {
            mockkStatic(Base64::class)
            // Stub encode()
            every { Base64.encode(any<ByteArray>(), Base64.URL_SAFE) } answers {
                java.util.Base64.getMimeEncoder().encode(firstArg<ByteArray>())
            }
            coEvery { Base64.encode(any<ByteArray>(), Base64.URL_SAFE) } answers {
                java.util.Base64.getMimeEncoder().encode(firstArg<ByteArray>())
            }
            // Stub decode()
            every { Base64.decode(any<ByteArray>(), Base64.DEFAULT) } answers {
                java.util.Base64.getMimeDecoder().decode(firstArg<ByteArray>())
            }
            coEvery { Base64.decode(any<ByteArray>(), Base64.DEFAULT) } answers {
                java.util.Base64.getMimeDecoder().decode(firstArg<ByteArray>())
            }
        }
    }
}