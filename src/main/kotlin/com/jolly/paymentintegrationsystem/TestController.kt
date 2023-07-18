package com.jolly.paymentintegrationsystem

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
    private val paymentService: PaymentService
) {
    @CircuitBreaker(name = "test")
    @PostMapping("/2c2p")
    suspend fun payment(@RequestBody paymentTokenRequest: PaymentTokenRequest): PaymentTokenResponse {
        val request = paymentTokenRequest.validate()
        return paymentService.generatePaymentToken(request)
    }
}
