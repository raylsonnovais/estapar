package com.estapar.parking.garage.infrastructure

import com.estapar.parking.garage.infrastructure.client.GarageConfigRestClient
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.client.RestClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GarageConfigRestClientTest {
    private val wireMock = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

    @BeforeAll
    fun startWireMock() {
        wireMock.start()
        com.github.tomakehurst.wiremock.client.WireMock
            .configureFor(wireMock.port())
    }

    @AfterAll
    fun stopWireMock() {
        wireMock.stop()
    }

    @Test
    fun `fetchConfig parses simulator response correctly`() {
        stubFor(
            get("/garage").willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(GARAGE_RESPONSE),
            ),
        )

        val restClient =
            RestClient
                .builder()
                .baseUrl("http://localhost:${wireMock.port()}")
                .messageConverters { converters ->
                    converters.clear()
                    converters.add(
                        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(
                            ObjectMapper()
                                .registerKotlinModule()
                                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                        ),
                    )
                }.build()

        val client = GarageConfigRestClient(restClient)
        val garage = client.fetchConfig()

        garage.sectors.size shouldBe 2
        garage.spots.size shouldBe 4

        val sectorA = garage.sectors.first { it.id == "A" }
        sectorA.maxCapacity shouldBe 10
        sectorA.basePrice.toDouble() shouldBe 40.5

        val spotA = garage.spots.first { it.sectorId == "A" }
        spotA.location.lat shouldBe -23.561684
        spotA.location.lng shouldBe -46.655981
        spotA.isOccupied shouldBe false
    }

    companion object {
        private val GARAGE_RESPONSE =
            """
            {
              "garage": [
                { "sector": "A", "base_price": 40.5, "max_capacity": 10,
                  "open_hour": "00:00", "close_hour": "23:59", "duration_limit_minutes": 1440 },
                { "sector": "B", "base_price": 4.1, "max_capacity": 20,
                  "open_hour": "08:00", "close_hour": "23:59", "duration_limit_minutes": 60 }
              ],
              "spots": [
                { "id": 1, "sector": "A", "lat": -23.561684, "lng": -46.655981, "occupied": false },
                { "id": 2, "sector": "A", "lat": -23.561664, "lng": -46.655961, "occupied": false },
                { "id": 3, "sector": "B", "lat": -23.561484, "lng": -46.655781, "occupied": false },
                { "id": 4, "sector": "B", "lat": -23.561464, "lng": -46.655761, "occupied": false }
              ]
            }
            """.trimIndent()
    }
}
