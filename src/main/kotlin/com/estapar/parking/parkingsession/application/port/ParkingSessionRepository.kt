package com.estapar.parking.parkingsession.application.port

import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.ParkingSession
import java.math.BigDecimal

interface ParkingSessionRepository {
    fun save(session: ParkingSession): ParkingSession

    fun findActiveByLicensePlate(licensePlate: LicensePlate): ParkingSession?

    fun totalRevenue(): BigDecimal
}
