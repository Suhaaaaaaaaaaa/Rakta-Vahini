package com.example.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.TextButton
import java.time.LocalDate
import java.time.temporal.ChronoUnit



@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isReadyToDonate by remember { mutableStateOf(false) }
    var lastDonationDate by remember { mutableStateOf("") }


    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("donors")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    name = document.getString("name") ?: ""
                    email = document.getString("email") ?: ""
                    bloodGroup = document.getString("bloodGroup") ?: ""
                    location = document.getString("location") ?: ""
                    lastDonationDate = document.getString("lastDonationDate") ?: ""

                    val daysSinceLastDonation = try {
                        if (lastDonationDate.isBlank()) {
                            999L
                        } else {
                            ChronoUnit.DAYS.between(LocalDate.parse(lastDonationDate), LocalDate.now())
                        }
                    } catch (e: Exception) {
                        999L
                    }

                    val eligibleByDate = daysSinceLastDonation >= 90
                    val savedReadyStatus = document.getBoolean("isReadyToDonate") ?: false
                    isReadyToDonate = eligibleByDate && savedReadyStatus

                    if (!eligibleByDate) {
                        firestore.collection("donors")
                            .document(userId)
                            .update("isReadyToDonate", false)
                    }

                }
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
            text = "Donor Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF7A0019),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(email, color = Color(0xFF666666))
                Text("Blood Group: $bloodGroup", fontWeight = FontWeight.SemiBold)
                Text("Location: $location")
                Text(
                    text = if (lastDonationDate.isBlank()) {
                        "Last Donation: No donation logged yet"
                    } else {
                        "Last Donation: $lastDonationDate"
                    },
                    color = Color(0xFF555555)
                )


                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ready to Donate",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B2B2B)
                    )

                    Switch(
                        checked = isReadyToDonate,
                        onCheckedChange = { newValue ->
                            val daysSinceLastDonation = try {
                                if (lastDonationDate.isBlank()) {
                                    999L
                                } else {
                                    ChronoUnit.DAYS.between(LocalDate.parse(lastDonationDate), LocalDate.now())
                                }
                            } catch (e: Exception) {
                                999L
                            }

                            val eligibleByDate = daysSinceLastDonation >= 90

                            if (eligibleByDate) {
                                isReadyToDonate = newValue

                                if (userId != null) {
                                    firestore.collection("donors")
                                        .document(userId)
                                        .update("isReadyToDonate", newValue)
                                }
                            } else {
                                isReadyToDonate = false

                                if (userId != null) {
                                    firestore.collection("donors")
                                        .document(userId)
                                        .update("isReadyToDonate", false)
                                }
                            }
                        }
                    )

                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7A0019)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log Out")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onDeleteAccountClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB11226)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Delete Account")
        }
    }
}
