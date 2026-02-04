package com.example.luca.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// --- FUNGSI 1: LOGIC WRAPPER ---
@Composable
fun GreetingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,         // Arah: Form Email/Password (User Manual)
    onNavigateToFillProfile: () -> Unit,    // Arah: Form Username/Avatar (User Google/X) -> PENTING!
    onNavigateToHome: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Web Client ID
    val webClientId = "119381624546-7f5ctjbbvdnd3f3civn56nct7s8ip4a0.apps.googleusercontent.com"

    Log.d("GreetingScreen", "Initializing with Web Client ID: $webClientId")

    // Observer 1: User Lama & Sukses -> Langsung Home
    LaunchedEffect(authViewModel.isSuccess) {
        if (authViewModel.isSuccess) {
            Toast.makeText(context, "Welcome Back!", Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
            onNavigateToHome()
        }
    }

    // Observer 2: User Baru / Hantu -> ISI PROFILE (Bukan Sign Up Email)
    LaunchedEffect(authViewModel.isNewUser) {
        if (authViewModel.isNewUser) {
            Toast.makeText(context, "Lengkapi profilmu dulu", Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
            // INI KUNCINYA: Jangan ke SignUp, tapi ke FillProfile
            onNavigateToFillProfile()
        }
    }

    // Observer 3: Error
    LaunchedEffect(authViewModel.errorMessage) {
        authViewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // --- LAUNCHER GOOGLE ---
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GreetingScreen", "=== Google Sign-In Result ===")
        Log.d("GreetingScreen", "Result Code: ${result.resultCode}")
        Log.d("GreetingScreen", "RESULT_OK = ${Activity.RESULT_OK}")
        Log.d("GreetingScreen", "RESULT_CANCELED = ${Activity.RESULT_CANCELED}")
        Log.d("GreetingScreen", "Data exists: ${result.data != null}")

        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d("GreetingScreen", "Sign-in result OK, processing...")
                result.data?.let { data ->
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val account = task.getResult(ApiException::class.java)

                        Log.d("GreetingScreen", "Account: ${account?.email}")
                        Log.d("GreetingScreen", "Display Name: ${account?.displayName}")
                        Log.d("GreetingScreen", "ID: ${account?.id}")

                        val idToken = account?.idToken
                        Log.d("GreetingScreen", "ID Token exists: ${idToken != null}")

                        if (idToken != null) {
                            Log.d("GreetingScreen", "ID Token length: ${idToken.length}")
                            authViewModel.googleLogin(idToken)
                        } else {
                            val msg = "Failed to get ID token from Google"
                            Log.e("GreetingScreen", msg)
                            Log.e("GreetingScreen", "This usually means:")
                            Log.e("GreetingScreen", "1. Web Client ID is wrong")
                            Log.e("GreetingScreen", "2. SHA-1 is not properly configured")
                            Log.e("GreetingScreen", "3. google-services.json is outdated")
                            Toast.makeText(context, "$msg\nCheck Logcat for details", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: ApiException) {
                        Log.e("GreetingScreen", "=== Google Sign-In Error ===")
                        Log.e("GreetingScreen", "Status Code: ${e.statusCode}")
                        Log.e("GreetingScreen", "Status Message: ${e.statusMessage}")
                        Log.e("GreetingScreen", "Status: ${e.status}")

                        val errorMsg = when (e.statusCode) {
                            10 -> {
                                Log.e("GreetingScreen", "ERROR 10: Developer Error")
                                Log.e("GreetingScreen", "Causes:")
                                Log.e("GreetingScreen", "- SHA-1 fingerprint not matching")
                                Log.e("GreetingScreen", "- Package name mismatch")
                                Log.e("GreetingScreen", "- google-services.json outdated")
                                "Developer Error (10)\n\nPossible causes:\n• SHA-1 not matching\n• Rebuild project needed\n• Check Firebase Console"
                            }
                            12501 -> {
                                Log.w("GreetingScreen", "ERROR 12501: User Cancelled or Config Error")
                                "Sign-in cancelled"
                            }
                            12500 -> {
                                Log.e("GreetingScreen", "ERROR 12500: Sign-in Failed")
                                "Sign-in failed. Try again"
                            }
                            7 -> {
                                Log.e("GreetingScreen", "ERROR 7: Network Error")
                                "Network error. Check internet"
                            }
                            8 -> {
                                Log.e("GreetingScreen", "ERROR 8: Internal Error")
                                "Internal error. Try again"
                            }
                            else -> {
                                Log.e("GreetingScreen", "ERROR ${e.statusCode}: Unknown")
                                "Error ${e.statusCode}. Try again"
                            }
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("GreetingScreen", "Unexpected exception: ${e.message}", e)
                        Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("GreetingScreen", "Result data is NULL!")
                    Toast.makeText(context, "No data returned from Google", Toast.LENGTH_SHORT).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.w("GreetingScreen", "Sign-in cancelled by user (or auto-cancelled)")
                // Don't show toast for user cancellation to avoid confusion
            }
            else -> {
                Log.e("GreetingScreen", "Unexpected result code: ${result.resultCode}")
            }
        }
    }

    // Handle Google Click
    val onGoogleClick: () -> Unit = {
        Log.d("GreetingScreen", "=== Google Sign-In Button Clicked ===")
        Log.d("GreetingScreen", "Web Client ID: $webClientId")
        Log.d("GreetingScreen", "Package: ${context.packageName}")

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, gso)

            // Check if already signed in
            val currentAccount = GoogleSignIn.getLastSignedInAccount(context)
            Log.d("GreetingScreen", "Current signed in account: ${currentAccount?.email}")

            if (currentAccount != null) {
                Log.d("GreetingScreen", "User already signed in, signing out first...")
                client.signOut().addOnCompleteListener {
                    Log.d("GreetingScreen", "Sign out complete, launching sign in...")
                    googleLauncher.launch(client.signInIntent)
                }
            } else {
                Log.d("GreetingScreen", "No previous sign in, launching sign in intent...")
                googleLauncher.launch(client.signInIntent)
            }
        } catch (e: Exception) {
            Log.e("GreetingScreen", "Error creating sign-in intent: ${e.message}", e)
            Toast.makeText(context, "Failed to start Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle X (Twitter) Click
    val onXClick: () -> Unit = {
        Log.d("GreetingScreen", "=== Twitter Sign-In Button Clicked ===")
        val activity = context as? Activity
        if (activity != null) {
            Log.d("GreetingScreen", "Activity context found, initiating Twitter login")
            authViewModel.twitterLogin(activity)
        } else {
            val msg = "Error: Context is not Activity"
            Log.e("GreetingScreen", msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(UIWhite)) {
        if (authViewModel.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = UIAccentYellow
            )
        }

        GreetingScreenContent(
            onGoogleClick = onGoogleClick,
            onXClick = onXClick,
            onSignUpClick = onNavigateToSignUp,
            onLoginClick = onNavigateToLogin
        )
    }
}

// --- UI CONTENT (TETAP SAMA) ---
@Composable
fun GreetingScreenContent(
    onGoogleClick: () -> Unit,
    onXClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIWhite)
            .padding(all = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_luca_logo),
                    contentDescription = "Luca Logo",
                    modifier = Modifier.size(width = 60.dp, height = 59.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Welcome to Luca!",
                    style = AppFont.SemiBold,
                    fontSize = 28.sp,
                    color = UIBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Splitting bills made easy.",
                    style = AppFont.Medium,
                    fontSize = 16.sp,
                    color = UIDarkGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(74.dp))

                SocialButton(
                    text = "Continue with Google",
                    iconRes = R.drawable.ic_google_logo,
                    onClick = onGoogleClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SocialButton(
                    text = "Continue with X",
                    iconRes = R.drawable.ic_x_logo,
                    onClick = onXClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(107.dp))

                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(49.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UIAccentYellow,
                        contentColor = UIBlack
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        style = AppFont.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(49.dp),
                    border = BorderStroke(1.dp, UIAccentYellow)
                ) {
                    Text(
                        text = "Log in",
                        style = AppFont.SemiBold,
                        fontSize = 14.sp,
                        color = UIBlack
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Privacy Policy   ·   Terms of Service",
                style = AppFont.SemiBold,
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f)
            )
        }
    }
}

// --- REUSABLE COMPONENT ---
@Composable
fun SocialButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(49.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = UIWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(14.dp))

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )

            Text(
                text = text,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(39.dp))
        }
    }
}

@Preview(showBackground = false)
@Composable
fun GreetingScreenPreview() {
    LucaTheme {
        GreetingScreenContent(
            onGoogleClick = {},
            onXClick = {},
            onSignUpClick = {},
            onLoginClick = {}
        )
    }
}