package com.estapar.parking.garage.application.port

import com.estapar.parking.garage.domain.Garage

interface GarageConfigClient {
    fun fetchConfig(): Garage
}
