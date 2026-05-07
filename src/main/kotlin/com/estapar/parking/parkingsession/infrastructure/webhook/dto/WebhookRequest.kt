package com.estapar.parking.parkingsession.infrastructure.webhook.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class WebhookRequest(
    @JsonProperty("license_plate") val licensePlate: String,
    @JsonProperty("event_type") val eventType: String,
    @JsonProperty("entry_time") val entryTime: Instant? = null,
    @JsonProperty("exit_time") val exitTime: Instant? = null,
    @JsonProperty("lat") val lat: Double? = null,
    @JsonProperty("lng") val lng: Double? = null,
)
