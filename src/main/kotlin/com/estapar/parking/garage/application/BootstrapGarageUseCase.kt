package com.estapar.parking.garage.application

import com.estapar.parking.garage.application.port.GarageConfigClient
import com.estapar.parking.garage.application.port.GarageRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BootstrapGarageUseCase(
    private val configClient: GarageConfigClient,
    private val garageRepository: GarageRepository,
) {
    private val logger = LoggerFactory.getLogger(BootstrapGarageUseCase::class.java)

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun execute() {
        if (garageRepository.hasSectorsConfigured()) {
            logger.info("Garage already configured — skipping bootstrap")
            return
        }

        logger.info("Starting garage bootstrap from simulator")
        val garage = configClient.fetchConfig()

        garage.sectors.forEach { garageRepository.saveSector(it) }
        garage.spots.forEach { garageRepository.saveSpot(it) }

        logger.info(
            "Garage bootstrap complete: {} sectors, {} spots",
            garage.sectors.size,
            garage.spots.size,
        )
    }
}
