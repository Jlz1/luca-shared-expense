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

        // Konfigurasi Status Bar & Nav Bar (Transparan/Putih)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            ),
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
                // 1. FLOATING ACTION BUTTON SEBAGAI NAVBAR
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

                // 3. Pastikan konten tembus full screen
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->

                // --- NAV HOST ---
                // FIX: Gunakan "splash" sebagai start destination netral
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.padding(innerPadding)
                ) {

                    // === 1. SPLASH SCREEN (LOGIKA PENENTU) ===
                    composable("splash") {
                        val auth = FirebaseAuth.getInstance()
                        // Cek status login sekali saja saat aplikasi dibuka
                        LaunchedEffect(Unit) {
                            // Opsional: Delay sedikit biar gak kedip terlalu cepat
                            delay(500)

                            if (auth.currentUser != null) {
                                // User Sudah Login -> Masuk Home
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                // User Belum Login -> Masuk Greeting
                                navController.navigate("greeting") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }

                        // Tampilan Loading Sementara
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // === ONBOARDING & AUTH ===
                    composable("greeting") {
                        GreetingScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateToHome = {
                                // Login Sosmed sukses -> Langsung Home
                                navController.navigate("home") { popUpTo("greeting") { inclusive = true } }
                            }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onNavigateToHome = {
                                // Login Manual sukses -> Ke Final Screen dulu (Welcome Back)
                                navController.navigate("final_login") { popUpTo("greeting") { inclusive = true } }
                            },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("sign_up") {
                        SignUpScreen(
                            onBackClick = { navController.popBackStack() },
                            // Logika: Sign Up Sukses -> Isi Profile
                            onContinueClick = { navController.navigate("fill_profile") }
                        )
                    }
                    composable("fill_profile") {
                        FillProfileScreen(
                            onBackClick = { navController.popBackStack() },
                            // Logika: Profile Sukses -> Welcome Screen
                            onCreateAccountClick = { navController.navigate("final_signup") }
                        )
                    }
                    composable("final_login") {
                        // Ambil nama user untuk ditampilkan
                        val auth = FirebaseAuth.getInstance()
                        val name = auth.currentUser?.displayName ?: "User"
                        FinalScreen(
                            name = name,
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("greeting") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("final_signup") {
                        // Ambil nama user untuk ditampilkan
                        val auth = FirebaseAuth.getInstance()
                        val name = auth.currentUser?.displayName ?: "Crew"
                        FinalSignUpScreen(
                            name = name,
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("greeting") { inclusive = true }
                                }
                            }
                        )
                    }

                    // === MAIN APP ===
                    composable("home") {
                        // --- INJEKSI VIEWMODEL & REPOSITORY ---
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
                        // CameraScreen() // Aktifkan jika sudah ada
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
                            UserProfileOverlay(
                                onClose = { showAddOverlay = false },
                                onAddContact = { println("Add Contact") }
                            )
                        }
                    }
                }
            }
        }
    }
}