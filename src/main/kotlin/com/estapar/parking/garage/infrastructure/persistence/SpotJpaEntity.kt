package com.estapar.parking.garage.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(
    name = "spots",
    indexes = [Index(name = "idx_spots_location", columnList = "lat, lng")],
)
class SpotJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "sector_id", nullable = false, length = 10)
    val sectorId: String,
    @Column(name = "lat", nullable = false)
    val lat: Double,
    @Column(name = "lng", nullable = false)
    val lng: Double,
    @Column(name = "is_occupied", nullable = false)
    var isOccupied: Boolean = false,
    @Version
    var version: Int = 0,
)
