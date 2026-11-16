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

package com.onlinepayments.sdk.client.android.facade.listeners

import java.util.Collections
import java.util.IdentityHashMap

/**
 * Prevents the default [GenericResponseListener.onFailure] from calling itself endlessly through the
 * default implementations of the deprecated `onApiError` and `onException`.
 *
 * The state is per thread and per listener instance, so other listeners are not affected.
 */
internal object ListenerFailureRouting {
    private val routing = ThreadLocal.withInitial<MutableSet<Any>> {
        Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
    }

    /** Runs [action] unless the failure of [listener] is already being routed on this thread. */
    fun route(listener: Any, action: () -> Unit) {
        val active = routing.get()
        if (!active.add(listener)) {
            return
        }
        try {
            action()
        } finally {
            active.remove(listener)
        }
    }
}
