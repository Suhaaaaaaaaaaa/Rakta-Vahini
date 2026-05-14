package com.example.raktavahini.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.example.raktavahini.data.FirestoreDonorRepository
import com.example.raktavahini.model.Donor
import androidx.compose.material3.TextButton
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices



@Composable
fun SearchScreen(
    onBackClick: () -> Unit
) {

    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var radiusKm by remember { mutableFloatStateOf(10f) }
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var requesterLatitude by remember { mutableStateOf<Double?>(null) }
    var requesterLongitude by remember { mutableStateOf<Double?>(null) }



    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    val repository = remember { FirestoreDonorRepository() }
    var donors by remember { mutableStateOf<List<Donor>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocationPermission || hasCoarseLocationPermission) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        requesterLatitude = location.latitude
                        requesterLongitude = location.longitude
                    }
                }
        }
    }


    LaunchedEffect(selectedBloodGroup, radiusKm, requesterLatitude, requesterLongitude) {
        val latitude = requesterLatitude
        val longitude = requesterLongitude

        if (latitude != null && longitude != null) {
            repository.searchEligibleDonors(
                bloodGroup = selectedBloodGroup,
                radiusKm = radiusKm.toDouble(),
                requesterLatitude = latitude,
                requesterLongitude = longitude,
                onResult = { result ->
                    donors = result
                    errorMessage = null
                },
                onError = { error ->
                    errorMessage = error
                }
            )
        } else {
            errorMessage = "Waiting for location permission or GPS fix."
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F5))
            .padding(16.dp)
    ) {
        TextButton(
            onClick = onBackClick
        ) {
            Text("Back")
        }

        Text(
            text = "Emergency Search",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF7A0019),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Only eligible and ready donors are shown.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Blood Group",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.height(110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bloodGroups.chunked(4)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { group ->
                        FilterChip(
                            selected = selectedBloodGroup == group,
                            onClick = { selectedBloodGroup = group },
                            label = { Text(group) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Search Radius: ${radiusKm.toInt()} km",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )

        Slider(
            value = radiusKm,
            onValueChange = { radiusKm = it },
            valueRange = 5f..20f,
            steps = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Eligible Donors Found: ${donors.size}",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A0019)
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                color = Color.Red
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(donors) { donor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = donor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B2B2B)
                        )

                        Text(
                            text = "${donor.bloodGroup} • ${donor.location}",
                            color = Color(0xFF555555)
                        )

                        Text(
                            text = "Distance: ${"%.1f".format(donor.distanceKm)} km away",
                            color = Color(0xFF777777)
                        )

                        Text(
                            text = if (donor.hasDonationHistory) {
                                "Last Donation: ${donor.lastDonationDate}"
                            } else {
                                "Last Donation: No donation logged yet"
                            },
                            color = Color(0xFF777777)
                        )


                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val phoneNumber = donor.phoneNumber.ifBlank { "100" }
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phoneNumber")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Call Donor Securely")
                        }

                    }
                }
            }
        }
    }
}
