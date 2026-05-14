package com.example.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.raktavahini.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import androidx.compose.material3.TextButton
import android.widget.Toast

@Composable
fun DonationLogScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var hospitalName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    fun loadLogs() {
        if (userId != null) {
            firestore.collection("donors")
                .document(userId)
                .collection("donationLogs")
                .get()
                .addOnSuccessListener { result ->
                    logs = result.documents.mapNotNull { it.data }
                }
        }
    }

    LaunchedEffect(userId) {
        loadLogs()
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
            text = "Donation Log",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF7A0019),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Track your donations and update eligibility automatically.",
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital / Camp Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (userId != null && hospitalName.isNotBlank()) {
                            val today = LocalDate.now().toString()

                            val log = hashMapOf(
                                "hospitalName" to hospitalName,
                                "notes" to notes,
                                "date" to today
                            )

                            firestore.collection("donors")
                                .document(userId)
                                .collection("donationLogs")
                                .add(log)
                                .addOnSuccessListener {
                                    firestore.collection("donors")
                                        .document(userId)
                                        .update(
                                            mapOf(
                                                "lastDonationDate" to today,
                                                "isReadyToDonate" to false
                                            )
                                        )

                                    hospitalName = ""
                                    notes = ""
                                    loadLogs()
                                    NotificationHelper.showDonationThanks(context)
                                    Toast.makeText(
                                        context,
                                        "Thank you! Donation log saved.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB11226)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Donation")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "History",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A0019)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = log["hospitalName"].toString(),
                            fontWeight = FontWeight.Bold
                        )
                        Text("Date: ${log["date"]}")
                        Text("Notes: ${log["notes"]}")
                    }
                }
            }
        }
    }
}
