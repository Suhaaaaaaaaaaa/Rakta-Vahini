package com.example.raktavahini.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.content.Context
import android.location.Geocoder
import java.util.Locale


object LocationUtils {

    fun calculateDistanceKm(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val latitudeDifference = Math.toRadians(endLatitude - startLatitude)
        val longitudeDifference = Math.toRadians(endLongitude - startLongitude)

        val startLatitudeRadians = Math.toRadians(startLatitude)
        val endLatitudeRadians = Math.toRadians(endLatitude)

        val a = sin(latitudeDifference / 2) * sin(latitudeDifference / 2) +
                cos(startLatitudeRadians) *
                cos(endLatitudeRadians) *
                sin(longitudeDifference / 2) *
                sin(longitudeDifference / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
    fun getCoordinatesFromAddress(
        context: Context,
        address: String
    ): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                val location = results[0]
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}
