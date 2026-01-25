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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIWhite
import com.example.luca.viewmodel.AuthViewModel
import com.example.luca.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.luca.viewmodel.ContactsViewModel

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
    // 1. GLOBAL VIEWMODELS
    val contactsViewModel: ContactsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // STATE NAVIGATION BAR
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
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = UIWhite) {
                    SidebarContent(
                        onCloseClick = { scope.launch { drawerState.close() } },
                        onDashboardClick = {
                            scope.launch {
                                drawerState.close()
                                if (currentRoute != "home") {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }
            }
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
                            onAddClick = { navController.navigate("add_event") }, // Default Add (Create New)
                            onHomeClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                            onContactsClick = { navController.navigate("contacts") }
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // --- 1. SPLASH & AUTH ---
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

                    composable("greeting") {
                        GreetingScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onNavigateToSignUp = { navController.navigate("sign_up") },
                            onNavigateToFillProfile = { navController.navigate("fill_profile") },
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
                            authViewModel = authViewModel,
                            onBackClick = { navController.popBackStack() },
                            onNavigateToOtp = { email -> navController.navigate("otp_screen/$email") }
                        )
                    }
                    composable(
                        route = "otp_screen/{email}",
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        OtpScreen(
                            emailTujuan = email,
                            authViewModel = authViewModel,
                            onBackClick = { navController.popBackStack() },
                            onVerificationSuccess = {
                                navController.navigate("fill_profile") { popUpTo("greeting") { inclusive = false } }
                            }
                        )
                    }
                    composable("fill_profile") {
                        FillProfileScreen(
                            onBackClick = { navController.popBackStack() },
                            onCreateAccountClick = { name, avatarName ->
                                navController.navigate("final_signup/$name/$avatarName")
                            }
                        )
                    }
                    composable("final_login") {
                        FinalScreen(onNavigateToHome = { navController.navigate("home") { popUpTo("greeting") { inclusive = true } } })
                    }
                    composable("final_signup/{name}/{avatarName}") { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: "Crew"
                        val avatarName = backStackEntry.arguments?.getString("avatarName") ?: "avatar_1"
                        FinalSignUpScreen(
                            name = name,
                            avatarName = avatarName,
                            onNavigateToHome = { navController.navigate("home") { popUpTo("greeting") { inclusive = true } } }
                        )
                    }

                    // --- 2. MAIN APP ---
                    composable("home") {
                        // Pass Context ke Repository (karena kita ubah repository butuh context)
                        val context = LocalContext.current.applicationContext
                        val repository = remember { LucaFirebaseRepository(context) }

                        val viewModel: HomeViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return HomeViewModel(repository) as T
                                }
                            }
                        )
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { eventId -> navController.navigate("detailed_event/$eventId") },
                            onContactsClick = { navController.navigate("contacts") },
                            onAddEventClick = { navController.navigate("add_event") },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable("contacts") {
                        ContactsScreen(onMenuClick = { scope.launch { drawerState.open() } })
                    }
                    composable("scan") {}

                    // --- 3. DETAIL & ADD EVENT (INI YANG PENTING) ---

                    // Route Detail
                    composable("detailed_event/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

                        DetailedEventScreen(
                            eventId = eventId,
                            onBackClick = { navController.popBackStack() },
                            onNavigateToAddActivity = { navController.navigate("new_activity") },
                            // Callback saat tombol Edit ditekan -> Kirim ID ke layar Add
                            onNavigateToEditEvent = { id ->
                                navController.navigate("add_event?eventId=$id")
                            }
                        )
                    }

                    // Route Add/Edit Event
                    composable(
                        route = "add_event?eventId={eventId}",
                        arguments = listOf(navArgument("eventId") { nullable = true })
                    ) { backStackEntry ->
                        // AMBIL ID DARI URL
                        val eventId = backStackEntry.arguments?.getString("eventId")

                        // KIRIM ID KE SCREEN (Agar ViewModel tahu ini mode Edit)
                        AddScreen(
                            onNavigateBack = { navController.popBackStack() },
                            eventId = eventId
                        )
                    }

                    // --- 4. ACTIVITIES ---
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

                    composable("new_event") {
                        NewEventScreen(
                            onCloseClick = { navController.popBackStack() },
                            onEditClick = { navController.navigate("add_event") },
                            onAddActivityClick = { navController.navigate("new_activity") },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                }

                // Overlay Contact
                if (showAddOverlay) {
                    Dialog(
                        onDismissRequest = { showAddOverlay = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UserProfileOverlay(
                                onClose = { showAddOverlay = false },
                                onAddContact = { name, phone, banks, avatarName ->
                                    contactsViewModel.addContact(name, phone, banks, avatarName)
                                    showAddOverlay = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}