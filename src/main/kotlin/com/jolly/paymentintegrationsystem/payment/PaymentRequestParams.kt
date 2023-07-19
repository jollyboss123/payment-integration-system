package com.jolly.paymentintegrationsystem.payment

import com.fasterxml.jackson.annotation.JsonProperty
import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation

/**
 * @author jolly
 */
data class PaymentRequestParams(
    val paymentToken: String,
    @get:JsonProperty("payment") val paymentParams: PaymentParams,
    val locale:String
) {
    fun validate(): PaymentRequestParams {
        Validation {
            PaymentRequestParams::paymentToken required {}
            PaymentRequestParams::paymentParams {
                PaymentParams::paymentCode required {
                    PaymentCode::channelCode required {}
                }
                PaymentParams::paymentData required {}
            }
        }.validateAndThrowOnFailure(this)

        return this
    }
}
