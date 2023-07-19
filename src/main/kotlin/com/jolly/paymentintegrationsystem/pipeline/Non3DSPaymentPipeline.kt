package com.jolly.paymentintegrationsystem.pipeline

import com.jolly.paymentintegrationsystem.PaymentClient.Companion.PAYMENT_REPLIES_CHANNEL
import com.jolly.paymentintegrationsystem.PaymentClient.Companion.PAYMENT_REQUESTS_CHANNEL
import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import com.jolly.paymentintegrationsystem.domain.PaymentResponse
import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryRequest
import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryResponse
import com.jolly.paymentintegrationsystem.inquiry.PaymentInquiryService
import com.jolly.paymentintegrationsystem.payment.*
import com.jolly.paymentintegrationsystem.payment.domain.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.annotation.Transformer
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.handler.LoggingHandler
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
/**
 * @author jolly
 */
@Configuration
@EnableIntegration
class Non3DSPaymentPipeline(
    private val paymentService: PaymentService,
    private val paymentInquiryService: PaymentInquiryService
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
        const val PAYMENT_TOKEN_IN = "payment.token.in"
        const val PAYMENT_TOKEN_OUT = "payment.token.out"
        const val PAYMENT_IN = "payment.in"
        const val PAYMENT_OUT = "payment.out"
        const val PAYMENT_INQUIRY_IN = "payment.inquiry.in"
        const val PAYMENT_INQUIRY_OUT = "payment.inquiry.out"

        const val CARD_NO = "card.no"
        const val EXPIRY_YEAR = "expiry.year"
        const val EXPIRY_MONTH = "expiry.month"
        const val MERCHANT_ID = "merchant.id"
        const val LOCALE = "en"
        const val CREDIT_CARD_CHANNEL = "CC"
    }

    @Bean(name = [PAYMENT_REQUESTS_CHANNEL]) fun paymentRequests(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_REPLIES_CHANNEL]) fun paymentReplies(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_TOKEN_IN]) fun paymentTokenIn(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_TOKEN_OUT]) fun paymentTokenOut(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_IN]) fun paymentIn(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_OUT]) fun paymentOut(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_INQUIRY_IN]) fun paymentInquiryIn(): MessageChannel = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_INQUIRY_OUT]) fun paymentInquiryOut(): MessageChannel = MessageChannels.direct().`object`

    @Bean
    suspend fun inboundPaymentPipeline(@Qualifier(PAYMENT_REQUESTS_CHANNEL) paymentRequest: MessageChannel,
                               @Qualifier(PAYMENT_TOKEN_IN) paymentTokenIn: MessageChannel): IntegrationFlow =
        integrationFlow(paymentRequest) {
            enrichHeaders {
                this.headerFunction<PaymentRequest>(CARD_NO) {
                    it.payload.cardNo
                }
                this.headerFunction<PaymentRequest>(EXPIRY_MONTH) {
                    it.payload.expiryMonth
                }
                this.headerFunction<PaymentRequest>(EXPIRY_YEAR) {
                    it.payload.expiryYear
                }
                this.headerFunction<PaymentRequest>(MERCHANT_ID) {
                    it.payload.merchantID
                }
            }
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                it.headers.map { header ->
                    "${header.key} = ${header.value}"
                }
            }
            transform<PaymentRequest> {
                PaymentTokenRequest(
                    merchantID = it.merchantID,
                    invoiceNo = it.invoiceNo,
                    description = it.description,
                    amount = it.amount,
                    currencyCode = it.currencyCode,
                    paymentChannel = it.paymentChannel,
                    request3DS = false
                )
            }
            channel(paymentTokenIn)
        }

    @ServiceActivator(inputChannel = PAYMENT_TOKEN_IN, outputChannel = PAYMENT_TOKEN_OUT, async = true.toString())
    suspend fun genPaymentToken(@Payload payload: PaymentTokenRequest): PaymentTokenResponse {
        logger.debug("doing generate payment token")
        return paymentService.generatePaymentToken(payload)
    }

    @Bean
    suspend fun preparePayment(@Qualifier(PAYMENT_TOKEN_OUT) paymentTokenOut: MessageChannel,
                               @Qualifier(PAYMENT_IN) paymentIn: MessageChannel): IntegrationFlow =
        integrationFlow(paymentTokenOut) {
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                "done payment token ${it.payload}"
            }
            filter<PaymentTokenResponse> {
                it.respCode == "0000"
            }
            handle<PaymentTokenResponse> { payload, headers ->
                toPaymentRequestParams(
                    payload, headers[CARD_NO] as String,
                    headers[EXPIRY_MONTH] as String, headers[EXPIRY_YEAR] as String)
            }
            channel(paymentIn)
        }

    @ServiceActivator(inputChannel = PAYMENT_IN, outputChannel = PAYMENT_OUT, async = true.toString())
    suspend fun doPayment(@Payload payload: PaymentRequestParams): PaymentResponseParams {
        logger.debug("doing payment")
        return paymentService.doPayment(payload)
    }

    @Bean
    suspend fun preparePaymentInquiry(@Qualifier(PAYMENT_OUT) paymentOut: MessageChannel,
                                      @Qualifier(PAYMENT_INQUIRY_IN) paymentInquiryIn: MessageChannel): IntegrationFlow =
        integrationFlow(paymentOut) {
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                "done payment ${it.payload}"
            }
            handle<PaymentResponseParams> { payload, headers ->
                toPaymentInquiryRequest(
                    payload, headers[MERCHANT_ID] as String)
            }
            channel(paymentInquiryIn)
        }

    @ServiceActivator(inputChannel = PAYMENT_INQUIRY_IN, outputChannel = PAYMENT_INQUIRY_OUT, async = true.toString())
    suspend fun doPaymentInquiry(@Payload payload: PaymentInquiryRequest): PaymentInquiryResponse {
        logger.debug("doing payment inquiry")
        return paymentInquiryService.doPaymentInquiry(payload)
    }

    @Bean
    suspend fun outBoundPaymentPipeline(@Qualifier(PAYMENT_REPLIES_CHANNEL) paymentReplies: MessageChannel,
                                @Qualifier(PAYMENT_INQUIRY_OUT) paymentInquiryOut: MessageChannel): IntegrationFlow =
        integrationFlow(paymentInquiryOut) {
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                "done payment process ${it.payload} ${it.headers}"
            }
            transform<PaymentInquiryResponse> {
                PaymentResponse(
                    merchantID = it.merchantID,
                    invoiceNo = it.invoiceNo,
                    amount = it.amount,
                    currencyCode = it.currencyCode,
                    transactionDateTime = it.transactionDateTime,
                    agentCode = it.agentCode,
                    channelCode = it.channelCode,
                    referenceNo = it.referenceNo,
                    cardNo = it.cardNo,
                    issuerCountry = it.issuerCountry,
                    issuerBank = it.issuerBank,
                    eci = it.eci,
                    paymentScheme = it.paymentScheme,
                    respCode = it.respCode!!,
                    respDesc = it.respDesc!!
                )
            }
            channel(paymentReplies)
        }

    @Transformer
    private fun toPaymentRequestParams(@Payload payload: PaymentTokenResponse, @Header(CARD_NO) cardNo: String,
                                       @Header(EXPIRY_MONTH) expiryMonth: String, @Header(EXPIRY_YEAR) expiryYear: String) : PaymentRequestParams {
        val paymentToken = payload.paymentToken.takeIf { !it.isNullOrBlank() } ?: throw IllegalStateException("payment token is mandatory")
        return PaymentRequestParams(
            paymentToken = paymentToken,
            paymentParams = PaymentParams(
                paymentCode = PaymentCode(
                    CREDIT_CARD_CHANNEL
                ),
                paymentData = PaymentData(
                    cardNo = cardNo,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear
                )
            ),
            locale = LOCALE
        )
    }

    @Transformer
    private fun toPaymentInquiryRequest(@Payload payload: PaymentResponseParams, @Header(MERCHANT_ID) merchantId: String): PaymentInquiryRequest {
        val invoiceNo = payload.invoiceNo.takeIf { !it.isNullOrBlank() } ?: throw IllegalStateException("invoice no is mandatory")
        return PaymentInquiryRequest(
            merchantID = merchantId,
            invoiceNo = invoiceNo,
            locale = LOCALE
        )
    }
}


