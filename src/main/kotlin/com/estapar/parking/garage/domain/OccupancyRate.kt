package com.estapar.parking.garage.domain

import java.math.BigDecimal

data class OccupancyRate(
    val occupied: Int,
    val capacity: Int,
) {
    init {
        require(capacity > 0) { "Capacity must be positive, was $capacity" }
        require(occupied >= 0) { "Occupied count must be non-negative, was $occupied" }
        require(occupied <= capacity) { "Occupied ($occupied) cannot exceed capacity ($capacity)" }
    }

    val ratio: Double
        get() = occupied.toDouble() / capacity.toDouble()

    fun isFull(): Boolean = occupied >= capacity

    fun pricingMultiplier(): BigDecimal =
        when {
            ratio < LOW_THRESHOLD -> BigDecimal("0.90")
            ratio < MID_THRESHOLD -> BigDecimal("1.00")
            ratio < HIGH_THRESHOLD -> BigDecimal("1.10")
            else -> BigDecimal("1.25")
        }

    companion object {
        private const val LOW_THRESHOLD = 0.25
        private const val MID_THRESHOLD = 0.50
        private const val HIGH_THRESHOLD = 0.75
    }
}
