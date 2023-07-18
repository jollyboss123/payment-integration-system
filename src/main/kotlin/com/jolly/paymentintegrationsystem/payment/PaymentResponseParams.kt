package com.jolly.paymentintegrationsystem.payment

import com.jolly.paymentintegrationsystem.BaseResponse

/**
 * @author jolly
 */
data class PaymentResponseParams(
    override val respDesc: String,
    override val respCode: String,
    override val payload: String,

    val channelCode: String,
    val invoiceNo: String
) : BaseResponse
