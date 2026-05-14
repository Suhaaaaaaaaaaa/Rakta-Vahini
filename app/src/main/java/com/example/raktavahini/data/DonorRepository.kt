package com.example.raktavahini.data

import com.example.raktavahini.model.Donor
import java.time.LocalDate

object DonorRepository {

    val donors = listOf(
        Donor(
            id = 1,
            name = "Aarav Sharma",
            bloodGroup = "A+",
            location = "Village Health Center",
            distanceKm = 4.5,
            phoneNumber = "9876543210",
            lastDonationDate = LocalDate.now().minusDays(120),
            isReadyToDonate = true
        ),
        Donor(
            id = 2,
            name = "Meera Patel",
            bloodGroup = "O-",
            location = "Rural Hospital",
            distanceKm = 8.2,
            phoneNumber = "9876501234",
            lastDonationDate = LocalDate.now().minusDays(150),
            isReadyToDonate = true
        ),
        Donor(
            id = 3,
            name = "Kabir Khan",
            bloodGroup = "B+",
            location = "Community Clinic",
            distanceKm = 12.0,
            phoneNumber = "9123456780",
            lastDonationDate = LocalDate.now().minusDays(45),
            isReadyToDonate = true
        ),
        Donor(
            id = 4,
            name = "Ananya Rao",
            bloodGroup = "AB+",
            location = "District Blood Bank",
            distanceKm = 6.8,
            phoneNumber = "9988776655",
            lastDonationDate = LocalDate.now().minusDays(200),
            isReadyToDonate = false
        ),
        Donor(
            id = 5,
            name = "Rohan Verma",
            bloodGroup = "O+",
            location = "Primary Care Center",
            distanceKm = 3.1,
            phoneNumber = "9090909090",
            lastDonationDate = LocalDate.now().minusDays(95),
            isReadyToDonate = true
        )
    )

    fun searchEligibleDonors(
        bloodGroup: String,
        radiusKm: Double
    ): List<Donor> {
        return donors.filter { donor ->
            donor.bloodGroup == bloodGroup &&
                    donor.distanceKm <= radiusKm &&
                    donor.isEligible()
        }
    }
}
