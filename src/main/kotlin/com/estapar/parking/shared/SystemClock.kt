package com.estapar.parking.shared

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SystemClock : Clock {
    override fun now(): Instant = Instant.now()
}
