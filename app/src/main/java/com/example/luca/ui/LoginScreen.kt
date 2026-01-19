package com.example.luca.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.data.AuthRepository
import com.example.luca.ui.theme.*
import kotlinx.coroutines.launch

// --- STATEFUL COMPOSABLE (Contains Business Logic) ---
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Handle Login Click
    val handleLoginClick: () -> Unit = {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            scope.launch {
                val success = authRepo.loginManual(email, password)
                isLoading = false
                if (success) {
                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    onNavigateToHome()
                } else {
                    Toast.makeText(context, "Email atau Password Salah", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Isi semua kolom dulu!", Toast.LENGTH_SHORT).show()
        }
    }

    // Pass state and callbacks to UI Content
    LoginScreenContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
        isLoading = isLoading,
        onBackClick = onNavigateBack,
        onLoginClick = handleLoginClick,
        onSignUpClick = onNavigateToSignUp
    )
}

// --- STATELESS COMPOSABLE (Pure UI) ---
@Composable
fun LoginScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(UIWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 30.dp),
        ) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "Welcome Back!",
                        style = AppFont.SemiBold,
                        fontSize = 32.sp,
                        color = UIBlack,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Enter your email and password to log in.",
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    CustomInputForm(
                        text = email,
                        onValueChange = onEmailChange,
                        placeholder = "Email Address",
                        iconRes = R.drawable.ic_email_form,
                        iconSizeWidth = 15.dp,
                        iconSizeHeight = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(13.dp))

                    CustomInputForm(
                        text = password,
                        onValueChange = onPasswordChange,
                        placeholder = "Password",
                        iconRes = R.drawable.ic_password_form,
                        iconSizeWidth = 15.dp,
                        iconSizeHeight = 10.dp,
                        isPasswordField = true,
                        isPasswordVisible = isPasswordVisible,
                        onVisibilityChange = onPasswordVisibilityChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(49.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = UIBlack, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Log in",
                                style = AppFont.Medium,
                                fontSize = 14.sp,
                                color = UIBlack,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    val signUpText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = UIBlack, fontSize = 14.sp, fontFamily = AppFont.Medium.fontFamily)) {
                            append("Don't have an account? ")
                        }
                        pushStringAnnotation(tag = "SIGN_UP", annotation = "navigate_signup")
                        withStyle(style = SpanStyle(color = UIAccentYellow, fontSize = 14.sp, fontFamily = AppFont.Medium.fontFamily)) {
                            append("Sign Up.")
                        }
                        pop()
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ClickableText(
                            text = signUpText,
                            onClick = { offset ->
                                signUpText.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset)
                                    .firstOrNull()?.let {
                                        onSignUpClick()
                                    }
                            }
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

@Composable
fun CustomInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    iconSizeWidth: Dp,
    iconSizeHeight: Dp,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                modifier = Modifier.size(width = iconSizeWidth, height = iconSizeHeight)
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
                    modifier = Modifier.fillMaxWidth()
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
                        .size(width = 15.92.dp, height = 13.44.dp)
                        .clickable { onVisibilityChange() }
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun LoginScreenPreview() {
    LucaTheme {
        LoginScreenContent(
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            isPasswordVisible = false,
            onPasswordVisibilityChange = {},
            isLoading = false,
            onBackClick = {},
            onLoginClick = {},
            onSignUpClick = {}
        )
    }
}