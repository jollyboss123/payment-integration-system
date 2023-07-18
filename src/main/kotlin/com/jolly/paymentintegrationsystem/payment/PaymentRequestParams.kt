package com.jolly.paymentintegrationsystem.payment

import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation

/**
 * @author jolly
 */
data class PaymentRequestParams(
    val paymentToken: String,
    val paymentParams: PaymentParams,
    val locale:String
) {
    fun validate(): PaymentRequestParams {
        Validation {
            PaymentRequestParams::paymentToken required {}
        }.validateAndThrowOnFailure(this)

        return this
    }
}
