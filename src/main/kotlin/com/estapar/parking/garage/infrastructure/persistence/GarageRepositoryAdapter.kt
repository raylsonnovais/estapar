package com.estapar.parking.garage.infrastructure.persistence

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.garage.domain.OccupancyRate
import com.estapar.parking.garage.domain.Sector
import com.estapar.parking.garage.domain.Spot
import com.estapar.parking.garage.domain.SpotLocation
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class GarageRepositoryAdapter(
    private val sectorJpaRepository: SectorJpaRepository,
    private val spotJpaRepository: SpotJpaRepository,
) : GarageRepository {
    override fun saveSector(sector: Sector) {
        sectorJpaRepository.save(sector.toEntity())
    }

    override fun saveSpot(spot: Spot) {
        spotJpaRepository.save(spot.toEntity())
    }

    override fun hasSectorsConfigured(): Boolean = sectorJpaRepository.count() > 0

    override fun findSectorById(id: String): Sector? = sectorJpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findSpotByLocation(
        lat: Double,
        lng: Double,
    ): Spot? = spotJpaRepository.findByLocation(lat, lng)?.toDomain()

    override fun hasAnyAvailableSpot(): Boolean = spotJpaRepository.existsByIsOccupiedFalse()

    override fun occupancyRateOf(sectorId: String): OccupancyRate {
        val sector =
            requireNotNull(sectorJpaRepository.findById(sectorId).orElse(null)) {
                "Sector '$sectorId' not found"
            }
        val occupied = spotJpaRepository.countBySectorIdAndIsOccupied(sectorId, true).toInt()
        return OccupancyRate(occupied = occupied, capacity = sector.maxCapacity)
    }

    override fun markSpotOccupied(spotId: Long) = updateSpotOccupancy(spotId, occupied = true)

    override fun markSpotFree(spotId: Long) = updateSpotOccupancy(spotId, occupied = false)

    private fun updateSpotOccupancy(
        spotId: Long,
        occupied: Boolean,
    ) {
        val entity =
            requireNotNull(spotJpaRepository.findById(spotId).orElse(null)) {
                "Spot '$spotId' not found"
            }
        entity.isOccupied = occupied
        spotJpaRepository.save(entity)
    }
}

private fun Sector.toEntity() = SectorJpaEntity(id = id, basePrice = basePrice, maxCapacity = maxCapacity)

private fun SectorJpaEntity.toDomain() = Sector(id = id, basePrice = basePrice, maxCapacity = maxCapacity)

private fun Spot.toEntity() =
    SpotJpaEntity(
        sectorId = sectorId,
        lat = location.lat,
        lng = location.lng,
        isOccupied = isOccupied,
    )

fun SpotJpaEntity.toDomain() =
    Spot(
        id = id,
        sectorId = sectorId,
        location = SpotLocation(lat, lng),
        isOccupied = isOccupied,
    )

fun SectorJpaEntity.toDomainWithBasePrice(): Pair<String, BigDecimal> = Pair(id, basePrice)
