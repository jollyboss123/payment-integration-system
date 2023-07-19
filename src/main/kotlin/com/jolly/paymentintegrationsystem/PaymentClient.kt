package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.payment.PaymentResponseParams
import com.jolly.paymentintegrationsystem.payment.PaymentTokenRequest
import com.jolly.paymentintegrationsystem.payment.PaymentTokenResponse
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway

/**
 * @author jolly
 */
@MessagingGateway
interface PaymentClient {
    companion object {
        const val PAYMENT_REQUESTS_CHANNEL = "payment.request"
        const val PAYMENT_REPLIES_CHANNEL = "payment.reply"
    }
    @Gateway(requestChannel = PAYMENT_REQUESTS_CHANNEL, replyChannel = PAYMENT_REPLIES_CHANNEL)
    suspend fun doNon3DSPayment(request: PaymentRequest) : PaymentResponseParams
}
