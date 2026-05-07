package com.estapar.parking.shared

import com.estapar.parking.shared.metrics.ParkingMetrics
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Test

class ParkingMetricsTest {
    private val registry = SimpleMeterRegistry()
    private val metrics = ParkingMetrics(registry)

    @Test
    fun `records ENTRY recorded event`() {
        metrics.recordWebhookEvent("ENTRY", "recorded")

        registry
            .get("parking.webhook.events")
            .tag("type", "ENTRY")
            .tag("outcome", "recorded")
            .counter()
            .count() shouldBe 1.0
    }

    @Test
    fun `counts duplicate events independently`() {
        metrics.recordWebhookEvent("ENTRY", "duplicate")
        metrics.recordWebhookEvent("ENTRY", "duplicate")

        registry
            .get("parking.webhook.events")
            .tag("type", "ENTRY")
            .tag("outcome", "duplicate")
            .counter()
            .count() shouldBe 2.0
    }

    @Test
    fun `different event types are counted independently`() {
        metrics.recordWebhookEvent("ENTRY", "recorded")
        metrics.recordWebhookEvent("EXIT", "recorded")

        registry
            .get("parking.webhook.events")
            .tag("type", "ENTRY")
            .tag("outcome", "recorded")
            .counter()
            .count() shouldBe 1.0
        registry
            .get("parking.webhook.events")
            .tag("type", "EXIT")
            .tag("outcome", "recorded")
            .counter()
            .count() shouldBe 1.0
    }
}
