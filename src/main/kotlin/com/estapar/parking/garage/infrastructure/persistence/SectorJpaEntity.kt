package com.estapar.parking.garage.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "sectors")
class SectorJpaEntity(
    @Id
    @Column(name = "id", length = 10)
    val id: String,
    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    val basePrice: BigDecimal,
    @Column(name = "max_capacity", nullable = false)
    val maxCapacity: Int,
)
