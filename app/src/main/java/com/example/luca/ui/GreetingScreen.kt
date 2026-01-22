package com.example.luca.ui

import android.app.Activity
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
import androidx.compose.runtime.rememberCoroutineScope
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
    val scope = rememberCoroutineScope()

    // Web Client ID
    val webClientId = "119381624546-7f5ctjbbvdnd3f3civn56nct7s8ip4a0.apps.googleusercontent.com"

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
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // --- LAUNCHER GOOGLE ---
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authViewModel.googleLogin(idToken)
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle Google Click
    val onGoogleClick: () -> Unit = {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        googleLauncher.launch(client.signInIntent)
    }

    // Handle X (Twitter) Click
    val onXClick: () -> Unit = {
        val activity = context as? Activity
        if (activity != null) {
            authViewModel.twitterLogin(activity)
        } else {
            Toast.makeText(context, "Error: Context bukan Activity", Toast.LENGTH_SHORT).show()
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