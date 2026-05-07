package com.estapar.parking.parkingsession.application

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant

object BillingCalculator {
    private const val FREE_MINUTES = 30L
    private const val MINUTES_PER_HOUR = 60L
    private const val MONETARY_SCALE = 2

    fun calculate(
        basePrice: BigDecimal,
        multiplier: BigDecimal,
        entryTime: Instant,
        exitTime: Instant,
    ): BigDecimal {
        val minutes = Duration.between(entryTime, exitTime).toMinutes()
        if (minutes <= FREE_MINUTES) return BigDecimal.ZERO.setScale(MONETARY_SCALE)
        // ceiling division: (minutes + 59) / 60
        val hours = (minutes + MINUTES_PER_HOUR - 1L) / MINUTES_PER_HOUR
        return basePrice
            .multiply(multiplier)
            .multiply(BigDecimal(hours))
            .setScale(MONETARY_SCALE, RoundingMode.HALF_UP)
    }
}
