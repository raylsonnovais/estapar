package com.estapar.parking.garage.domain

import java.math.BigDecimal

data class Sector(
    val id: String,
    val basePrice: BigDecimal,
    val maxCapacity: Int,
)
