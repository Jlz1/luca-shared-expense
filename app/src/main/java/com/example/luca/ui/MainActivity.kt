package com.example.luca.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.WHITE),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent { LucaApp() }
    }
}

@Composable
fun LucaApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // --- STATE MANAGEMENT ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainTabs = listOf("home", "contacts", "scan")
    val showBottomBar = currentRoute in mainTabs
    val currentTab = when (currentRoute) {
        "scan" -> 0
        "contacts" -> 2
        else -> 1
    }
    var showAddOverlay by remember { mutableStateOf(false) }

    LucaTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showBottomBar,
            drawerContent = { ModalDrawerSheet { SidebarContent(onCloseClick = {}) } }
        ) {
            Scaffold(
                floatingActionButton = {
                    if (showBottomBar) {
                        FloatingNavbar(
                            selectedIndex = currentTab,
                            onItemSelected = { index ->
                                when (index) {
                                    0 -> navController.navigate("scan") { launchSingleTop = true; popUpTo("home") }
                                    1 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                    2 -> navController.navigate("contacts") { launchSingleTop = true; popUpTo("home") }
                                }
                            },
                            onAddClick = { navController.navigate("add_event") },
                            onHomeClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onContactsClick = {
                                navController.navigate("contacts")
                            }
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->

                // --- NAV HOST --
                // START DESTINATION: Splash (Netral)
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // 1. SPLASH SCREEN
                    composable("splash") {
                        val auth = FirebaseAuth.getInstance()
                        LaunchedEffect(Unit) {
                            delay(500)
                            if (auth.currentUser != null) {
                                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                            } else {
                                navController.navigate("greeting") { popUpTo("splash") { inclusive = true } }
                            }
                        }
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    // 2. AUTH FLOW
                    composable("greeting") {
                        GreetingScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateToHome = { navController.navigate("home") { popUpTo("greeting") { inclusive = true } } }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onNavigateToHome = { navController.navigate("final_login") { popUpTo("greeting") { inclusive = true } } },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("sign_up") {
                        SignUpScreen(
                            onBackClick = { navController.popBackStack() },
                            // PENTING: Mengarah ke Fill Profile
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
                        val name = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
                        FinalScreen(name = name, onNavigateToHome = { navController.navigate("home") { popUpTo("greeting") { inclusive = true } } })
                    }
                    composable("final_signup") {
                        val name = FirebaseAuth.getInstance().currentUser?.displayName ?: "Crew"
                        FinalSignUpScreen(name = name, onNavigateToHome = { navController.navigate("home") { popUpTo("greeting") { inclusive = true } } })
                    }

                    // 3. MAIN APP
                    composable("home") {
                        val repository = remember { LucaFirebaseRepository() }
                        val viewModel: HomeViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return HomeViewModel(repository) as T
                                }
                            }
                        )
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { eventId -> navController.navigate("detailed_event") },
                            onContactsClick = { navController.navigate("contacts") },
                            onAddEventClick = { navController.navigate("add_event") }
                        )
                    }
                    composable("contacts") { ContactsScreen() }
                    composable("scan") {} // CameraScreen

                    // 4. DETAILS & ADD
                    composable("detailed_event") {
                        DetailedEventScreen(onBackClick = { navController.popBackStack() }, onNavigateToAddActivity = { navController.navigate("new_activity") })
                    }
                    composable("detailed_activity") {
                        DetailedActivityScreen(onBackClick = { navController.popBackStack() }, onEditClick = { navController.navigate("edit_activity") })
                    }
                    composable("new_activity") {
                        AddActivityScreen(onBackClick = { navController.popBackStack() }, onContinueClick = { navController.navigate("new_activity_2") })
                    }
                    composable("new_activity_2") {
                        AddActivityScreen2(onBackClick = { navController.popBackStack() }, onEditClick = { navController.navigate("edit_activity") })
                    }
                    composable("edit_activity") { NewActivityEditScreen(onBackClick = { navController.popBackStack() }) }
                    composable("add_event") { AddScreen(onNavigateBack = { navController.popBackStack() }) }
                    composable("new_event") {
                        NewEventScreen(onCloseClick = { navController.popBackStack() }, onEditClick = { navController.navigate("add_event") }, onAddActivityClick = { navController.navigate("new_activity") })
                    }
                }

                if (showAddOverlay) {
                    Dialog(onDismissRequest = { showAddOverlay = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UserProfileOverlay(onClose = { showAddOverlay = false }, onAddContact = {})
                        }
                    }
                }
            }
        }
    }
}