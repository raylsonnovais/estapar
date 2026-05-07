package com.estapar.parking.parkingsession.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ProcessedEventJpaRepository : JpaRepository<ProcessedEventJpaEntity, Long> {
    @Modifying
    @Query(
        value = """
            INSERT IGNORE INTO processed_events
                (license_plate, event_type, event_timestamp, processed_at)
            VALUES (:plate, :eventType, :timestamp, :now)
        """,
        nativeQuery = true,
    )
    fun insertIgnore(
        @Param("plate") plate: String,
        @Param("eventType") eventType: String,
        @Param("timestamp") timestamp: Instant?,
        @Param("now") now: Instant,
    ): Int
}
