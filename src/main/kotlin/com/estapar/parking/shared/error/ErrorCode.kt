package com.estapar.parking.shared.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
) {
    GARAGE_FULL(HttpStatus.SERVICE_UNAVAILABLE),
    SECTOR_FULL(HttpStatus.CONFLICT),
    SPOT_NOT_FOUND(HttpStatus.NOT_FOUND),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT),
    INVALID_LICENSE_PLATE(HttpStatus.BAD_REQUEST),
    INVALID_WEBHOOK_EVENT(HttpStatus.BAD_REQUEST),
}
