package com.jolly.paymentintegrationsystem.payment

/**
 * @author jolly
 */
interface PaymentService {
    suspend fun generatePaymentToken(request: PaymentTokenRequest): PaymentTokenResponse
}
