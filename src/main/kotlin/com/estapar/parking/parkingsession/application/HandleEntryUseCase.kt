package com.estapar.parking.parkingsession.application

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.application.port.ProcessedEventRepository
import com.estapar.parking.parkingsession.domain.ParkingSession
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.GarageFullException
import com.estapar.parking.shared.metrics.ParkingMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HandleEntryUseCase(
    private val garageRepository: GarageRepository,
    private val sessionRepository: ParkingSessionRepository,
    private val processedEventRepository: ProcessedEventRepository,
    private val metrics: ParkingMetrics,
) {
    private val logger = LoggerFactory.getLogger(HandleEntryUseCase::class.java)

    @Transactional
    fun execute(event: WebhookEvent.Entry) {
        if (!processedEventRepository.tryRecord(event.licensePlate, "ENTRY", event.entryTime)) {
            logger.info("Duplicate ENTRY event for plate {}, ignoring", event.licensePlate)
            metrics.recordWebhookEvent("ENTRY", "duplicate")
            return
        }

        if (!garageRepository.hasAnyAvailableSpot()) {
            throw GarageFullException()
        }

        val session = ParkingSession.create(licensePlate = event.licensePlate, entryTime = event.entryTime)
        sessionRepository.save(session)

        metrics.recordWebhookEvent("ENTRY", "recorded")
        logger.info("ENTRY recorded for plate {}", event.licensePlate)
    }
}
