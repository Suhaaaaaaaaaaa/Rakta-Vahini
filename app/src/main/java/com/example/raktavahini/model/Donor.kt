package com.example.raktavahini.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Donor(
    val id: Int,
    val name: String,
    val bloodGroup: String,
    val location: String,
    val distanceKm: Double,
    val phoneNumber: String,
    val lastDonationDate: LocalDate,
    val isReadyToDonate: Boolean,
    val hasDonationHistory: Boolean = true
) {
    fun isEligible(): Boolean {
        val daysSinceLastDonation = ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now())
        return daysSinceLastDonation >= 90 && isReadyToDonate
    }
}
