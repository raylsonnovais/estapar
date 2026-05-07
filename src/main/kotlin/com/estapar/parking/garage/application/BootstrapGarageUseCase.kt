package com.estapar.parking.garage.application

import com.estapar.parking.garage.application.port.GarageConfigClient
import com.estapar.parking.garage.application.port.GarageRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.ResourceAccessException

@Service
class BootstrapGarageUseCase(
    private val configClient: GarageConfigClient,
    private val garageRepository: GarageRepository,
    @Value("\${garage.simulator.bootstrap-enabled:true}") private val bootstrapEnabled: Boolean,
) {
    private val logger = LoggerFactory.getLogger(BootstrapGarageUseCase::class.java)

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun execute() {
        if (!bootstrapEnabled) {
            logger.info("Garage bootstrap disabled (prod profile) — skipping simulator call")
            return
        }

        if (garageRepository.hasSectorsConfigured()) {
            logger.info("Garage already configured — skipping bootstrap")
            return
        }

        logger.info("Starting garage bootstrap from simulator")
        try {
            val garage = configClient.fetchConfig()
            garage.sectors.forEach { garageRepository.saveSector(it) }
            garage.spots.forEach { garageRepository.saveSpot(it) }
            logger.info(
                "Garage bootstrap complete: {} sectors, {} spots",
                garage.sectors.size,
                garage.spots.size,
            )
        } catch (
            @Suppress("SwallowedException") e: ResourceAccessException,
        ) {
            if (garageRepository.hasSectorsConfigured()) {
                logger.warn("Simulator unavailable, using existing garage data from database")
            } else {
                logger.error("Simulator unavailable and no garage data found — parking service degraded")
            }
        }
    }
}
