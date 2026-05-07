package com.estapar.parking.parkingsession.infrastructure.persistence

import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.ParkingSession
import com.estapar.parking.parkingsession.domain.SessionState
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ParkingSessionRepositoryAdapter(
    private val jpaRepository: ParkingSessionJpaRepository,
) : ParkingSessionRepository {
    override fun save(session: ParkingSession): ParkingSession {
        val saved = jpaRepository.save(session.toEntity())
        return saved.toDomain()
    }

    override fun findActiveByLicensePlate(licensePlate: LicensePlate): ParkingSession? =
        jpaRepository.findActiveByLicensePlate(licensePlate.value)?.toDomain()

    override fun totalRevenue(): BigDecimal = jpaRepository.sumTotalCharged()
}

private fun ParkingSession.toEntity(): ParkingSessionJpaEntity =
    when (val s = state) {
        is SessionState.Entered ->
            ParkingSessionJpaEntity(
                id = id,
                licensePlate = licensePlate.value,
                state = "ENTERED",
                entryTime = s.entryTime,
            )

        is SessionState.Parked ->
            ParkingSessionJpaEntity(
                id = id,
                licensePlate = licensePlate.value,
                state = "PARKED",
                entryTime = s.entryTime,
                spotId = s.spotId,
                sectorId = s.sectorId,
                pricingMultiplier = s.pricingMultiplier,
            )

        is SessionState.Exited ->
            ParkingSessionJpaEntity(
                id = id,
                licensePlate = licensePlate.value,
                state = "EXITED",
                entryTime = s.entryTime,
                spotId = s.spotId,
                sectorId = s.sectorId,
                pricingMultiplier = s.pricingMultiplier,
                exitTime = s.exitTime,
                totalCharged = s.totalCharged,
            )
    }

fun ParkingSessionJpaEntity.toDomain(): ParkingSession {
    val sessionState =
        when (state) {
            "ENTERED" -> SessionState.Entered(entryTime = entryTime)
            "PARKED" ->
                SessionState.Parked(
                    entryTime = entryTime,
                    spotId = requireNotNull(spotId) { "spotId missing for PARKED session $id" },
                    sectorId = requireNotNull(sectorId) { "sectorId missing for PARKED session $id" },
                    pricingMultiplier = requireNotNull(pricingMultiplier) { "pricingMultiplier missing for PARKED session $id" },
                )

            "EXITED" ->
                SessionState.Exited(
                    entryTime = entryTime,
                    spotId = requireNotNull(spotId) { "spotId missing for EXITED session $id" },
                    sectorId = requireNotNull(sectorId) { "sectorId missing for EXITED session $id" },
                    pricingMultiplier = requireNotNull(pricingMultiplier) { "pricingMultiplier missing for EXITED session $id" },
                    exitTime = requireNotNull(exitTime) { "exitTime missing for EXITED session $id" },
                    totalCharged = requireNotNull(totalCharged) { "totalCharged missing for EXITED session $id" },
                )

            else -> error("Unknown session state '$state' for session $id")
        }
    return ParkingSession.reconstitute(id = id, licensePlate = LicensePlate(licensePlate), state = sessionState)
}
