package com.estapar.parking.garage.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class OccupancyRateTest {
    @ParameterizedTest(name = "{0}/{1} => multiplier {2}")
    @CsvSource(
        "0,  10, 0.90", // 0%   → < 25%
        "2,  10, 0.90", // 20%  → < 25%
        "2,   8, 1.00", // 25%  boundary (2/8 = 25%) → ratio < 0.25 is false → 1.00
        "3,  10, 1.00", // 30%  → [25%, 50%)
        "4,  10, 1.00", // 40%  → [25%, 50%)
        "5,  10, 1.10", // 50%  → [50%, 75%)
        "7,  10, 1.10", // 70%  → [50%, 75%)
        "8,  10, 1.25", // 80%  → [75%, 100%]
        "10, 10, 1.25", // 100% → [75%, 100%]
    )
    fun `pricingMultiplier returns correct value for occupancy`(
        occupied: Int,
        capacity: Int,
        expectedMultiplier: String,
    ) {
        val rate = OccupancyRate(occupied = occupied, capacity = capacity)
        rate.pricingMultiplier() shouldBe BigDecimal(expectedMultiplier)
    }

    @Test
    fun `isFull returns true when occupied equals capacity`() {
        OccupancyRate(occupied = 10, capacity = 10).isFull() shouldBe true
    }

    @Test
    fun `isFull returns false when there are free spots`() {
        OccupancyRate(occupied = 9, capacity = 10).isFull() shouldBe false
    }

    @Test
    fun `throws when capacity is zero`() {
        shouldThrow<IllegalArgumentException> {
            OccupancyRate(occupied = 0, capacity = 0)
        }
    }

    @Test
    fun `throws when occupied exceeds capacity`() {
        shouldThrow<IllegalArgumentException> {
            OccupancyRate(occupied = 11, capacity = 10)
        }
    }

    @Test
    fun `throws when occupied is negative`() {
        shouldThrow<IllegalArgumentException> {
            OccupancyRate(occupied = -1, capacity = 10)
        }
    }

    @Test
    fun `ratio is calculated correctly`() {
        OccupancyRate(occupied = 5, capacity = 10).ratio shouldBe 0.5
    }
}
