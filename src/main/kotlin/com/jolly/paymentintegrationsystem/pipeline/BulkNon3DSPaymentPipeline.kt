package com.jolly.paymentintegrationsystem.pipeline

import com.jolly.paymentintegrationsystem.PaymentClient.Companion.BULK_PAYMENT_REQUESTS_CHANNEL
import com.jolly.paymentintegrationsystem.PaymentClient.Companion.PAYMENT_REQUESTS_CHANNEL
import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.*
import org.springframework.integration.json.ObjectToJsonTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.support.ErrorMessage

/**
 * @author jolly
 */
@Configuration
@EnableIntegration
class BulkNon3DSPaymentPipeline {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Bean(name = [BULK_PAYMENT_REQUESTS_CHANNEL])
    fun bulkPaymentIn(): MessageChannel = MessageChannels.direct().`object`

    @Bean
    @ServiceActivator(inputChannel = "errorChannel")
    fun errorChannelHandler(): MessageHandler {
        return MessageHandler { message: Message<*> ->
            val errorMessage = message as ErrorMessage
            logger.error("Error occurred in the integration flow: ${errorMessage.payload.message}")
        }
    }

    @Bean
    fun outboundMqFlow(template: AmqpTemplate,
                       @Qualifier(BULK_PAYMENT_REQUESTS_CHANNEL) bulkPaymentIn: MessageChannel): IntegrationFlow =
        integrationFlow(bulkPaymentIn) {
            log()
            transform(Transformers.toJson(ObjectToJsonTransformer.ResultType.STRING))
//            handle<PaymentRequest> { payload, headers ->
//                logger.info("payload sent: $payload, id: ${headers.id}")
//            }
            handle(Amqp.outboundAdapter(template)
                .exchangeName(BULK_PAYMENT_REQUESTS_CHANNEL)
                .routingKey(BULK_PAYMENT_REQUESTS_CHANNEL)
                .`object`)
        }

    @Bean
    fun inboundMqFlow(connectionFactory: ConnectionFactory): IntegrationFlow =
        integrationFlow(Amqp.inboundAdapter(connectionFactory, BULK_PAYMENT_REQUESTS_CHANNEL)) {
            log()
            transform(Transformers.fromJson(PaymentRequest::class.java))
//            handle<PaymentRequest> { payload, headers ->
//                logger.info("payload received: $payload, id: ${headers.id}")
//            }
            channel(PAYMENT_REQUESTS_CHANNEL)
        }
}
