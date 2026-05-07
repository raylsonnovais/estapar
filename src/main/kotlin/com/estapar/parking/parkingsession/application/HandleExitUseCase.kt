package com.estapar.parking.parkingsession.application

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.application.port.ProcessedEventRepository
import com.estapar.parking.parkingsession.domain.SessionState
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.SessionNotFoundException
import com.estapar.parking.shared.metrics.ParkingMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HandleExitUseCase(
    private val garageRepository: GarageRepository,
    private val sessionRepository: ParkingSessionRepository,
    private val processedEventRepository: ProcessedEventRepository,
    private val metrics: ParkingMetrics,
) {
    private val logger = LoggerFactory.getLogger(HandleExitUseCase::class.java)

    @Transactional
    fun execute(event: WebhookEvent.Exit) {
        if (!processedEventRepository.tryRecord(event.licensePlate, "EXIT", event.exitTime)) {
            logger.info("Duplicate EXIT event for plate {}, ignoring", event.licensePlate)
            metrics.recordWebhookEvent("EXIT", "duplicate")
            return
        }

        val session =
            sessionRepository.findActiveByLicensePlate(event.licensePlate)
                ?: throw SessionNotFoundException(event.licensePlate.value)

        val parked =
            session.state as? SessionState.Parked
                ?: error("Session for plate ${event.licensePlate} is in state ${session.state::class.simpleName}, expected Parked")

        val sector =
            requireNotNull(garageRepository.findSectorById(parked.sectorId)) {
                "Sector ${parked.sectorId} not found for session ${session.id}"
            }

        val totalCharged =
            BillingCalculator.calculate(
                basePrice = sector.basePrice,
                multiplier = parked.pricingMultiplier,
                entryTime = parked.entryTime,
                exitTime = event.exitTime,
            )

        garageRepository.markSpotFree(parked.spotId)

        val exitedSession = session.exit(exitTime = event.exitTime, totalCharged = totalCharged)
        sessionRepository.save(exitedSession)

        metrics.recordWebhookEvent("EXIT", "recorded")
        logger.info(
            "EXIT recorded for plate {}, spot {} freed, charged {}",
            event.licensePlate,
            parked.spotId,
            totalCharged,
        )
    }
}
