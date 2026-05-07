package com.estapar.parking.parkingsession.domain

import java.math.BigDecimal
import java.time.Instant

sealed class SessionState {
    data class Entered(
        val entryTime: Instant,
    ) : SessionState()

    data class Parked(
        val entryTime: Instant,
        val spotId: Long,
        val sectorId: String,
        val pricingMultiplier: BigDecimal,
    ) : SessionState()

    data class Exited(
        val entryTime: Instant,
        val spotId: Long,
        val sectorId: String,
        val pricingMultiplier: BigDecimal,
        val exitTime: Instant,
        val totalCharged: BigDecimal,
    ) : SessionState()
}
