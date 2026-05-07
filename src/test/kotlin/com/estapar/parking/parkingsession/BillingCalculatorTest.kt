package com.estapar.parking.parkingsession

import com.estapar.parking.parkingsession.application.BillingCalculator
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class BillingCalculatorTest {
    private val base = Instant.parse("2025-01-01T10:00:00Z")
    private val basePrice = BigDecimal("10.00")

    @ParameterizedTest(name = "{0} min × multiplier {1} → {2}")
    @CsvSource(
        // minutes, multiplier, expectedCharge
        "0,   0.90, 0.00",
        "29,  1.00, 0.00",
        "30,  1.10, 0.00",
        "31,  1.25, 12.50", // ceil(31/60)=1 × 10 × 1.25
        "59,  0.90, 9.00", // ceil(59/60)=1 × 10 × 0.90
        "60,  1.00, 10.00", // ceil(60/60)=1 × 10 × 1.00
        "61,  1.10, 22.00", // ceil(61/60)=2 × 10 × 1.10
        "119, 1.25, 25.00", // ceil(119/60)=2 × 10 × 1.25
        "120, 0.90, 18.00", // ceil(120/60)=2 × 10 × 0.90
        "121, 1.00, 30.00", // ceil(121/60)=3 × 10 × 1.00
    )
    fun `billing table`(
        minutes: Long,
        multiplier: String,
        expected: String,
    ) {
        val exitTime = base.plus(minutes, ChronoUnit.MINUTES)
        val result =
            BillingCalculator.calculate(
                basePrice = basePrice,
                multiplier = BigDecimal(multiplier),
                entryTime = base,
                exitTime = exitTime,
            )
        result shouldBe BigDecimal(expected)
    }
}
