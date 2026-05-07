package com.estapar.parking.parkingsession

import com.estapar.parking.garage.application.port.GarageRepository
import com.estapar.parking.garage.domain.OccupancyRate
import com.estapar.parking.garage.domain.Spot
import com.estapar.parking.garage.domain.SpotLocation
import com.estapar.parking.parkingsession.application.HandleParkedUseCase
import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.parkingsession.domain.ParkingSession
import com.estapar.parking.parkingsession.domain.SessionState
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.shared.error.SectorFullException
import com.estapar.parking.shared.error.SessionNotFoundException
import com.estapar.parking.shared.error.SpotNotFoundException
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

class HandleParkedUseCaseTest {
    private val garageRepository = mockk<GarageRepository>()
    private val sessionRepository = mockk<ParkingSessionRepository>()
    private val metrics = ParkingMetrics(SimpleMeterRegistry())
    private val useCase = HandleParkedUseCase(garageRepository, sessionRepository, metrics)

    private val plate = LicensePlate("ZUL0001")
    private val lat = -23.561684
    private val lng = -46.655981
    private val event = WebhookEvent.Parked(licensePlate = plate, lat = lat, lng = lng)

    private val spot = Spot(id = 1L, sectorId = "A", location = SpotLocation(lat, lng))
    private val enteredSession = ParkingSession.create(plate, Instant.parse("2025-01-01T12:00:00Z"))

    @Test
    fun `transitions session to Parked and marks spot occupied`() {
        every { garageRepository.findSpotByLocation(lat, lng) } returns spot
        every { garageRepository.occupancyRateOf("A") } returns OccupancyRate(occupied = 2, capacity = 10)
        every { sessionRepository.findActiveByLicensePlate(plate) } returns enteredSession
        every { garageRepository.markSpotOccupied(1L) } returns Unit
        val saved = slot<ParkingSession>()
        every { sessionRepository.save(capture(saved)) } answers { saved.captured }

        useCase.execute(event)

        val session = saved.captured
        session.state.shouldBeInstanceOf<SessionState.Parked>()
        val parked = session.state as SessionState.Parked
        parked.spotId shouldBe 1L
        parked.sectorId shouldBe "A"
        parked.pricingMultiplier shouldBe BigDecimal("0.90") // 20% < 25%
        verify { garageRepository.markSpotOccupied(1L) }
    }

    @Test
    fun `throws SpotNotFoundException when coordinates match nothing`() {
        every { garageRepository.findSpotByLocation(lat, lng) } returns null

        shouldThrow<SpotNotFoundException> { useCase.execute(event) }
    }

    @Test
    fun `throws SectorFullException when sector is at 100% capacity`() {
        every { garageRepository.findSpotByLocation(lat, lng) } returns spot
        every { garageRepository.occupancyRateOf("A") } returns OccupancyRate(occupied = 10, capacity = 10)

        shouldThrow<SectorFullException> { useCase.execute(event) }
    }

    @Test
    fun `throws SessionNotFoundException when no active session exists`() {
        every { garageRepository.findSpotByLocation(lat, lng) } returns spot
        every { garageRepository.occupancyRateOf("A") } returns OccupancyRate(occupied = 2, capacity = 10)
        every { sessionRepository.findActiveByLicensePlate(plate) } returns null

        shouldThrow<SessionNotFoundException> { useCase.execute(event) }
    }

    @Test
    fun `ignores duplicate PARKED event when session is already in Parked state`() {
        val parkedSession = enteredSession.park(spotId = 1L, sectorId = "A", multiplier = BigDecimal("1.00"))
        every { garageRepository.findSpotByLocation(lat, lng) } returns spot
        every { garageRepository.occupancyRateOf("A") } returns OccupancyRate(occupied = 5, capacity = 10)
        every { sessionRepository.findActiveByLicensePlate(plate) } returns parkedSession

        useCase.execute(event)

        verify(exactly = 0) { garageRepository.markSpotOccupied(any()) }
        verify(exactly = 0) { sessionRepository.save(any()) }
    }
}
