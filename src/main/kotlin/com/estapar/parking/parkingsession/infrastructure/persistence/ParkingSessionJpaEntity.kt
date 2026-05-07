package com.estapar.parking.parkingsession.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "parking_sessions",
    indexes = [Index(name = "idx_sessions_plate_state", columnList = "license_plate, state")],
)
class ParkingSessionJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "license_plate", nullable = false, length = 8)
    val licensePlate: String,
    @Column(name = "state", nullable = false, length = 10)
    var state: String,
    @Column(name = "entry_time", nullable = false)
    val entryTime: Instant,
    @Column(name = "spot_id")
    var spotId: Long? = null,
    @Column(name = "sector_id", length = 10)
    var sectorId: String? = null,
    @Column(name = "pricing_multiplier", precision = 4, scale = 2)
    var pricingMultiplier: BigDecimal? = null,
    @Column(name = "exit_time")
    var exitTime: Instant? = null,
    @Column(name = "total_charged", precision = 10, scale = 2)
    var totalCharged: BigDecimal? = null,
)
