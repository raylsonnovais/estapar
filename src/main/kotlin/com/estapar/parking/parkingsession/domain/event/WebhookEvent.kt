package com.estapar.parking.parkingsession.domain.event

import com.estapar.parking.parkingsession.domain.LicensePlate
import java.time.Instant

sealed class WebhookEvent {
    data class Entry(
        val licensePlate: LicensePlate,
        val entryTime: Instant,
    ) : WebhookEvent()

    data class Parked(
        val licensePlate: LicensePlate,
        val lat: Double,
        val lng: Double,
    ) : WebhookEvent()

    data class Exit(
        val licensePlate: LicensePlate,
        val exitTime: Instant,
    ) : WebhookEvent()
}
