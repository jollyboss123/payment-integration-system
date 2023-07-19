package com.jolly.paymentintegrationsystem.payment

import com.auth0.jwt.JWT
import com.jolly.paymentintegrationsystem.extensions.exchangeToken
import com.jolly.paymentintegrationsystem.extensions.getClaimAsString
import com.jolly.paymentintegrationsystem.payment.domain.PaymentRequestParams
import com.jolly.paymentintegrationsystem.payment.domain.PaymentResponseParams
import com.jolly.paymentintegrationsystem.payment.domain.PaymentTokenRequest
import com.jolly.paymentintegrationsystem.payment.domain.PaymentTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Mono

/**
 * @author jolly
 */
@Service
class PaymentServiceImpl(
    private val webClient: WebClient,
    @Value("\${payment.token.url}") private val paymentTokenUrl: String,
    @Value("\${payment.url}") private val paymentUrl: String,
    @Value("\${merchant.secret.key}") private val merchantSecretKey: String
) : PaymentService {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }
    override suspend fun generatePaymentToken(request: PaymentTokenRequest): PaymentTokenResponse {
        val paymentTokenRequest = request.validate()
        val response: PaymentTokenResponse = webClient.exchangeToken(paymentTokenRequest, merchantSecretKey, paymentTokenUrl, PaymentTokenResponse::class.java)
        val jwt = JWT.decode(response.payload)
        val responseData = jwt.claims

        return PaymentTokenResponse(
            paymentToken = responseData.getClaimAsString("paymentToken"),
            respCode = responseData.getClaimAsString("respCode"),
            respDesc = responseData.getClaimAsString("respDesc"),
            payload = null
        )
    }

    override suspend fun doPayment(request: PaymentRequestParams): PaymentResponseParams {
        val paymentRequestParams = request.validate()

        return webClient.post()
            .uri(paymentUrl)
            .body(Mono.just(paymentRequestParams), PaymentRequestParams::class.java)
            .awaitExchange { responseEntity ->
                val responseBody = responseEntity.awaitBody<PaymentResponseParams>()
                if (responseEntity.statusCode().isError) {
                    throw RestClientException("Payment request failed with status code: ${responseEntity.statusCode()}")
                } else if (responseBody.invoiceNo.isNullOrEmpty()) {
                    throw RestClientException("Invoice no received is null or empty, respCode: ${responseBody.respCode}, respDesc: ${responseBody.respDesc}")
                }
                else {
                    responseBody
                }
            }
    }
}
