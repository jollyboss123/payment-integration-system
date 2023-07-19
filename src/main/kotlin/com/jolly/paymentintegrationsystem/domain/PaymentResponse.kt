package com.jolly.paymentintegrationsystem.domain

import java.math.BigDecimal

/**
 * @author jolly
 */
data class PaymentResponse(
    val merchantID: String?,
    val invoiceNo: String?,
    val amount: BigDecimal?,
    val currencyCode: String?,
    val transactionDateTime: String?,
    val agentCode: String?,
    val channelCode: String?,
    val referenceNo: String?,
    val cardNo: String?,
    val issuerCountry: String?,
    val issuerBank: String?,
    val eci: String?,
    val paymentScheme: String?,
    val respCode: String?,
    val respDesc: String?
)
