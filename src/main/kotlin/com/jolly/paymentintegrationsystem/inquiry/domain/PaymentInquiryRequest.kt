package com.jolly.paymentintegrationsystem.inquiry.domain

import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation

/**
 * @author jolly
 */
data class PaymentInquiryRequest(
    val paymentToken: String? = null,
    val merchantID: String,
    val invoiceNo: String,
    val locale: String
) {
    fun validate(): PaymentInquiryRequest {
        Validation {
            PaymentInquiryRequest::merchantID required {}
        }.validateAndThrowOnFailure(this)
        return this
    }
}
