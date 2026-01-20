package com.example.luca.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit, // Diganti jadi navigasi ke Home kalau sukses
    authViewModel: AuthViewModel = viewModel() // Inject ViewModel
) {
    // State Form
    var name by remember { mutableStateOf("") } // Tambahan: Input Nama
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // --- LOGIKA UTAMA ---
    // 1. Cek jika sukses register -> Pindah ke Home
    LaunchedEffect(authViewModel.isSuccess) {
        if (authViewModel.isSuccess) {
            Toast.makeText(context, "Akun Berhasil Dibuat!", Toast.LENGTH_LONG).show()
            authViewModel.resetState()
            onNavigateToHome()
        }
    }

    // 2. Cek jika ada error
    LaunchedEffect(authViewModel.errorMessage) {
        authViewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(UIWhite)
    ) {
        // Loading Indicator (Optional)
        if (authViewModel.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = UIAccentYellow
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 30.dp),
        ) {

            // BOX 1: HEADER (Back Icon)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = UIBlack,
                    modifier = Modifier
                        .size(29.dp)
                        .clickable { onBackClick() }
                )
            }

            // BOX 2: CONTENT (Middle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Welcome to Luca!",
                            style = AppFont.SemiBold,
                            fontSize = 28.sp,
                            color = UIBlack,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.ic_luca_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(34.5.dp, 34.dp)
                        )
                    }

                    Text(
                        text = "Splitting bills made easy.",
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // INPUT NAMA (Baru ditambahkan agar sesuai database)
                    SignUpInputForm(
                        text = name,
                        onValueChange = { name = it },
                        placeholder = "Full Name",
                        // Pakai icon user atau email sementara kalau belum ada icon user
                        iconRes = R.drawable.ic_email_form
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    SignUpInputForm(
                        text = email,
                        onValueChange = { email = it },
                        placeholder = "Email Address",
                        iconRes = R.drawable.ic_email_form
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    SignUpInputForm(
                        text = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        iconRes = R.drawable.ic_password_form,
                        isPasswordField = true,
                        isPasswordVisible = isPasswordVisible,
                        onVisibilityChange = { isPasswordVisible = !isPasswordVisible }
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    SignUpInputForm(
                        text = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        iconRes = R.drawable.ic_password_form,
                        isPasswordField = true,
                        isPasswordVisible = isConfirmPasswordVisible,
                        onVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            // Validasi Sederhana sebelum kirim ke ViewModel
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                            } else if (password != confirmPassword) {
                                Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                            } else {
                                // EKSEKUSI KE DATABASE
                                authViewModel.register(email, password, name)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !authViewModel.isLoading, // Matikan tombol saat loading
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack,
                            disabledContainerColor = UIAccentYellow.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (authViewModel.isLoading) {
                            CircularProgressIndicator(
                                color = UIBlack,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Continue",
                                style = AppFont.Medium,
                                fontSize = 14.sp,
                                color = UIBlack,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // BOX 3: FOOTER (Bottom)
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

// Component Form tetap sama
@Composable
fun SignUpInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.width(27.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(15.dp, 10.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = AppFont.Medium.fontFamily,
                        fontSize = 14.sp,
                        color = UIBlack
                    ),
                    singleLine = true,
                    visualTransformation = if (isPasswordField && !isPasswordVisible)
                        PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth() // Tambahan agar text field full width
                )
            }

            if (isPasswordField) {
                Icon(
                    painter = painterResource(
                        id = if (isPasswordVisible) R.drawable.ic_eye_unhide_form
                        else R.drawable.ic_eye_hide_form
                    ),
                    contentDescription = "Toggle Password",
                    tint = UIBlack,
                    modifier = Modifier
                        .padding(end = 18.dp)
                        .size(15.92.dp, 13.44.dp)
                        .clickable { onVisibilityChange() }
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun SignUpScreenPreview() {
    LucaTheme {
        SignUpScreen(onBackClick = {}, onNavigateToHome = {})
    }
}