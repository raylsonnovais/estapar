package com.estapar.parking.parkingsession

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.parkingsession.application.HandleEntryUseCase
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.application.port.ProcessedEventRepository
import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.ParkingSession
import com.estapar.parking.parkingsession.domain.SessionState
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.GarageFullException
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
import java.time.Instant

class HandleEntryUseCaseTest {
    private val garageRepository = mockk<GarageRepository>()
    private val sessionRepository = mockk<ParkingSessionRepository>()
    private val processedEventRepository = mockk<ProcessedEventRepository>()
    private val metrics = ParkingMetrics(SimpleMeterRegistry())
    private val useCase = HandleEntryUseCase(garageRepository, sessionRepository, processedEventRepository, metrics)

    private val plate = LicensePlate("ZUL0001")
    private val entryTime = Instant.parse("2025-01-01T12:00:00Z")
    private val event = WebhookEvent.Entry(licensePlate = plate, entryTime = entryTime)

    @Test
    fun `creates session when garage has available spots`() {
        every { processedEventRepository.tryRecord(plate, "ENTRY", entryTime) } returns true
        every { garageRepository.hasAnyAvailableSpot() } returns true
        val saved = slot<ParkingSession>()
        every { sessionRepository.save(capture(saved)) } answers { saved.captured }

        useCase.execute(event)

        val session = saved.captured
        session.licensePlate shouldBe plate
        session.state.shouldBeInstanceOf<SessionState.Entered>()
        (session.state as SessionState.Entered).entryTime shouldBe entryTime
    }

    @Test
    fun `throws GarageFullException when garage is 100% occupied`() {
        every { processedEventRepository.tryRecord(plate, "ENTRY", entryTime) } returns true
        every { garageRepository.hasAnyAvailableSpot() } returns false

        shouldThrow<GarageFullException> { useCase.execute(event) }
    }

    @Test
    fun `ignores duplicate entry event silently`() {
        every { processedEventRepository.tryRecord(plate, "ENTRY", entryTime) } returns false

        useCase.execute(event)

        verify(exactly = 0) { garageRepository.hasAnyAvailableSpot() }
        verify(exactly = 0) { sessionRepository.save(any()) }
    }
}
