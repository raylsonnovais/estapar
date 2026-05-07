package com.estapar.parking.garage.domain

class Garage(
    val sectors: List<Sector>,
    val spots: List<Spot>,
) {
    fun findSectorById(id: String): Sector? = sectors.find { it.id == id }

    fun isFull(): Boolean = spots.none { !it.isOccupied }
}
