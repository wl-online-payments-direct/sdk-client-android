/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.configuration

/**
 * Optional configuration of the SDK.
 *
 * @param environmentIsProduction selects the Google Pay environment (production or test). Use `true` in release builds.
 * @param appIdentifier the name and version of your app, sent with the client meta information
 * @param sdkIdentifier overrides the SDK identifier; leave `null`
 * @param loggingEnabled when `true`, the SDK writes its HTTP requests and responses (URL, headers including the
 * session id, and bodies) to Logcat. Off by default; must stay off in production. The setting is process-wide: the
 * SDK instance created last decides.
 */
data class SdkConfiguration(
    val environmentIsProduction: Boolean?,
    val appIdentifier: String?,
    val sdkIdentifier: String?,
    val loggingEnabled: Boolean = false,
)
