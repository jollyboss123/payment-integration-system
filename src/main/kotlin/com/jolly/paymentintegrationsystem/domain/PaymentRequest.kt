package com.jolly.paymentintegrationsystem.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.jolly.paymentintegrationsystem.extensions.validateAndThrowOnFailure
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minimum
import java.math.BigDecimal

/**
 * @author jolly
 */
data class PaymentRequest @JsonCreator constructor(
    @JsonProperty("merchantID") val merchantID: String,
    @JsonProperty("invoiceNo") val invoiceNo: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("currencyCode") val currencyCode: String,
    @JsonProperty("paymentChannel") val paymentChannel: List<String>,
    @JsonProperty("request3DS") val request3DS: Boolean,
    @JsonProperty("securePayToken") val securePayToken: String? = null,
    @JsonProperty("cardNo") val cardNo: String,
    @JsonProperty("expiryMonth") val expiryMonth: String,
    @JsonProperty("expiryYear") val expiryYear: String
) {
//    constructor() : this("", "", "", BigDecimal.ONE, "", emptyList(), false, null, "","", "")
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
