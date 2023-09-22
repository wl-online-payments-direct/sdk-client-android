package com.onlinepayments.sdk.client.android.asynctask

import android.content.Context
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator
import com.onlinepayments.sdk.client.android.listener.SurchargeCalculationResponseListener
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.surcharge.request.CardSource
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @param context [Context] used for reading device metadata which is sent to the Online Payments gateway
 * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
 * @param cardSource contains the card or token for which the Surcharge should be calculated
 * @param communicator [C2sCommunicator] which does the communication to the Online Payments gateway
 * @param listener [SurchargeCalculationResponseListener] which will be called by the AsyncTask when the [SurchargeCalculationResponse] is loaded
 */
internal class SurchargeCalculationNetworkTask(
    private val context: Context,
    private val amountOfMoney: AmountOfMoney,
    private val cardSource: CardSource,
    private val communicator: C2sCommunicator,
    private val listener: SurchargeCalculationResponseListener
) {
    private val surchargeCalculationCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun getSurchargeCalculation() {
        surchargeCalculationCoroutineScope.launch {
            val apiResponse = communicator.getSurchargeCalculation(amountOfMoney, cardSource, context)

            val apiResponseError : ErrorResponse? = apiResponse.error
            val surchargeCalculationResponse: SurchargeCalculationResponse? = apiResponse.data

            if (apiResponseError == null) {
                if (surchargeCalculationResponse != null) {
                    listener.onSuccess(surchargeCalculationResponse)
                } else {
                    val error = ErrorResponse("Empty Response without Error")
                    listener.onApiError(error)
                }
            } else {
                if (apiResponseError.throwable != null) {
                    listener.onException(apiResponseError.throwable)
                } else {
                    listener.onApiError(apiResponseError)
                }
            }
        }
    }
}