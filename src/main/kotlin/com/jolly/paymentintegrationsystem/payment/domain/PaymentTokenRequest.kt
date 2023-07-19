package com.jolly.paymentintegrationsystem.payment.domain

import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minimum
import java.math.BigDecimal

/**
 * @author jolly
 */
data class PaymentTokenRequest(
    val merchantID: String,
    val invoiceNo: String,
    val description: String,
    val amount: BigDecimal,
    val currencyCode: String,
    val paymentChannel: List<String>,
    val request3DS: Boolean
) {
    fun validate(): PaymentTokenRequest {
        Validation {
            PaymentTokenRequest::merchantID required {}
            PaymentTokenRequest::invoiceNo required {}
            PaymentTokenRequest::description required {}
            PaymentTokenRequest::amount required {}
            PaymentTokenRequest::amount {
                minimum(BigDecimal.ONE) hint "amount must be greater than 0"
            }
            PaymentTokenRequest::currencyCode required {}
        }.validateAndThrowOnFailure(this)

        return this
    }
}
