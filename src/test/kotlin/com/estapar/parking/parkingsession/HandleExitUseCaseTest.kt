package com.estapar.parking.parkingsession

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.garage.domain.Sector
import com.estapar.parking.parkingsession.application.HandleExitUseCase
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.application.port.ProcessedEventRepository
import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.ParkingSession
import com.estapar.parking.parkingsession.domain.SessionState
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.SessionNotFoundException
import com.estapar.parking.shared.metrics.ParkingMetrics
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class HandleExitUseCaseTest {
    private val garageRepository = mockk<GarageRepository>()
    private val sessionRepository = mockk<ParkingSessionRepository>()
    private val processedEventRepository = mockk<ProcessedEventRepository>()
    private val metrics = ParkingMetrics(SimpleMeterRegistry())
    private val useCase = HandleExitUseCase(garageRepository, sessionRepository, processedEventRepository, metrics)

    private val plate = LicensePlate("ZUL0001")

    // entry 12:00, exit 14:00 → 120 min → ceil(120/60)=2 hours × 10.00 × 1.00 = 20.00
    private val entryTime = Instant.parse("2025-01-01T12:00:00Z")
    private val exitTime = Instant.parse("2025-01-01T14:00:00Z")
    private val event = WebhookEvent.Exit(licensePlate = plate, exitTime = exitTime)

    private val sectorA = Sector(id = "A", basePrice = BigDecimal("10.00"), maxCapacity = 10)
    private val parkedSession =
        ParkingSession
            .create(plate, entryTime)
            .park(spotId = 1L, sectorId = "A", multiplier = BigDecimal("1.00"))

    @Test
    fun `transitions session to Exited, frees the spot, and charges correctly`() {
        every { processedEventRepository.tryRecord(plate, "EXIT", exitTime) } returns true
        every { sessionRepository.findActiveByLicensePlate(plate) } returns parkedSession
        every { garageRepository.findSectorById("A") } returns sectorA
        every { garageRepository.markSpotFree(1L) } returns Unit
        val saved = slot<ParkingSession>()
        every { sessionRepository.save(capture(saved)) } answers { saved.captured }

        useCase.execute(event)

        val session = saved.captured
        session.state.shouldBeInstanceOf<SessionState.Exited>()
        val exited = session.state as SessionState.Exited
        exited.exitTime shouldBe exitTime
        exited.spotId shouldBe 1L
        // 120 min → 2 hours × 10.00 × 1.00 = 20.00
        exited.totalCharged shouldBe BigDecimal("20.00")
        verify { garageRepository.markSpotFree(1L) }
    }

    @Test
    fun `throws SessionNotFoundException when no active session exists`() {
        every { processedEventRepository.tryRecord(plate, "EXIT", exitTime) } returns true
        every { sessionRepository.findActiveByLicensePlate(plate) } returns null

        shouldThrow<SessionNotFoundException> { useCase.execute(event) }
    }

    @Test
    fun `ignores duplicate EXIT event silently`() {
        every { processedEventRepository.tryRecord(plate, "EXIT", exitTime) } returns false

        useCase.execute(event)

        verify(exactly = 0) { sessionRepository.findActiveByLicensePlate(plate) }
    }
}
