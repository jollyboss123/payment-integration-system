package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minimum
import java.math.BigDecimal

/**
 * @author jolly
 */
data class PaymentRequest(
    val merchantID: String,
    val invoiceNo: String,
    val description: String,
    val amount: BigDecimal,
    val currencyCode: String,
    val paymentChannel: List<String>,
    val request3DS: Boolean,
    val securePayToken: String? = null,
    val cardNo: String,
    val expiryMonth: String,
    val expiryYear: String
) {
    fun validate(): PaymentRequest {
        Validation {
            PaymentRequest::merchantID required {}
            PaymentRequest::invoiceNo required {}
            PaymentRequest::description required {}
            PaymentRequest::amount required {}
            PaymentRequest::amount {
                minimum(BigDecimal.ONE)
            }
            PaymentRequest::currencyCode required {}
        }.validateAndThrowOnFailure(this)
        return this
    }
}
