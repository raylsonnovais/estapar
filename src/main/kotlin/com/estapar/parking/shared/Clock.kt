package com.estapar.parking.shared

import java.time.Instant

interface Clock {
    fun now(): Instant
}
