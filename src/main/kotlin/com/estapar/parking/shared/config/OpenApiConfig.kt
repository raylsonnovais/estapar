package com.estapar.parking.shared.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Estapar Parking API")
                    .version("1.0.0")
                    .description("Parking garage management system — webhook ingestion and revenue reporting"),
            )
}
