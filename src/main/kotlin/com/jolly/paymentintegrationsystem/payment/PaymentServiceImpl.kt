package com.jolly.paymentintegrationsystem.payment

import com.auth0.jwt.JWT
import com.jolly.paymentintegrationsystem.extensions.exchangeToken
import com.jolly.paymentintegrationsystem.extensions.getClaimAsString
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

/**
 * @author jolly
 */
@Service
class PaymentServiceImpl(
    private val webClient: WebClient,
    @Value("\${payment.token.url}") private val paymentTokenUrl: String,
    @Value("\${merchant.secret.key}") private val merchantSecretKey: String
) : PaymentService {
    override suspend fun generatePaymentToken(request: PaymentTokenRequest): PaymentTokenResponse {
        val response: PaymentTokenResponse = webClient.exchangeToken(request, merchantSecretKey, paymentTokenUrl, PaymentTokenResponse::class.java)
        val jwt = JWT.decode(response.payload)
        val responseData = jwt.claims

        return PaymentTokenResponse(
            paymentToken = responseData.getClaimAsString("paymentToken"),
            respCode = responseData.getClaimAsString("respCode"),
            respDesc = responseData.getClaimAsString("respDesc"),
            payload = null
        )
    }
}
