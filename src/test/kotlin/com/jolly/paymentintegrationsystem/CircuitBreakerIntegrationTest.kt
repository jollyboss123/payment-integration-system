package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import org.junit.jupiter.api.BeforeEach
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
import java.time.Duration


/**
 * @author jolly
 */
@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension::class)
@ActiveProfiles("dev")
class CircuitBreakerIntegrationTest(
    @Autowired private var webTestClient: WebTestClient
) {
    @BeforeEach
    fun setUp() {
        webTestClient = webTestClient
            .mutate()
            .responseTimeout(Duration.ofMillis(10000))
            .build()
    }

    @RepeatedTest(10)
    fun repeatTest(repetitionInfo: RepetitionInfo) {
        val attempt = 1 + repetitionInfo.currentRepetition
        val body = PaymentRequest(
            merchantID = "JT07",
            invoiceNo = "123456-$attempt",
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

    @Test
    fun test() {
        val body = PaymentRequest(
            merchantID = "JT07",
            invoiceNo = "123456-${System.currentTimeMillis()}",
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

    @Test
    fun bulkTest() {
//        val body = listOf(PaymentRequest(
//            merchantID = "JT07",
//            invoiceNo = "123456",
//            description = "default desc",
//            amount = BigDecimal.valueOf(100),
//            currencyCode = "MYR",
//            paymentChannel = listOf("CC"),
//            request3DS = false,
//            cardNo = "4111111111111111",
//            expiryMonth = "12",
//            expiryYear = "23"
//        ))
        val body = PaymentRequest(
            merchantID = "JT07",
            invoiceNo = "123456-${System.currentTimeMillis()}",
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
            .uri("/api/jolly/v1/payment/integration/2c2p/bulk")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun newApiTest() {
        val body = PaymentRequest(
            merchantID = "JT07",
            invoiceNo = "123456-${System.currentTimeMillis()}",
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
            .uri("/api/jolly/v1/payment/integration/2c2p/new")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
    }
}
