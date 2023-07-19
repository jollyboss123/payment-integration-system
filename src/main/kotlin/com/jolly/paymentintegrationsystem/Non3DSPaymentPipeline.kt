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
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
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
    }

    @Bean(name = [PAYMENT_REQUESTS_CHANNEL])
    fun paymentRequests(): MessageChannel {
        return MessageChannels.direct().`object`
    }

    @Bean(name = [PAYMENT_REPLIES_CHANNEL])
    fun paymentReplies(): MessageChannel {
        return MessageChannels.direct().`object`
    }

    @Bean(name = ["payment.token.in"]) fun paymentTokenIn() = MessageChannels.direct().`object`
    @Bean(name = ["payment.token.out"]) fun paymentTokenOut() = MessageChannels.direct().`object`

    @Bean
    suspend fun inboundPaymentPipeline(@Qualifier(PAYMENT_REQUESTS_CHANNEL) paymentRequest: MessageChannel,
                               @Qualifier(PAYMENT_REPLIES_CHANNEL) paymentReplies: MessageChannel): IntegrationFlow =
        integrationFlow(paymentRequest) {
            channel(paymentTokenIn())
        }

    @ServiceActivator(inputChannel = "payment.token.in", outputChannel = "payment.token.out")
    suspend fun genPaymentToken(@Payload payload: PaymentTokenRequest): PaymentTokenResponse {
        return paymentService.generatePaymentToken(payload)
    }

    @Bean
    suspend fun outBoundPaymentPipeline(@Qualifier(PAYMENT_REPLIES_CHANNEL) paymentReplies: MessageChannel,
                                @Qualifier("payment.token.out") paymentTokenOut: MessageChannel) =
        integrationFlow(paymentTokenOut) {
            channel(paymentReplies)
        }

//    @Bean
//    fun paymentPipeline(): IntegrationFlow {
//        val scope = CoroutineScope(Dispatchers.Default)
//        return integrationFlow(paymentRequests()) {
//            enrichHeaders {
//                headerFunction<PaymentRequest>("card-no") {
//                    it.payload.cardNo
//                }
//                headerFunction<PaymentRequest>("expiry-month") {
//                    it.payload.expiryMonth
//                }
//                headerFunction<PaymentRequest>("expiry-year") {
//                    it.payload.expiryYear
//                }
//            }
//            transform<PaymentRequest> {
//                PaymentTokenRequest(
//                    merchantID = it.merchantID,
//                    invoiceNo = it.invoiceNo,
//                    description = it.description,
//                    amount = it.amount,
//                    currencyCode = it.currencyCode,
//                    paymentChannel = it.paymentChannel,
//                    request3DS = it.request3DS
//                )
//            }
////            handle { message -> scope.async {
////                paymentService.generatePaymentToken(message.payload as PaymentTokenRequest)
////            } }
////            handle { message -> scope.launch {
////                paymentService.generatePaymentToken(message.payload as PaymentTokenRequest)
////            } }
//            log<String>(LoggingHandler.Level.INFO, "com.jolly") {
//                "Payload: ${it.payload}"
//            }
//            handle { message ->
//                PaymentRequestParams(
//                    paymentToken = (message.payload as PaymentTokenResponse).paymentToken.takeIf { !it.isNullOrBlank() }
//                        ?: throw IllegalStateException("payment token is mandatory"),
//                    locale = "en",
//                    paymentParams = PaymentParams(
//                        paymentCode = PaymentCode(
//                            channelCode = "CC"
//                        ),
//                        paymentData = PaymentData(
//                            cardNo = message.headers["card-no"] as String,
//                            expiryMonth = message.headers["expiry-month"] as String,
//                            expiryYear = message.headers["expiry-year"] as String
//                        )
//                    )
//                )
//            }
//            channel(paymentReplies())
//        }
//    }
}

