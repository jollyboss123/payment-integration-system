package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.payment.PaymentTokenRequest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

/**
 * @author jolly
 */
@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension::class)
@ActiveProfiles("dev")
class CircuitBreakerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient
) {
    @RepeatedTest(10)
    fun repeatTest(repetitionInfo: RepetitionInfo) {
        val attempt = 1 + repetitionInfo.currentRepetition
        val body = PaymentTokenRequest(
            merchantID = "JT07",
            invoiceNo = "12345-${attempt}",
            description = "default desc",
            amount = BigDecimal.valueOf(100),
            currencyCode = "MYR",
            paymentChannel = listOf("CC"),
            request3DS = false
        )

        webTestClient.post()
            .uri("/api/jolly/v1/payment/integration/2c2p")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
    }

//    @Test
//    fun test() {
//        val body = PaymentTokenRequest(
//            merchantID = "JT07",
//            invoiceNo = "12345-${System.currentTimeMillis()}",
//            description = "default desc",
//            amount = BigDecimal.valueOf(100),
//            currencyCode = "MYR",
//            paymentChannel = listOf("CC"),
//            request3DS = false
//        )
//
//        webTestClient.post()
//            .uri("/api/jolly/v1/payment/integration/2c2p")
//            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .bodyValue(body)
//            .exchange()
//            .expectStatus()
//            .isOk
//    }

    @Test
    fun test() {
        val body = PaymentRequest(
            merchantID = "JT07",
            invoiceNo = "123456",
            description = "default desc",
            amount = BigDecimal.valueOf(100),
            currencyCode = "MYR",
            paymentChannel = listOf("CC"),
            request3DS = false,
            cardNo = "4111111111111111",
            expiryMonth = "12",
            expiryYear = "23"
        )

        webTestClient.post()
            .uri("/api/jolly/v1/payment/integration/2c2p")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
    }
}
