package com.example.luca.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.luca.ui.theme.LucaTheme
import android.widget.Toast
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus enableEdgeToEdge() jika menyebabkan masalah atau pastikan sudah di-import
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "greeting") {
                composable("greeting") {
                    GreetingScreen(
                        onNavigateToLogin = {
                            // Pindah ke LoginScreen
                            navController.navigate("login")
                        },
                        onNavigateToSignUp = {
                            // Pindah ke SignUpScreen
                            navController.navigate("sign_up")
                        },
                        onNavigateToHome = {
                            // Logika kalau sukses login
                            Toast.makeText(this@MainActivity, "Masuk ke Home...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onBackClick = { navController.popBackStack() },
                        onLoginClick = {
                            // Pindah ke FinalLoginScreen setelah login
                            navController.navigate("final_login")
                        },
                        onSignUpClick = {
                            navController.navigate("sign_up")
                        }
                    )
                }
                composable("sign_up") {
                    SignUpScreen(
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = {
                            navController.navigate("fill_profile")
                        }
                    )
                }
                composable("fill_profile") {
                    FillProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onCreateAccountClick = {
                            // Pindah ke FinalSignUp atau Home setelah create account
                            navController.navigate("final_signup")
                        }
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
                composable("home") {
                    HomeScreen(
                        onEventClick = {
                            navController.navigate("detailed_event")
                        },
                        onContactsClick = {
                            navController.navigate("contacts")
                        },
                        onAddEventClick = {
                            navController.navigate("add_event")
                        }
                    )
                }
                composable("detailed_event") {
                    DetailedEventScreen(
                        onBackClick = { navController.popBackStack() },
                        onActivityClick = {
                            navController.navigate("detailed_activity")
                        },
                        onAddActivityClick = {
                            navController.navigate("new_activity")
                        }
                    )
                }
                composable("detailed_activity") {
                    DetailedActivityScreen()
                }
                composable("new_activity") {
                    AddActivityScreen()
                }
                composable("contacts") {
                    ContactsScreen()
                }
                composable("add_event") {
                    AddScreen()
                }
            }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                0x00000000, // Warna background scrim (transparan aja)
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
