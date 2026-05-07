package com.estapar.parking.parkingsession.domain

import java.math.BigDecimal
import java.time.Instant

class ParkingSession private constructor(
    val id: Long,
    val licensePlate: LicensePlate,
    val state: SessionState,
) {
    fun park(
        spotId: Long,
        sectorId: String,
        multiplier: BigDecimal,
    ): ParkingSession {
        val entered =
            state as? SessionState.Entered
                ?: error("Cannot park session in state ${state::class.simpleName} for plate $licensePlate")
        return ParkingSession(
            id = id,
            licensePlate = licensePlate,
            state =
                SessionState.Parked(
                    entryTime = entered.entryTime,
                    spotId = spotId,
                    sectorId = sectorId,
                    pricingMultiplier = multiplier,
                ),
        )
    }

    fun exit(
        exitTime: Instant,
        totalCharged: BigDecimal,
    ): ParkingSession {
        val parked =
            state as? SessionState.Parked
                ?: error("Cannot exit session in state ${state::class.simpleName} for plate $licensePlate")
        return ParkingSession(
            id = id,
            licensePlate = licensePlate,
            state =
                SessionState.Exited(
                    entryTime = parked.entryTime,
                    spotId = parked.spotId,
                    sectorId = parked.sectorId,
                    pricingMultiplier = parked.pricingMultiplier,
                    exitTime = exitTime,
                    totalCharged = totalCharged,
                ),
        )
    }

    companion object {
        fun create(
            licensePlate: LicensePlate,
            entryTime: Instant,
        ): ParkingSession =
            ParkingSession(
                id = 0L,
                licensePlate = licensePlate,
                state = SessionState.Entered(entryTime = entryTime),
            )

        fun reconstitute(
            id: Long,
            licensePlate: LicensePlate,
            state: SessionState,
        ): ParkingSession = ParkingSession(id = id, licensePlate = licensePlate, state = state)
    }
}
