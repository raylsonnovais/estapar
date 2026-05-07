package com.estapar.parking.parkingsession.application.port

import com.estapar.parking.parkingsession.domain.LicensePlate
import java.time.Instant

interface ProcessedEventRepository {
    /** Returns true if this is a new event, false if it is a duplicate (ADR-001). */
    fun tryRecord(
        licensePlate: LicensePlate,
        eventType: String,
        eventTimestamp: Instant?,
    ): Boolean
}
