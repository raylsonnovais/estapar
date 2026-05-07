package com.estapar.parking.garage.domain

import kotlin.math.abs

class SpotLocation(
    val lat: Double,
    val lng: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpotLocation) return false
        return abs(lat - other.lat) < EPSILON && abs(lng - other.lng) < EPSILON
    }

    // Rounds to 5 decimal places (~1 m precision) to keep equals/hashCode contract
    // consistent for values within the same epsilon neighbourhood.
    override fun hashCode(): Int {
        val latKey = (lat * 1e5).toLong()
        val lngKey = (lng * 1e5).toLong()
        return 31 * latKey.hashCode() + lngKey.hashCode()
    }

    override fun toString(): String = "SpotLocation(lat=$lat, lng=$lng)"

    companion object {
        const val EPSILON = 1e-6
    }
}
