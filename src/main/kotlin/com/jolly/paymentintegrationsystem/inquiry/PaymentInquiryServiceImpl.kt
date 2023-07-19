package com.jolly.paymentintegrationsystem.inquiry

import com.auth0.jwt.JWT
import com.jolly.paymentintegrationsystem.extensions.exchangeToken
import com.jolly.paymentintegrationsystem.extensions.getClaimAsLong
import com.jolly.paymentintegrationsystem.extensions.getClaimAsString
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

/**
 * @author jolly
 */
@Service
class PaymentInquiryServiceImpl(
    @Value("\${payment.inquiry.url}") private val paymentInquiryUrl: String,
    @Value("\${merchant.secret.key}") private val merchantSecretKey: String,
    private val webClient: WebClient
) : PaymentInquiryService {
    override suspend fun doPaymentInquiry(request: PaymentInquiryRequest): PaymentInquiryResponse {
        val paymentInquiryRequest = request.validate()
        val response: PaymentInquiryResponse = webClient.exchangeToken(paymentInquiryRequest, merchantSecretKey, paymentInquiryUrl, PaymentInquiryResponse::class.java)
        val jwt = JWT.decode(response.payload)
        val responseData = jwt.claims

        return PaymentInquiryResponse(
            agentCode = responseData.getClaimAsString("agentCode"),
            respCode = responseData.getClaimAsString("respCode"),
            respDesc = responseData.getClaimAsString("respDesc"),
            amount = BigDecimal.valueOf(responseData.getClaimAsLong("amount")),
            cardNo = responseData.getClaimAsString("cardNo"),
            paymentScheme = responseData.getClaimAsString("paymentScheme"),
            channelCode = responseData.getClaimAsString("channelCode"),
            currencyCode = responseData.getClaimAsString("currencyCode"),
            invoiceNo = responseData.getClaimAsString("invoiceNo"),
            issuerBank = responseData.getClaimAsString("issuerBank"),
            issuerCountry = responseData.getClaimAsString("issuerCountry"),
            referenceNo = responseData.getClaimAsString("referenceNo"),
            transactionDateTime = responseData.getClaimAsString("transactionDateTime"),
            eci = responseData.getClaimAsString("eci"),
            merchantID = responseData.getClaimAsString("merchantID"),
            payload = null
        )
    }
}
