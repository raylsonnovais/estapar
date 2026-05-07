package com.estapar.parking.parkingsession.integration

import com.estapar.parking.garage.application.port.GarageConfigClient
import com.estapar.parking.garage.domain.Garage
import com.estapar.parking.garage.domain.Sector
import com.estapar.parking.garage.domain.Spot
import com.estapar.parking.garage.domain.SpotLocation
import com.estapar.parking.parkingsession.infrastructure.persistence.ParkingSessionJpaRepository
import com.estapar.parking.parkingsession.infrastructure.persistence.ProcessedEventJpaRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(WebhookIntegrationTest.StubConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebhookIntegrationTest {
    @TestConfiguration
    class StubConfig {
        @Bean
        @Primary
        fun garageConfigClient(): GarageConfigClient =
            mockk<GarageConfigClient>().also {
                every { it.fetchConfig() } returns
                    Garage(
                        sectors = listOf(Sector(id = "A", basePrice = BigDecimal("10.00"), maxCapacity = 2)),
                        spots =
                            listOf(
                                Spot(sectorId = "A", location = SpotLocation(-23.561684, -46.655981)),
                                Spot(sectorId = "A", location = SpotLocation(-23.561664, -46.655961)),
                            ),
                    )
            }
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var sessionRepository: ParkingSessionJpaRepository

    @Autowired
    private lateinit var processedEventRepository: ProcessedEventJpaRepository

    @BeforeEach
    fun cleanSessions() {
        sessionRepository.deleteAll()
        processedEventRepository.deleteAll()
    }

    @Test
    fun `full cycle ENTRY PARKED EXIT succeeds and transitions state correctly`() {
        // ENTRY
        val entryResponse =
            restTemplate.postForEntity(
                "/webhook",
                mapOf(
                    "license_plate" to "ZUL0001",
                    "event_type" to "ENTRY",
                    "entry_time" to "2025-01-01T12:00:00.000Z",
                ),
                Void::class.java,
            )
        entryResponse.statusCode shouldBe HttpStatus.OK

        val afterEntry = sessionRepository.findActiveByLicensePlate("ZUL0001")
        requireNotNull(afterEntry)
        afterEntry.state shouldBe "ENTERED"

        // PARKED
        val parkedResponse =
            restTemplate.postForEntity(
                "/webhook",
                mapOf(
                    "license_plate" to "ZUL0001",
                    "event_type" to "PARKED",
                    "lat" to -23.561684,
                    "lng" to -46.655981,
                ),
                Void::class.java,
            )
        parkedResponse.statusCode shouldBe HttpStatus.OK

        val afterParked = sessionRepository.findActiveByLicensePlate("ZUL0001")
        requireNotNull(afterParked)
        afterParked.state shouldBe "PARKED"
        afterParked.sectorId shouldBe "A"

        // EXIT
        val exitResponse =
            restTemplate.postForEntity(
                "/webhook",
                mapOf(
                    "license_plate" to "ZUL0001",
                    "event_type" to "EXIT",
                    "exit_time" to "2025-01-01T14:00:00.000Z",
                ),
                Void::class.java,
            )
        exitResponse.statusCode shouldBe HttpStatus.OK

        val afterExit = sessionRepository.findActiveByLicensePlate("ZUL0001")
        afterExit shouldBe null // session is EXITED — not returned by findActive
    }

    @Test
    fun `duplicate ENTRY event returns 200 without creating second session`() {
        val payload =
            mapOf(
                "license_plate" to "ZUL0002",
                "event_type" to "ENTRY",
                "entry_time" to "2025-01-01T10:00:00.000Z",
            )

        restTemplate.postForEntity("/webhook", payload, Void::class.java)
        val second = restTemplate.postForEntity("/webhook", payload, Void::class.java)

        second.statusCode shouldBe HttpStatus.OK
        sessionRepository.findAll().count { it.licensePlate == "ZUL0002" } shouldBe 1
    }
}
