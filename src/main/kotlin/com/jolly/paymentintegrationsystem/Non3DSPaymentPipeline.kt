package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.PaymentClient.Companion.PAYMENT_REPLIES_CHANNEL
import com.jolly.paymentintegrationsystem.PaymentClient.Companion.PAYMENT_REQUESTS_CHANNEL
import com.jolly.paymentintegrationsystem.inquiry.PaymentInquiryService
import com.jolly.paymentintegrationsystem.payment.*
import kotlinx.coroutines.*
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
import org.springframework.messaging.Message
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
        val logger = LoggerFactory.getLogger(this::class.java)
        const val PAYMENT_TOKEN_IN = "payment.token.in"
        const val PAYMENT_TOKEN_OUT = "payment.token.out"
        const val PAYMENT_IN = "payment.in"
        const val PAYMENT_OUT = "payment.out"
        const val CARD_NO = "card.no"
        const val EXPIRY_YEAR = "expiry.year"
        const val EXPIRY_MONTH = "expiry.month"
        const val LOCALE = "en"
        const val CREDIT_CARD_CHANNEL = "CC"
    }

    @Bean(name = [PAYMENT_REQUESTS_CHANNEL])
    fun paymentRequests(): MessageChannel {
        return MessageChannels.direct().`object`
    }

    @Bean(name = [PAYMENT_REPLIES_CHANNEL])
    fun paymentReplies(): MessageChannel {
        return MessageChannels.direct().`object`
    }

    @Bean(name = [PAYMENT_TOKEN_IN]) fun paymentTokenIn() = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_TOKEN_OUT]) fun paymentTokenOut() = MessageChannels.direct().`object`

    @Bean(name = [PAYMENT_IN]) fun paymentIn() = MessageChannels.direct().`object`
    @Bean(name = [PAYMENT_OUT]) fun paymentOut() = MessageChannels.direct().`object`

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
    suspend fun genPaymentToken(@Payload payload: PaymentTokenRequest): PaymentTokenResponse = paymentService.generatePaymentToken(payload)

    @Bean
    suspend fun preparePayment(@Qualifier(PAYMENT_TOKEN_OUT) paymentTokenOut: MessageChannel): IntegrationFlow =
        integrationFlow(paymentTokenOut) {
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                "done payment token ${it.payload}"
            }
            handle<PaymentTokenResponse> { payload, headers ->
                toPaymentRequestParams(
                    payload, headers[CARD_NO] as String,
                    headers[EXPIRY_MONTH] as String, headers[EXPIRY_YEAR] as String)
            }
            channel(PAYMENT_IN)
        }

    @Transformer
    private fun toPaymentRequestParams(@Payload payload: PaymentTokenResponse, @Header(CARD_NO) cardNo: String,
                                       @Header(EXPIRY_MONTH) expiryMonth: String, @Header(EXPIRY_YEAR) expiryYear: String) : PaymentRequestParams {
        val paymentToken = payload.paymentToken.takeIf { !it.isNullOrBlank() } ?: throw IllegalStateException("payment token is mandatory")
        return PaymentRequestParams(
            paymentToken,
            PaymentParams(
                PaymentCode(
                    CREDIT_CARD_CHANNEL
                ),
                PaymentData(
                    cardNo = cardNo,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear
                )
            ),
            LOCALE
        )
    }

    @ServiceActivator(inputChannel = PAYMENT_IN, outputChannel = PAYMENT_OUT, async = true.toString())
    suspend fun doPayment(@Payload payload: PaymentRequestParams): PaymentResponseParams = paymentService.doPayment(payload)

    @Bean
    suspend fun outBoundPaymentPipeline(@Qualifier(PAYMENT_REPLIES_CHANNEL) paymentReplies: MessageChannel,
                                @Qualifier(PAYMENT_OUT) paymentOut: MessageChannel): IntegrationFlow =
        integrationFlow(paymentOut) {
            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
                "done payment process ${it.payload} ${it.headers}"
            }
            channel(paymentReplies)
        }
}

