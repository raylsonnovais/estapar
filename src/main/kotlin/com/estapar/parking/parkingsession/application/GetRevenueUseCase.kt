package com.estapar.parking.parkingsession.application

import com.estapar.parking.parkingsession.application.port.ParkingSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

data class RevenueResponse(
    val totalRevenue: BigDecimal,
    val currency: String = "BRL",
)

@Service
class GetRevenueUseCase(
    private val sessionRepository: ParkingSessionRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): RevenueResponse = RevenueResponse(totalRevenue = sessionRepository.totalRevenue())
}
