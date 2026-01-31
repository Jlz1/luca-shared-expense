package com.example.luca.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.luca.model.Event
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite
import com.example.luca.viewmodel.AuthViewModel
import com.example.luca.viewmodel.ContactsViewModel
import com.example.luca.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    // =================================================================
    // 1. GLOBAL VIEWMODELS
    // =================================================================
    val contactsViewModel: ContactsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            gesturesEnabled = false,
            scrimColor = UIBlack.copy(alpha = 0.5f),
            drawerContent = {
                // 3. TRANSPARENT FULL SHEET
                ModalDrawerSheet(
                    drawerContainerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(1f),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Row(Modifier.fillMaxSize()) {

                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(0.60f)
                                .fillMaxHeight()
                                .background(UIWhite)
                        ) {
                            val fixedWidth = maxWidth

                            Box(
                                modifier = Modifier
                                    .width(fixedWidth)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Box(modifier = Modifier.width(fixedWidth).fillMaxHeight()) {
                                    SidebarContent(
                                        onCloseClick = { scope.launch { drawerState.close() } },
                                        onDashboardClick = {
                                            scope.launch {
                                                drawerState.close()
                                                if (currentRoute != "home") {
                                                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                                }
                                            }
                                        },
                                        onAccountSettingsClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("account_settings")
                                            }
                                        },
                                        onLogoutClick = {
                                            scope.launch {
                                                drawerState.close()
                                                FirebaseAuth.getInstance().signOut()
                                                navController.navigate("greeting") { popUpTo(0) { inclusive = true } }
                                            }
                                        },
                                        onSettingsClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("settings")
                                            }
                                        },
                                        onReportBugClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("report_bugs")
                                            }
                                        },
                                        onAboutUsClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("about_us")
                                            }
                                        },
                                        onHelpSupportClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("help_support")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // [B] DEAD ZONE (7%)
                        Box(
                            modifier = Modifier
                                .weight(0.07f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* ZONK */ }
                        )

                        // [C] CLOSE ZONE (33%)
                        Box(
                            modifier = Modifier
                                .weight(0.33f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { scope.launch { drawerState.close() } }
                        )
                    }
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

                    // 3. MAIN APP
                    composable("home") {
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
                        ContactsScreen(
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable("scan") {
                        ScanScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("account_settings") {
                        AccountSettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onLogoutClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("greeting") { popUpTo(0) { inclusive = true } }
                            }
                        )
                    }

                    composable("about_us") {
                        AboutUsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onAboutUsClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("about_us")
                                }
                            },
                            onAccountSettingsClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("account_settings")
                                }
                            }
                        )
                    }

                    composable("report_bugs") {
                        ReportBugScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("help_support") {
                        HelpSupportScreen(
                            onBackClick = { navController.popBackStack() },
                            onReportBugClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("report_bugs")
                                }
                            }
                        )
                    }

                    composable("detailed_event/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        DetailedEventScreen(
                            eventId = eventId,
                            onBackClick = { navController.popBackStack() },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onNavigateToAddActivity = { navController.navigate("new_activity/$eventId") },
                            onNavigateToEditEvent = { id ->
                                navController.navigate("add_event?eventId=$id")
                            }
                        )
                    }

                    composable(
                        route = "add_event?eventId={eventId}",
                        arguments = listOf(navArgument("eventId") { nullable = true })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId")
                        AddScreen(
                            onNavigateBack = { navController.popBackStack() },
                            eventId = eventId
                        )
                    }

                    composable("detailed_activity") {
                        DetailedActivityScreen(onBackClick = { navController.popBackStack() }, onEditClick = { navController.navigate("edit_activity") })
                    }
                    composable("new_activity/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        val repository = remember { LucaFirebaseRepository() }
                        var eventData by remember { mutableStateOf<Event?>(null) }

                        LaunchedEffect(eventId) {
                            eventData = repository.getEventById(eventId)
                        }

                        if (eventData != null) {
                            AddActivityScreen(
                                eventId = eventId,
                                event = eventData!!,
                                onBackClick = { navController.popBackStack() },
                                onDoneClick = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("new_activity_2") {
                        AddActivityScreen2(onBackClick = { navController.popBackStack() })
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

                // --- GLOBAL OVERLAY ADD CONTACT ---
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