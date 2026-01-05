/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.mocks

import android.content.Context
import android.graphics.Rect
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

class MockContext {
    companion object {
        private val mockContext = mockk<Context>(relaxed = true)
        private val mockWindowManager = mockk<WindowManager>(relaxed = true)
        private val mockDisplayManager = mockk<Display>(relaxed = true)
        private val mockWindowMetrics = mockk<WindowMetrics>(relaxed = true)
        private val mockRect = mockk<Rect>(relaxed = true)

        fun setup(): Context {
            every { mockContext.getSystemService(Context.WINDOW_SERVICE) } returns mockWindowManager
            coEvery { mockContext.getSystemService(Context.WINDOW_SERVICE) } returns mockWindowManager
            @Suppress("DEPRECATION")
            every { mockWindowManager.defaultDisplay } returns mockDisplayManager
            @Suppress("DEPRECATION")
            coEvery { mockWindowManager.defaultDisplay } returns mockDisplayManager
            every { mockWindowManager.currentWindowMetrics } returns mockWindowMetrics
            coEvery { mockWindowManager.currentWindowMetrics } returns mockWindowMetrics
            every { mockWindowMetrics.bounds } returns mockRect
            coEvery { mockWindowMetrics.bounds } returns mockRect
            every { mockRect.width() } returns 1080
            coEvery { mockRect.width() } returns 1080
            every { mockRect.height() } returns 2400
            coEvery { mockRect.height() } returns 2400

            return mockContext
        }
    }
}