package com.example.raktavahini.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user

                user?.reload()?.addOnSuccessListener {
                    if (user.isEmailVerified) {
                        onResult(true, null)
                    } else {
                        auth.signOut()
                        onResult(false, "Please verify your email before logging in.")
                    }
                }?.addOnFailureListener { exception ->
                    onResult(false, exception.message)
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }


    fun signUp(
        name: String,
        email: String,
        password: String,
        bloodGroup: String,
        location: String,
        phoneNumber: String,
        latitude: Double,
        longitude: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                val userId = user?.uid ?: return@addOnSuccessListener

                val donorProfile = hashMapOf(
                    "userId" to userId,
                    "name" to name,
                    "email" to email,
                    "bloodGroup" to bloodGroup,
                    "location" to location,
                    "lastDonationDate" to "",
                    "isReadyToDonate" to true,
                    "distanceKm" to 5.0,
                    "phoneNumber" to phoneNumber,
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                firestore.collection("donors")
                    .document(userId)
                    .set(donorProfile)
                    .addOnSuccessListener {
                        deleteOldDonorProfiles(
                            currentUserId = userId,
                            email = email,
                            phoneNumber = phoneNumber,
                            onComplete = {
                                sendVerificationAndSignOut(user, onResult)
                            },
                            onError = {
                                sendVerificationAndSignOut(user, onResult)
                            }
                        )
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, exception.message ?: "Could not save donor profile.")
                    }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser ?: return false
        return if (user.isEmailVerified) {
            true
        } else {
            auth.signOut()
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun deleteAccount(
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        val userId = user?.uid

        if (user == null || userId == null) {
            onResult(false, "No logged-in user found.")
            return
        }

        firestore.collection("donors")
            .get()
            .addOnSuccessListener { donorSnapshot ->
                val currentDonorDocument = donorSnapshot.documents.firstOrNull { document ->
                    document.id == userId || document.getString("userId") == userId
                }
                val currentPhoneNumber = currentDonorDocument?.getString("phoneNumber")

                val matchingDonorDocuments = donorSnapshot.documents.filter { document ->
                    document.id == userId ||
                            document.getString("userId") == userId ||
                            document.getString("email") == user.email ||
                            (
                                    !currentPhoneNumber.isNullOrBlank() &&
                                            document.getString("phoneNumber") == currentPhoneNumber
                                    )
                }

                val batch = firestore.batch()

                if (matchingDonorDocuments.isEmpty()) {
                    deleteFirebaseUser(onResult)
                    return@addOnSuccessListener
                }

                var pendingLogQueries = matchingDonorDocuments.size

                matchingDonorDocuments.forEach { donorDocument ->
                    donorDocument.reference.collection("donationLogs")
                        .get()
                        .addOnSuccessListener { logs ->
                            logs.documents.forEach { logDocument ->
                                batch.delete(logDocument.reference)
                            }

                            batch.delete(donorDocument.reference)
                            pendingLogQueries--

                            if (pendingLogQueries == 0) {
                                batch.commit()
                                    .addOnSuccessListener {
                                        deleteFirebaseUser(onResult)
                                    }
                                    .addOnFailureListener { exception ->
                                        onResult(false, exception.message ?: "Could not delete donor details.")
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            onResult(false, exception.message ?: "Could not load donation logs.")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message ?: "Could not load donor details.")
            }
    }

    private fun deleteFirebaseUser(
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onResult(false, "No logged-in user found.")
            return
        }

        user.delete()
            .addOnSuccessListener {
                onResult(true, "Account and donor details deleted.")
            }
            .addOnFailureListener { exception ->
                val message = if (exception is FirebaseAuthRecentLoginRequiredException) {
                    "Please log out, log in again, then delete your account."
                } else {
                    exception.message ?: "Could not delete Firebase account."
                }
                onResult(false, message)
            }
    }

    private fun sendVerificationAndSignOut(
        user: com.google.firebase.auth.FirebaseUser,
        onResult: (Boolean, String?) -> Unit
    ) {
        user.sendEmailVerification()
            .addOnSuccessListener {
                auth.signOut()
                onResult(true, "Verification email sent. Please check your inbox.")
            }
            .addOnFailureListener { exception ->
                auth.signOut()
                onResult(false, exception.message ?: "Account created, but verification email could not be sent.")
            }
    }

    private fun deleteOldDonorProfiles(
        currentUserId: String,
        email: String,
        phoneNumber: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("donors")
            .get()
            .addOnSuccessListener { donorSnapshot ->
                val oldDonorDocuments = donorSnapshot.documents.filter { document ->
                    val documentUserId = document.getString("userId")
                    val documentEmail = document.getString("email")
                    val documentPhone = document.getString("phoneNumber")

                    document.id != currentUserId &&
                            documentUserId != currentUserId &&
                            (
                                    documentEmail.equals(email, ignoreCase = true) ||
                                            documentPhone == phoneNumber
                                    )
                }

                if (oldDonorDocuments.isEmpty()) {
                    onComplete()
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                oldDonorDocuments.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Could not remove old donor profiles.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not check old donor profiles.")
            }
    }
}
