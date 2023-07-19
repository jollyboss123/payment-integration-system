package com.jolly.paymentintegrationsystem

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author jolly
 */
@RestController
@RequestMapping("/api/jolly/v1/payment/integration")
class PaymentController(
    private val paymentClient: PaymentClient
) {
//    @CircuitBreaker(name = "test")
//    @PostMapping("/2c2p")
//    suspend fun payment(@RequestBody paymentTokenRequest: PaymentTokenRequest): PaymentTokenResponse {
//        return paymentService.generatePaymentToken(paymentTokenRequest)
//    }

    @PostMapping("/2c2p")
    suspend fun non3DSPayment(@RequestBody paymentRequest: PaymentRequest): PaymentResponse {
        return paymentClient.doNon3DSPayment(paymentRequest)
    }
}
