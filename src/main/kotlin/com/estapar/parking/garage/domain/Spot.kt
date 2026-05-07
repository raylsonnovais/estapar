package com.estapar.parking.garage.domain

data class Spot(
    val id: Long = 0L,
    val sectorId: String,
    val location: SpotLocation,
    val isOccupied: Boolean = false,
) {
    fun occupy(): Spot = copy(isOccupied = true)

    fun free(): Spot = copy(isOccupied = false)
}
