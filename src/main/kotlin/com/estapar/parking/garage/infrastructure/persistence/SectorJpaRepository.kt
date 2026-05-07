package com.estapar.parking.garage.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SectorJpaRepository : JpaRepository<SectorJpaEntity, String>
