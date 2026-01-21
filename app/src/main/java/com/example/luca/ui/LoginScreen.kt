package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.luca.ui.theme.*
import com.example.luca.util.ValidationUtils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.viewmodel.AuthViewModel

// --- STATEFUL COMPOSABLE (Contains Business Logic) ---
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Error states for validation
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Validate on input change with sanitization
    val onEmailChangeWithValidation: (String) -> Unit = { input ->
        val sanitized = ValidationUtils.sanitizeInput(input)
        email = sanitized
        emailError = if (sanitized.isNotEmpty()) ValidationUtils.getEmailError(sanitized) else null
    }

    val onPasswordChangeWithValidation: (String) -> Unit = { input ->
        password = input
        passwordError = if (input.isNotEmpty()) ValidationUtils.getPasswordError(input) else null
    }

    // Check if form is valid for enabling button
    val isFormValid = emailError == null && passwordError == null &&
                      email.isNotBlank() && password.isNotBlank() &&
                      ValidationUtils.isLoginFormValid(email, password)

    // Observe ViewModel states
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            viewModel.resetState()
            onNavigateToHome()
        }
    }

    // Show error notification
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.resetState()
        }
    }

    // Pass state and callbacks to UI Content
    LoginScreenContent(
        email = email,
        onEmailChange = onEmailChangeWithValidation,
        password = password,
        onPasswordChange = onPasswordChangeWithValidation,
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
        isFormValid = isFormValid,
        emailError = emailError,
        passwordError = passwordError,
        isLoading = viewModel.isLoading,
        snackbarHostState = snackbarHostState,
        onBackClick = onNavigateBack,
        onLoginClick = {
            // Final validation before login
            emailError = ValidationUtils.getEmailError(email)
            passwordError = ValidationUtils.getPasswordError(password)

            if (emailError == null && passwordError == null) {
                viewModel.login(email, password)
            }
        },
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
    isFormValid: Boolean,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (data.visuals.message.contains("does not exist", ignoreCase = true)) {
                        Color(0xFFE57373) // Red for account not exists
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIWhite)
                .padding(paddingValues)
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
                            errorMessage = emailError,
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
                            errorMessage = passwordError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(49.dp))

                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = isFormValid && !isLoading,
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UIAccentYellow,
                                contentColor = UIBlack,
                                disabledContainerColor = UIAccentYellow.copy(alpha = 0.5f),
                                disabledContentColor = UIBlack.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = UIBlack,
                                    strokeWidth = 2.dp
                                )
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
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = if (errorMessage != null) UIGrey.copy(alpha = 0.9f) else UIGrey,
                    shape = RoundedCornerShape(23.dp)
                )
                .clickable { focusRequester.requestFocus() }
                .then(
                    if (errorMessage != null) {
                        Modifier.background(
                            color = Color.Red.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(23.dp)
                        )
                    } else Modifier
                ),
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
                    tint = if (errorMessage != null) Color.Red.copy(alpha = 0.7f) else UIBlack,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
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

        // Error message display
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = AppFont.Medium,
                fontSize = 12.sp,
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
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
            isFormValid = false,
            emailError = null,
            passwordError = null,
            isLoading = false,
            onBackClick = {},
            onLoginClick = {},
            onSignUpClick = {}
        )
    }
}