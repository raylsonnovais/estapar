package com.estapar.parking.parkingsession.infrastructure.persistence

import com.estapar.parking.parkingsession.application.port.ProcessedEventRepository
import com.estapar.parking.parkingsession.domain.LicensePlate
import com.estapar.parking.shared.Clock
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class ProcessedEventRepositoryAdapter(
    private val jpaRepository: ProcessedEventJpaRepository,
    private val clock: Clock,
) : ProcessedEventRepository {
    // INSERT IGNORE returns 1 when inserted (new event) and 0 on duplicate key (already processed).
    // Running in the same outer transaction keeps idempotency atomic with the business operation.
    @Transactional
    override fun tryRecord(
        licensePlate: LicensePlate,
        eventType: String,
        eventTimestamp: Instant?,
    ): Boolean =
        jpaRepository.insertIgnore(
            plate = licensePlate.value,
            eventType = eventType,
            timestamp = eventTimestamp,
            now = clock.now(),
        ) > 0
}
