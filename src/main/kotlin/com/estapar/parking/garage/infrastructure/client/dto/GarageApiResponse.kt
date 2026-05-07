package com.estapar.parking.garage.infrastructure.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GarageApiResponse(
    @JsonProperty("garage") val sectors: List<SectorDto>,
    @JsonProperty("spots") val spots: List<SpotDto>,
)

data class SectorDto(
    @JsonProperty("sector") val id: String,
    @JsonProperty("base_price") val basePrice: Double,
    @JsonProperty("max_capacity") val maxCapacity: Int,
    @JsonProperty("open_hour") val openHour: String?,
    @JsonProperty("close_hour") val closeHour: String?,
    @JsonProperty("duration_limit_minutes") val durationLimitMinutes: Int?,
)

data class SpotDto(
    @JsonProperty("id") val simulatorId: Long,
    @JsonProperty("sector") val sectorId: String,
    @JsonProperty("lat") val lat: Double,
    @JsonProperty("lng") val lng: Double,
    @JsonProperty("occupied") val occupied: Boolean,
)
