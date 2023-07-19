package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.payment.PaymentResponseParams
import com.jolly.paymentintegrationsystem.payment.PaymentService
import com.jolly.paymentintegrationsystem.payment.PaymentTokenRequest
import com.jolly.paymentintegrationsystem.payment.PaymentTokenResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author jolly
 */
@RestController
@RequestMapping("/api/jolly/v1/payment/integration")
class TestController(
    private val paymentService: PaymentService,
    private val paymentClient: PaymentClient
) {
//    @CircuitBreaker(name = "test")
//    @PostMapping("/2c2p")
//    suspend fun payment(@RequestBody paymentTokenRequest: PaymentTokenRequest): PaymentTokenResponse {
//        return paymentService.generatePaymentToken(paymentTokenRequest)
//    }

    @PostMapping("/2c2p")
    suspend fun payment(@RequestBody paymentRequest: PaymentRequest): PaymentResponseParams {
        return paymentClient.doNon3DSPayment(paymentRequest)
    }
}
