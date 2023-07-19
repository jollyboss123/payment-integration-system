package com.jolly.paymentintegrationsystem.rabbitmq

import com.jolly.paymentintegrationsystem.PaymentClient.Companion.BULK_PAYMENT_REQUESTS_CHANNEL
import org.springframework.amqp.core.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

/**
 * @author jolly
 */
@Configuration
class InfrastructureConfig {
    @Bean
    fun initializeRabbitMqBroker(admin: AmqpAdmin): InitializingBean {
        return InitializingBean {
            mutableSetOf(BULK_PAYMENT_REQUESTS_CHANNEL)
                .forEach(Consumer { name: String -> define(admin, name) })
        }
    }

    private fun define(admin: AmqpAdmin, name: String): Queue {
        val q = QueueBuilder
            .durable(name)
            .build()
        val e = ExchangeBuilder
            .topicExchange(name)
            .build<Exchange>()
        val b = BindingBuilder
            .bind(q)
            .to(e)
            .with(name)
            .noargs()
        admin.declareQueue(q)
        admin.declareExchange(e)
        admin.declareBinding(b)
        return q
    }
}
