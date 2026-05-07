package com.estapar.parking.parkingsession.infrastructure.webhook

import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.parkingsession.infrastructure.webhook.dto.WebhookRequest

object WebhookEventMapper {
    fun toEvent(request: WebhookRequest): WebhookEvent {
        val plate = LicensePlate(request.licensePlate)
        return when (request.eventType) {
            "ENTRY" ->
                WebhookEvent.Entry(
                    licensePlate = plate,
                    entryTime = requireNotNull(request.entryTime) { "entry_time is required for ENTRY events" },
                )

            "PARKED" ->
                WebhookEvent.Parked(
                    licensePlate = plate,
                    lat = requireNotNull(request.lat) { "lat is required for PARKED events" },
                    lng = requireNotNull(request.lng) { "lng is required for PARKED events" },
                )

            "EXIT" ->
                WebhookEvent.Exit(
                    licensePlate = plate,
                    exitTime = requireNotNull(request.exitTime) { "exit_time is required for EXIT events" },
                )

            else -> error("Unknown event_type '${request.eventType}'")
        }
    }
}
