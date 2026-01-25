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
    val contactsViewModel: ContactsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "contacts", "scan")
    val currentTab = when (currentRoute) { "scan" -> 0; "contacts" -> 2; else -> 1 }
    var showAddOverlay by remember { mutableStateOf(false) }

    LucaTheme {
        ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = false, drawerContent = {
            ModalDrawerSheet(drawerContainerColor = UIWhite) {
                SidebarContent(onCloseClick = { scope.launch { drawerState.close() } }, onDashboardClick = { scope.launch { drawerState.close(); if (currentRoute != "home") navController.navigate("home") { popUpTo("home") { inclusive = true } } } })
            }
        }) {
            Scaffold(
                floatingActionButton = {
                    if (showBottomBar) FloatingNavbar(selectedIndex = currentTab, onItemSelected = { index -> when (index) { 0 -> navController.navigate("scan"); 1 -> navController.navigate("home"); 2 -> navController.navigate("contacts") } }, onAddClick = { navController.navigate("add_event") }, onHomeClick = { navController.navigate("home") }, onContactsClick = { navController.navigate("contacts") })
                },
                floatingActionButtonPosition = FabPosition.Center, contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                NavHost(navController = navController, startDestination = "splash", modifier = Modifier.padding(innerPadding)) {
                    composable("splash") {
                        val auth = FirebaseAuth.getInstance()
                        LaunchedEffect(Unit) { delay(500); if (auth.currentUser != null) navController.navigate("home") else navController.navigate("greeting") }
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    composable("greeting") { GreetingScreen({ navController.navigate("login") }, { navController.navigate("sign_up") }, { navController.navigate("fill_profile") }, { navController.navigate("home") }) }
                    composable("login") { LoginScreen({ navController.navigate("final_login") }, { navController.navigate("sign_up") }, { navController.popBackStack() }) }
                    composable("sign_up") { SignUpScreen(authViewModel, { navController.popBackStack() }, { email -> navController.navigate("otp_screen/$email") }) }
                    composable("otp_screen/{email}", arguments = listOf(navArgument("email") { type = NavType.StringType })) {
                        val email = it.arguments?.getString("email") ?: ""
                        OtpScreen(email, authViewModel, { navController.popBackStack() }, { navController.navigate("fill_profile") })
                    }
                    composable("fill_profile") { FillProfileScreen({ navController.popBackStack() }, { name, avatar -> navController.navigate("final_signup/$name/$avatar") }) }
                    composable("final_login") { FinalScreen { navController.navigate("home") } }
                    composable("final_signup/{name}/{avatarName}") { FinalSignUpScreen(it.arguments?.getString("name") ?: "Crew", it.arguments?.getString("avatarName") ?: "avatar_1") { navController.navigate("home") } }

                    composable("home") {
                        val context = LocalContext.current.applicationContext
                        val repository = remember { LucaFirebaseRepository(context) }
                        val viewModel: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T })
                        HomeScreen(viewModel, { id -> navController.navigate("detailed_event/$id") }, { navController.navigate("contacts") }, { navController.navigate("add_event") }, { scope.launch { drawerState.open() } })
                    }
                    composable("contacts") { ContactsScreen { scope.launch { drawerState.open() } } }
                    composable("scan") {}

                    // --- DETAIL EVENT (UPDATED) ---
                    composable("detailed_event/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        DetailedEventScreen(
                            eventId,
                            onBackClick = { navController.popBackStack() },
                            onMenuClick = { scope.launch { drawerState.open() } }, // [BARU] Buka sidebar
                            onNavigateToAddActivity = { navController.navigate("new_activity") },
                            onNavigateToEditEvent = { id -> navController.navigate("add_event?eventId=$id") }
                        )
                    }

                    composable("add_event?eventId={eventId}", arguments = listOf(navArgument("eventId") { nullable = true })) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId")
                        AddScreen(onNavigateBack = { navController.popBackStack() }, eventId = eventId)
                    }

                    composable("new_activity") { AddActivityScreen({ navController.popBackStack() }, { navController.navigate("new_activity_2") }) }
                    composable("new_activity_2") { AddActivityScreen2({ navController.popBackStack() }, { navController.navigate("edit_activity") }) }
                    composable("edit_activity") { NewActivityEditScreen { navController.popBackStack() } }
                    composable("new_event") { NewEventScreen({ navController.popBackStack() }, { navController.navigate("add_event") }, { navController.navigate("new_activity") }, { scope.launch { drawerState.open() } }) }
                }

                if (showAddOverlay) {
                    Dialog(onDismissRequest = { showAddOverlay = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            UserProfileOverlay({ showAddOverlay = false }, { name, phone, banks, avatar -> contactsViewModel.addContact(name, phone, banks, avatar); showAddOverlay = false })
                        }
                    }
                }
            }
        }
    }
}