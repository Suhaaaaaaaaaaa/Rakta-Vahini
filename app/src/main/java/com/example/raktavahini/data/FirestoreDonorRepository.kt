package com.example.raktavahini.data

import com.example.raktavahini.model.Donor
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import com.example.raktavahini.utils.LocationUtils

class FirestoreDonorRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun searchEligibleDonors(
        bloodGroup: String,
        radiusKm: Double,
        requesterLatitude: Double,
        requesterLongitude: Double,
        onResult: (List<Donor>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("donors")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("isReadyToDonate", true)
            .get()
            .addOnSuccessListener { result ->
                val firebaseDonors = result.documents.mapNotNull { document ->
                    val lastDonationText = document.getString("lastDonationDate") ?: ""

                    val hasDonationHistory = lastDonationText.isNotBlank()

                    val lastDonationDate = try {
                        if (hasDonationHistory) {
                            LocalDate.parse(lastDonationText)
                        } else {
                            LocalDate.now().minusDays(100)
                        }
                    } catch (e: Exception) {
                        LocalDate.now().minusDays(100)
                    }


                    val donorLatitude = document.getDouble("latitude")
                    val donorLongitude = document.getDouble("longitude")

                    if (donorLatitude == null || donorLongitude == null) {
                        return@mapNotNull null
                    }

                    val calculatedDistanceKm = LocationUtils.calculateDistanceKm(
                        startLatitude = requesterLatitude,
                        startLongitude = requesterLongitude,
                        endLatitude = donorLatitude,
                        endLongitude = donorLongitude
                    )


                    Donor(
                        id = document.id.hashCode(),
                        name = document.getString("name") ?: "Unknown Donor",
                        bloodGroup = document.getString("bloodGroup") ?: "",
                        location = document.getString("location") ?: "Unknown location",
                        distanceKm = calculatedDistanceKm,

                        phoneNumber = document.getString("phoneNumber") ?: "",
                        lastDonationDate = lastDonationDate,
                        isReadyToDonate = document.getBoolean("isReadyToDonate") ?: false,
                        hasDonationHistory = hasDonationHistory
                    )
                }.filter { donor ->
                    donor.distanceKm <= radiusKm && donor.isEligible()
                }

                val demoDonors = getDemoDonors(
                    requesterLatitude = requesterLatitude,
                    requesterLongitude = requesterLongitude
                ).filter { donor ->
                    donor.bloodGroup == bloodGroup &&
                            donor.distanceKm <= radiusKm &&
                            donor.isEligible()
                }

                val donors = (firebaseDonors + demoDonors)
                    .distinctBy { donor -> donor.id }
                    .sortedBy { donor -> donor.distanceKm }

                onResult(donors)
            }
            .addOnFailureListener { exception ->
                val demoDonors = getDemoDonors(
                    requesterLatitude = requesterLatitude,
                    requesterLongitude = requesterLongitude
                ).filter { donor ->
                    donor.bloodGroup == bloodGroup &&
                            donor.distanceKm <= radiusKm &&
                            donor.isEligible()
                }.sortedBy { donor -> donor.distanceKm }

                if (demoDonors.isNotEmpty()) {
                    onResult(demoDonors)
                } else {
                    onError(exception.message ?: "Failed to load donors")
                }
            }
    }

    private fun getDemoDonors(
        requesterLatitude: Double,
        requesterLongitude: Double
    ): List<Donor> {
        val aPositiveDistance = LocationUtils.calculateDistanceKm(
            startLatitude = requesterLatitude,
            startLongitude = requesterLongitude,
            endLatitude = requesterLatitude + 0.01,
            endLongitude = requesterLongitude + 0.01
        )

        val bPositiveDistance = LocationUtils.calculateDistanceKm(
            startLatitude = requesterLatitude,
            startLongitude = requesterLongitude,
            endLatitude = requesterLatitude + 0.025,
            endLongitude = requesterLongitude + 0.025
        )

        return listOf(
            Donor(
                id = -101,
                name = "Demo Donor Neha",
                bloodGroup = "A+",
                location = "Indiranagar, Bengaluru",
                distanceKm = aPositiveDistance,
                phoneNumber = "9876543210",
                lastDonationDate = LocalDate.parse("2025-01-01"),
                isReadyToDonate = true,
                hasDonationHistory = true
            ),
            Donor(
                id = -102,
                name = "Demo Donor Rahul",
                bloodGroup = "B+",
                location = "Jayanagar, Bengaluru",
                distanceKm = bPositiveDistance,
                phoneNumber = "9123456780",
                lastDonationDate = LocalDate.parse("2025-02-01"),
                isReadyToDonate = true,
                hasDonationHistory = true
            )
        )
    }
}
