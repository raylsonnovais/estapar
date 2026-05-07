package com.estapar.parking.parkingsession.infrastructure.webhook

import com.estapar.parking.parkingsession.application.GetRevenueUseCase
import com.estapar.parking.parkingsession.application.RevenueResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RevenueController(
    private val getRevenueUseCase: GetRevenueUseCase,
) {
    @GetMapping("/revenue")
    fun getRevenue(): RevenueResponse = getRevenueUseCase.execute()
}
