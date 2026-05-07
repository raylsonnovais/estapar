package com.estapar.parking.parkingsession.application

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.domain.SessionState
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.SectorFullException
import com.estapar.parking.shared.error.SessionNotFoundException
import com.estapar.parking.shared.error.SpotNotFoundException
import com.estapar.parking.shared.metrics.ParkingMetrics
import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HandleParkedUseCase(
    private val garageRepository: GarageRepository,
    private val sessionRepository: ParkingSessionRepository,
    private val metrics: ParkingMetrics,
) {
    private val logger = LoggerFactory.getLogger(HandleParkedUseCase::class.java)

    @Transactional
    @Retryable(
        retryFor = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 50L),
    )
    fun execute(event: WebhookEvent.Parked) {
        val spot =
            garageRepository.findSpotByLocation(event.lat, event.lng)
                ?: throw SpotNotFoundException(event.lat, event.lng)

        val occupancyRate = garageRepository.occupancyRateOf(spot.sectorId)
        if (occupancyRate.isFull()) throw SectorFullException(spot.sectorId)

        val session =
            sessionRepository.findActiveByLicensePlate(event.licensePlate)
                ?: throw SessionNotFoundException(event.licensePlate.value)

        if (session.state is SessionState.Parked) {
            logger.info("Duplicate PARKED event for plate {}, ignoring", event.licensePlate)
            metrics.recordWebhookEvent("PARKED", "duplicate")
            return
        }

        // ADR-002: freeze the multiplier at the moment the car parks in the sector
        val multiplier = occupancyRate.pricingMultiplier()

        garageRepository.markSpotOccupied(spot.id)

        val parkedSession = session.park(spotId = spot.id, sectorId = spot.sectorId, multiplier = multiplier)
        sessionRepository.save(parkedSession)

        metrics.recordWebhookEvent("PARKED", "recorded")
        logger.info(
            "PARKED plate {} at spot {} in sector {}, multiplier={}",
            event.licensePlate,
            spot.id,
            spot.sectorId,
            multiplier,
        )
    }
}
