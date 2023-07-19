package com.jolly.paymentintegrationsystem.payment

import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation

/**
 * @author jolly
 */
data class PaymentCode(
    val channelCode: String,
    val agentCode: String? = null,
    val agentChannelCode: String? = null
) {
    fun validate(): PaymentCode {
        Validation {
            PaymentCode::channelCode required {}
        }.validateAndThrowOnFailure(this)
        return this
    }
}
