package com.estapar.parking.parkingsession.infrastructure.webhook

import com.estapar.parking.parkingsession.application.HandleEntryUseCase
import com.estapar.parking.parkingsession.application.HandleExitUseCase
import com.estapar.parking.parkingsession.application.HandleParkedUseCase
import com.estapar.parking.parkingsession.domain.event.WebhookEvent
import com.estapar.parking.parkingsession.infrastructure.webhook.dto.WebhookRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val handleEntryUseCase: HandleEntryUseCase,
    private val handleParkedUseCase: HandleParkedUseCase,
    private val handleExitUseCase: HandleExitUseCase,
) {
    @PostMapping
    fun handleWebhook(
        @RequestBody request: WebhookRequest,
    ): ResponseEntity<Void> {
        when (val event = WebhookEventMapper.toEvent(request)) {
            is WebhookEvent.Entry -> handleEntryUseCase.execute(event)
            is WebhookEvent.Parked -> handleParkedUseCase.execute(event)
            is WebhookEvent.Exit -> handleExitUseCase.execute(event)
        }
        return ResponseEntity.ok().build()
    }
}
