package com.estapar.parking.shared.error

abstract class ApplicationException(
    val errorCode: ErrorCode,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class GarageFullException :
    ApplicationException(
        ErrorCode.GARAGE_FULL,
        "Garage is at full capacity — entry denied",
    )

class SectorFullException(
    sectorId: String,
) : ApplicationException(
        ErrorCode.SECTOR_FULL,
        "Sector '$sectorId' is at full capacity",
    )

class SpotNotFoundException(
    lat: Double,
    lng: Double,
) : ApplicationException(
        ErrorCode.SPOT_NOT_FOUND,
        "No spot found at coordinates lat=$lat, lng=$lng",
    )

class SessionNotFoundException(
    plate: String,
) : ApplicationException(
        ErrorCode.SESSION_NOT_FOUND,
        "No active session found for plate '$plate'",
    )

class InvalidLicensePlateException(
    value: String,
) : ApplicationException(
        ErrorCode.INVALID_LICENSE_PLATE,
        "Invalid license plate format: '$value'",
    )
