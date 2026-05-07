package com.estapar.parking.garage.infrastructure.client

import com.estapar.parking.garage.application.port.GarageConfigClient
import com.estapar.parking.garage.domain.Garage
import com.estapar.parking.garage.domain.Sector
import com.estapar.parking.garage.domain.Spot
import com.estapar.parking.garage.domain.SpotLocation
import com.estapar.parking.garage.infrastructure.client.dto.GarageApiResponse
import com.estapar.parking.garage.infrastructure.client.dto.SectorDto
import com.estapar.parking.garage.infrastructure.client.dto.SpotDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class GarageConfigRestClient(
    private val garageSimulatorRestClient: RestClient,
) : GarageConfigClient {
    private val logger = LoggerFactory.getLogger(GarageConfigRestClient::class.java)

    override fun fetchConfig(): Garage {
        logger.info("Fetching garage configuration from simulator")

        val response =
            garageSimulatorRestClient
                .get()
                .uri("/garage")
                .retrieve()
                .body(GarageApiResponse::class.java)
                ?: error("Simulator returned null response for GET /garage")

        val sectors = response.sectors.map { it.toDomain() }
        val spots = response.spots.map { it.toDomain() }

        logger.info("Fetched {} sectors and {} spots from simulator", sectors.size, spots.size)

        return Garage(sectors = sectors, spots = spots)
    }
}

private fun SectorDto.toDomain() =
    Sector(
        id = id,
        basePrice = BigDecimal.valueOf(basePrice),
        maxCapacity = maxCapacity,
    )

private fun SpotDto.toDomain() =
    Spot(
        sectorId = sectorId,
        location = SpotLocation(lat = lat, lng = lng),
        isOccupied = occupied,
    )
