package com.example.luca.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.repository.AuthRepository
import com.example.luca.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun GreetingScreen(
    onNavigateToLogin: () -> Unit, // Callback ke Login Manual
    onNavigateToHome: () -> Unit   // Callback kalau sukses Google
) {
    // --- BAGIAN 1: LOGIC (Otak) ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }

    // 🔥 JANGAN LUPA: Paste Client ID kamu disini!
    val webClientId = "1193816...googleusercontent.com"

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    scope.launch {
                        val success = authRepo.signInWithGoogle(idToken)
                        if (success) {
                            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                            onNavigateToHome()
                        } else {
                            Toast.makeText(context, "Gagal simpan ke Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- BAGIAN 2: UI (Tampilan Bagus Kamu) ---
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

                    // Pastikan kamu punya gambar ini di res/drawable
                    // Image(
                    //    painter = painterResource(id = R.drawable.ic_luca_logo),
                    //    contentDescription = "Luca Logo",
                    //    modifier = Modifier.size(width = 60.dp, height = 59.dp)
                    // )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Welcome to Luca!",
                        style = AppFont.SemiBold, // Pastikan Font ini ada
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

                    // --- TOMBOL GOOGLE (SUDAH DISAMBUNG KABELNYA) ---
                    SocialButton(
                        text = "Continue with Google",
                        // iconRes = R.drawable.ic_google_logo, // Uncomment kalau icon ada
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(webClientId)
                                .requestEmail()
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleLauncher.launch(client.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TOMBOL FACEBOOK ---
                    SocialButton(
                        text = "Continue with Facebook",
                        // iconRes = R.drawable.ic_facebook_logo,
                        onClick = { Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TOMBOL X ---
                    SocialButton(
                        text = "Continue with X",
                        // iconRes = R.drawable.ic_x_logo,
                        onClick = { Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(107.dp))

                    // --- SIGN UP BUTTON ---
                    Button(
                        onClick = { onNavigateToLogin() }, // Sementara arahkan ke Login juga
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(49.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- LOG IN BUTTON ---
                    OutlinedButton(
                        onClick = { onNavigateToLogin() }, // Navigasi Manual Login
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(49.dp),
                        border = BorderStroke(1.dp, UIAccentYellow),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Log in",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack,
                            fontWeight = FontWeight.SemiBold
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
                    color = UIBlack.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- REUSABLE COMPONENT (TETAP DIPAKAI) ---
@Composable
fun SocialButton(
    text: String,
    // iconRes: Int, // Uncomment kalau icon sudah ada
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(49.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = UIWhite
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(14.dp))

            // Uncomment kalau icon sudah ada
            // Image(
            //     painter = painterResource(id = iconRes),
            //     contentDescription = null,
            //     modifier = Modifier.size(25.dp)
            // )

            Text(
                text = text,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(39.dp))
        }
    }
}

// Update Preview biar gak error karena butuh parameter
@Preview
@Composable
fun GreetingScreenPreview() {
    LucaTheme {
        GreetingScreen(onNavigateToLogin = {}, onNavigateToHome = {})
    }
}