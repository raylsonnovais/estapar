package com.estapar.parking.garage.application.port

import com.estapar.parking.garage.domain.OccupancyRate
import com.estapar.parking.garage.domain.Sector
import com.estapar.parking.garage.domain.Spot

interface GarageRepository {
    fun saveSector(sector: Sector)

    fun saveSpot(spot: Spot)

    fun hasSectorsConfigured(): Boolean

    fun findSectorById(id: String): Sector?

    fun findSpotByLocation(
        lat: Double,
        lng: Double,
    ): Spot?

    fun hasAnyAvailableSpot(): Boolean

    fun occupancyRateOf(sectorId: String): OccupancyRate

    fun markSpotOccupied(spotId: Long)

    fun markSpotFree(spotId: Long)
}
