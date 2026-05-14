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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onEmergencySearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDonationLogClick: () -> Unit
) {



    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7A0019),
            Color(0xFFB11226),
            Color(0xFFFFF5F5)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Rakta-Vahini",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Filtered Blood Donor Network",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.96f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Emergency Blood Search",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF7A0019),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Search by blood group, readiness, and distance. Ineligible donors are hidden automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A4A4A)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onEmergencySearchClick,

                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB11226)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Start Emergency Search")
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onProfileClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7A0019)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "My Donor Profile")
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onDonationLogClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B2B2B)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Donation Log")
                    }


                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Private calling",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "90-day rule",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

        }
    }
}
