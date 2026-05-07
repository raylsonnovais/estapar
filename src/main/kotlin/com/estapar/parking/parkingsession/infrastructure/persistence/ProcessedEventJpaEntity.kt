package com.estapar.parking.parkingsession.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "processed_events",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_processed_event",
            columnNames = ["license_plate", "event_type", "event_timestamp"],
        ),
    ],
)
class ProcessedEventJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "license_plate", nullable = false, length = 8)
    val licensePlate: String,
    @Column(name = "event_type", nullable = false, length = 10)
    val eventType: String,
    @Column(name = "event_timestamp")
    val eventTimestamp: Instant? = null,
    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant,
)
