package com.jolly.paymentintegrationsystem.payment

import com.jolly.paymentintegrationsystem.payment.domain.PaymentRequestParams
import com.jolly.paymentintegrationsystem.payment.domain.PaymentResponseParams
import com.jolly.paymentintegrationsystem.payment.domain.PaymentTokenRequest
import com.jolly.paymentintegrationsystem.payment.domain.PaymentTokenResponse

/**
 * @author jolly
 */
interface PaymentService {
    suspend fun generatePaymentToken(request: PaymentTokenRequest): PaymentTokenResponse
    suspend fun doPayment(request: PaymentRequestParams): PaymentResponseParams
}
