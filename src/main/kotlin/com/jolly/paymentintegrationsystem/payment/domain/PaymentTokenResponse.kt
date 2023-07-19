package com.jolly.paymentintegrationsystem.payment.domain

import com.jolly.paymentintegrationsystem.domain.BaseResponse

/**
 * @author jolly
 */
data class PaymentTokenResponse (
    override val respDesc: String?,
    override val respCode: String?,
    override val payload: String?,

    val paymentToken: String?
) : BaseResponse
