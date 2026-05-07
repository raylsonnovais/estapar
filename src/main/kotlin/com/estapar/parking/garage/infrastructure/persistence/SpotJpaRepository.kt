package com.estapar.parking.garage.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SpotJpaRepository : JpaRepository<SpotJpaEntity, Long> {
    fun existsByIsOccupiedFalse(): Boolean

    fun countBySectorIdAndIsOccupied(
        sectorId: String,
        isOccupied: Boolean,
    ): Long

    @Query(
        "SELECT s FROM SpotJpaEntity s " +
            "WHERE ABS(s.lat - :lat) < 1e-6 AND ABS(s.lng - :lng) < 1e-6",
    )
    fun findByLocation(
        lat: Double,
        lng: Double,
    ): SpotJpaEntity?

    fun findBySectorId(sectorId: String): List<SpotJpaEntity>
}
