package com.example.luca.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.luca.ui.theme.LucaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hapus enableEdgeToEdge() di sini jika bikin tampilan ketutup status bar
        // atau biarkan kalau sudah di-handle di Scaffold

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "greeting") {

                // --- ONBOARDING & AUTH ---
                composable("greeting") {
                    GreetingScreen(
                        onNavigateToLogin = { navController.navigate("login") },
                        onNavigateToSignUp = { navController.navigate("sign_up") },
                        onNavigateToHome = {
                            Toast.makeText(this@MainActivity, "Masuk ke Home...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onNavigateToHome = { navController.navigate("final_login") },
                        onNavigateToSignUp = { navController.navigate("sign_up") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("sign_up") {
                    SignUpScreen(
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = { navController.navigate("fill_profile") }
                    )
                }
                composable("fill_profile") {
                    FillProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onCreateAccountClick = { navController.navigate("final_signup") }
                    )
                }
                composable("final_login") {
                    FinalScreen(
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("greeting") { inclusive = true }
                            }
                        }
                    )
                }
                composable("final_signup") {
                    FinalSignUpScreen(
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("greeting") { inclusive = true }
                            }
                        }
                    )
                }

                // --- MAIN APP ---
                composable("home") {
                    HomeScreen(
                        onNavigateToDetail = { eventId -> navController.navigate("detailed_event") },
                        onContactsClick = { navController.navigate("contacts") },
                        onAddEventClick = { navController.navigate("add_event") }
                    )
                }

                // --- UPDATE: DETAILED EVENT (Pakai versi baru) ---
                composable("detailed_event") {
                    DetailedEventScreen(
                        // Nanti kalau sudah canggih, oper ID di sini: eventId = ...
                        onBackClick = { navController.popBackStack() },
                        onNavigateToAddActivity = { navController.navigate("new_activity") }
                    )
                }

                composable("detailed_activity") {
                    DetailedActivityScreen(
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate("edit_activity") }
                    )
                }

                composable("new_activity") {
                    AddActivityScreen(
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = { navController.navigate("new_activity_2") }
                    )
                }

                composable("new_activity_2") {
                    AddActivityScreen2(
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate("edit_activity") }
                    )
                }

                composable("edit_activity") {
                    NewActivityEditScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("contacts") {
                    ContactsScreen(
                        onHomeClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }

                // --- UPDATE: ADD EVENT (Pakai versi baru) ---
                composable("add_event") {
                    // Cukup handle navigasi Back aja.
                    // Logic "Continue/Save" sudah diurus ViewModel di dalam AddScreen.
                    AddScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // (Opsional) Screen lama kalau masih dipakai buat testing UI
                composable("new_event") {
                    NewEventScreen(
                        onCloseClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate("add_event") },
                        onAddActivityClick = { navController.navigate("new_activity") }
                    )
                }
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                0x00000000,
                0x00000000
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TemplatePreview() {
    LucaTheme {
        DetailedEventScreen()
    }
}