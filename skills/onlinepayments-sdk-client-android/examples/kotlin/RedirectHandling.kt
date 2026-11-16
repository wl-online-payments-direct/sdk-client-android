package com.example.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// [START pending-payment-store]
// Remembers the order that waits for its result while the customer is on the 3-D Secure page.
// Persist it, because the system can stop the app meanwhile. Store only the order id: never card
// data or session data.
interface PendingPaymentStore {
    fun readOrderId(): String?
    fun saveOrderId(orderId: String)
    fun clear()
}

// commit() writes before it returns (apply() writes later): the app can be stopped as soon as the
// browser opens.
@SuppressLint("ApplySharedPref")
class SharedPreferencesPendingPaymentStore(context: Context) : PendingPaymentStore {
    private val preferences =
        context.applicationContext.getSharedPreferences("pending_payment", Context.MODE_PRIVATE)

    override fun readOrderId(): String? = preferences.getString("orderId", null)

    override fun saveOrderId(orderId: String) {
        preferences.edit().putString("orderId", orderId).commit()
    }

    override fun clear() {
        preferences.edit().remove("orderId").commit()
    }
}
// [END pending-payment-store]

// [START handle-payment-result]
fun handlePaymentResult(
    activity: Activity,
    result: MerchantPaymentResponse,
    orderId: String,
    pendingPayments: PendingPaymentStore,
) {
    val redirectUrl = result.redirectUrl
    if (redirectUrl != null) {
        // For example 3-D Secure or a third-party payment page. Open the URL exactly as received:
        // do not build or change it. Your backend sets the payment's returnUrl to a deep link of
        // this app, so the customer comes back to the app afterwards.
        pendingPayments.saveOrderId(orderId)
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl)))
        } catch (_: ActivityNotFoundException) {
            pendingPayments.clear()
            showStatusUnknown() // the payment exists; let the customer check the order later
        }
        return
    }

    // The status comes from your backend. The app never decides that a payment succeeded.
    showPaymentOutcome(result.status)
}
// [END handle-payment-result]

// [START handle-return-deep-link]
// The start of the returnUrl your backend sets in Create Payment. Your manifest has an intent filter
// for it (see references/android.md).
const val RETURN_URL_PREFIX = "myshop://payment-return"

// Runs when the customer comes back. Acts only when a payment waits for its result: the same link
// can be delivered again, for example after the Activity is recreated.
suspend fun onPaymentReturn(pendingPayments: PendingPaymentStore) {
    val orderId = pendingPayments.readOrderId() ?: return
    pendingPayments.clear()

    // Do not read the outcome from the link: ask your backend for the status.
    try {
        showPaymentOutcome(fetchPaymentStatus(orderId).status)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        showStatusUnknown() // the outcome is unknown, not failed: let the customer check the order later
    }
}

// Handle the link in the Activity that is the root of the app (the one with the launcher intent
// filter and android:launchMode="singleTask" or "singleTop"), not in the checkout screen: the
// customer can come back while another screen is shown.
class MainActivity : ComponentActivity() {
    private val pendingPayments by lazy { SharedPreferencesPendingPaymentStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the system stopped the app while the customer was on the 3-D Secure page, the deep link
        // starts the app again: the launch intent holds it. onPaymentReturn ignores an old link
        // because no payment waits any more.
        handleReturnLink(intent)
    }

    // The app is running: the link arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleReturnLink(intent)
    }

    private fun handleReturnLink(intent: Intent?) {
        val link = intent?.data?.toString() ?: return
        if (link.startsWith(RETURN_URL_PREFIX)) {
            lifecycleScope.launch { onPaymentReturn(pendingPayments) }
        }
    }
}
// [END handle-return-deep-link]
