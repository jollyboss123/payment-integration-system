package com.jolly.paymentintegrationsystem.inquiry.domain

import com.jolly.paymentintegrationsystem.domain.BaseResponse
import java.math.BigDecimal

/**
 * @author jolly
 */
data class PaymentInquiryResponse(
    override val payload: String?,
    override val respCode: String?,
    override val respDesc: String?,

    val merchantID: String?,
    val invoiceNo:String?,
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
    val paymentScheme: String?
) : BaseResponse
