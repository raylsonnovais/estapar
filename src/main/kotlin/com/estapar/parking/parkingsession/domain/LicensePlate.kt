package com.estapar.parking.parkingsession.domain

import com.estapar.parking.shared.error.InvalidLicensePlateException

@JvmInline
value class LicensePlate(
    val value: String,
) {
    init {
        if (!PATTERN.matches(value)) throw InvalidLicensePlateException(value)
    }

    override fun toString(): String = value

    companion object {
        // Old Brazilian format: ABC1234 | Mercosul: ABC1D23
        private val PATTERN = Regex("[A-Z]{3}\\d{4}|[A-Z]{3}\\d[A-Z]\\d{2}")
    }
}
