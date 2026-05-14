package com.example.raktavahini.ui

import com.example.raktavahini.ui.screens.DonationLogScreen
import com.example.raktavahini.ui.screens.ProfileScreen
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raktavahini.data.AuthRepository
import com.example.raktavahini.ui.screens.HomeScreen
import com.example.raktavahini.ui.screens.LoginScreen
import com.example.raktavahini.ui.screens.SearchScreen
import com.example.raktavahini.ui.screens.SignUpScreen

@Composable
fun RaktaVahiniApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }

    NavHost(
        navController = navController,
        startDestination = if (authRepository.isUserLoggedIn()) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    authRepository.login(email, password) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        } else {
                            Toast.makeText(context, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onCreateAccountClick = { name, email, password, bloodGroup, location, phoneNumber, latitude, longitude ->

                authRepository.signUp(
                        name = name,
                        email = email,
                        password = password,
                        bloodGroup = bloodGroup,
                        location = location,
                        phoneNumber = phoneNumber,
                    latitude = latitude,
                    longitude = longitude,


                    ) { success, message ->
                        if (success) {
                            Toast.makeText(
                                context,
                                message ?: "Account created. Verify your email before login.",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.navigate("login") {
                                popUpTo("signup") {
                                    inclusive = true
                                }
                            }

                        } else {
                            Toast.makeText(context, message ?: "Sign up failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onEmergencySearchClick = {
                    navController.navigate("search")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onDonationLogClick = {
                    navController.navigate("donation_log")
                }
            )
        }



        composable("search") {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {

                authRepository.logout()
                    navController.navigate("login") {
                        popUpTo("home") {
                            inclusive = true
                        }
                    }
                },
                onDeleteAccountClick = {
                    authRepository.deleteAccount { success, message ->
                        Toast.makeText(
                            context,
                            message ?: if (success) "Account deleted" else "Delete failed",
                            Toast.LENGTH_LONG
                        ).show()

                        if (success) {
                            navController.navigate("login") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }
        composable("donation_log") {
            DonationLogScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }


    }

}
