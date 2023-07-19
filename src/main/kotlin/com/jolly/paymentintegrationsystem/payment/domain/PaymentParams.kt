package com.jolly.paymentintegrationsystem.payment.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation

/**
 * @author jolly
 */
data class PaymentParams(
    @get:JsonProperty("code") val paymentCode: PaymentCode,
    @get:JsonProperty("data") val paymentData: PaymentData
) {
    fun validate(): PaymentParams {
        Validation {
            PaymentParams::paymentCode required {}
            PaymentParams::paymentData required {}
        }.validateAndThrowOnFailure(this)
        return this
    }
}
