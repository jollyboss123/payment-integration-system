package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import com.jolly.paymentintegrationsystem.domain.PaymentResponse
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
class PaymentController(
    private val paymentClient: PaymentClient
) {
    companion object {
        const val PAYMENT_CIRCUIT_BREAKER_NAME = "payment"
    }

    @CircuitBreaker(name = PAYMENT_CIRCUIT_BREAKER_NAME)
    @PostMapping("/2c2p")
    suspend fun non3DSPayment(@RequestBody paymentRequest: PaymentRequest): PaymentResponse {
        val request = paymentRequest.validate()
        return paymentClient.doNon3DSPayment(request)
    }

//    @CircuitBreaker(name = PAYMENT_CIRCUIT_BREAKER_NAME)
    @PostMapping("/2c2p/bulk")
    suspend fun bulkNon3DSPayment(@RequestBody requests: PaymentRequest): PaymentRequest {
//        val paymentRequests = requests.map { it.validate() }
        val paymentRequests = requests.validate()
        paymentClient.doBulkNon3DSPayment(paymentRequests)
        return paymentRequests
    }
}
