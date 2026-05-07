package com.estapar.parking.parkingsession.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal

interface ParkingSessionJpaRepository : JpaRepository<ParkingSessionJpaEntity, Long> {
    @Query("SELECT s FROM ParkingSessionJpaEntity s WHERE s.licensePlate = :plate AND s.state <> 'EXITED'")
    fun findActiveByLicensePlate(plate: String): ParkingSessionJpaEntity?

    fun findBySectorIdAndState(
        sectorId: String,
        state: String,
    ): List<ParkingSessionJpaEntity>

    fun findByStateAndExitTimeBetween(
        state: String,
        from: java.time.Instant,
        to: java.time.Instant,
    ): List<ParkingSessionJpaEntity>

    @Query("SELECT COALESCE(SUM(s.totalCharged), 0) FROM ParkingSessionJpaEntity s WHERE s.state = 'EXITED'")
    fun sumTotalCharged(): BigDecimal
}
