package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import com.jolly.paymentintegrationsystem.domain.PaymentResponse
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
        const val BULK_PAYMENT_REQUESTS_CHANNEL = "bulk.payment.request"
    }
    @Gateway(requestChannel = PAYMENT_REQUESTS_CHANNEL, replyChannel = PAYMENT_REPLIES_CHANNEL)
    suspend fun doNon3DSPayment(request: PaymentRequest) : PaymentResponse

    @Gateway(requestChannel = BULK_PAYMENT_REQUESTS_CHANNEL)
    fun doBulkNon3DSPayment(requests: PaymentRequest)
}
