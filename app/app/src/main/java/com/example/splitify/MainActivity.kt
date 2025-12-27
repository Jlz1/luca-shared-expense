package com.example.splitify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitify.screens.auth.FillProfileScreen
import com.example.splitify.screens.auth.LoginScreen
import com.example.splitify.ui.theme.SplitifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitifyTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "fill_profile") {
                    composable("fill_profile") {
                        FillProfileScreen(
                            onNavigateToLogin = {
                                navController.navigate("login")
                            }
                        )
                    }

                    composable("login") {
                        LoginScreen()
                    }
                }
            }
        }
    }
}