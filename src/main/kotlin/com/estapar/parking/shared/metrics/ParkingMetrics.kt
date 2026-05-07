package com.estapar.parking.shared.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ParkingMetrics(
    private val registry: MeterRegistry,
) {
    fun recordWebhookEvent(
        type: String,
        outcome: String,
    ) {
        Counter
            .builder("parking.webhook.events")
            .description("Total webhook events processed")
            .tag("type", type)
            .tag("outcome", outcome)
            .register(registry)
            .increment()
    }
}
