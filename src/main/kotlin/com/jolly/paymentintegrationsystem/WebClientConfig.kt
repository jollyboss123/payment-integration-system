package com.jolly.paymentintegrationsystem

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * @author jolly
 */
@Configuration
class WebClientConfig {
    @Bean
    fun webClient(builder: WebClient.Builder) : WebClient =
        builder
            .baseUrl("http://localhost:8081")
            .build()
}
