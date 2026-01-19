package com.example.luca.ui

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.luca.ui.theme.LucaTheme
// Pastikan semua screen dan komponen UI di-import dengan benar
import com.example.luca.ui.* class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UBAH BAGIAN INI:
        enableEdgeToEdge(
            // statusBarStyle.light = Background Terang (Putih), Ikon Gelap (Hitam)
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE, // Warna Background (Siang) -> PUTIH
                android.graphics.Color.WHITE  // Warna Background (Malam/Dark Mode) -> Tetap PUTIH
            ),
            // navigationBarStyle biarkan transparan agar navbar bawah tetap menyatu
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            LucaApp()
        }
    }
}

@Composable
fun LucaApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- STATE MANAGEMENT ---

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tentukan halaman mana yang menampilkan Navbar
    val mainTabs = listOf("home", "contacts", "scan")
    val showBottomBar = currentRoute in mainTabs

    // 0: Scan, 1: Home, 2: Contacts
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
            drawerContent = {
                ModalDrawerSheet {
                    SidebarContent(
                        onCloseClick = {
                            // scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                // 1. PINDAHKAN NAVBAR KE SINI (FLOATING ACTION BUTTON)
                floatingActionButton = {
                    if (showBottomBar) {
                        FloatingNavbar(
                            selectedIndex = currentTab,
                            onItemSelected = { index ->
                                when (index) {
                                    0 -> navController.navigate("scan") {
                                        launchSingleTop = true
                                        popUpTo("home")
                                    }
                                    1 -> navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                    2 -> navController.navigate("contacts") {
                                        launchSingleTop = true
                                        popUpTo("home")
                                    }
                                }
                            },
                            onAddClick = { showAddOverlay = true },
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
                // 2. SET POSISI KE TENGAH (CENTER)
                floatingActionButtonPosition = FabPosition.Center,

                // 3. HAPUS bottomBar (Supaya bar putih di belakang hilang)
                // bottomBar = { ... }, <--- DIBUANG

                // 4. Pastikan konten tembus full screen
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->

                // --- NAV HOST ---
                NavHost(
                    navController = navController,
                    startDestination = "greeting",
                    modifier = Modifier.padding(innerPadding)
                    // Note: innerPadding dari FAB biasanya 0 di bagian bawah,
                    // jadi kontenmu sekarang akan bablas sampai bawah layar (di belakang navbar)
                ) {

                    // === ONBOARDING & AUTH ===
                    composable("greeting") {
                        GreetingScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateToHome = { navController.navigate("home") }
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

                    // === MAIN APP ===
                    composable("home") {
                        HomeScreen(
                            onNavigateToDetail = { eventId -> navController.navigate("detailed_event") },
                            onContactsClick = { navController.navigate("contacts") },
                            onAddEventClick = { showAddOverlay = true }
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
                    composable("scan") {
//                        CameraScreen()
                    }

                    // === DETAIL PAGES ===
                    composable("detailed_event") {
                        DetailedEventScreen(
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
                    composable("add_event") {
                        AddScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("new_event") {
                        NewEventScreen(
                            onCloseClick = { navController.popBackStack() },
                            onEditClick = { navController.navigate("add_event") },
                            onAddActivityClick = { navController.navigate("new_activity") }
                        )
                    }
                }

                // --- OVERLAY ---
                if (showAddOverlay) {
                    Dialog(
                        onDismissRequest = { showAddOverlay = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            UserProfileOverlay()
                        }
                    }
                }
            }
        }
    }
}