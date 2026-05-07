package com.estapar.parking.shared.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class BeanConfig {
    @Bean
    fun garageSimulatorRestClient(
        @Value("\${garage.simulator.base-url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient = builder.baseUrl(baseUrl).build()
}
