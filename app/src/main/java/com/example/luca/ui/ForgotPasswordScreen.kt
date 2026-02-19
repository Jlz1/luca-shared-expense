package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.util.ValidationUtils
import com.example.luca.viewmodel.ForgotPasswordUiState
import com.example.luca.viewmodel.ForgotPasswordViewModel

/**
 * ForgotPasswordScreen - Screen for users to enter their email
 * to receive a password reset link.
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Collect states from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Email validation on change
    val onEmailChange: (String) -> Unit = { input ->
        val sanitized = ValidationUtils.sanitizeInput(input)
        email = sanitized
        emailError = if (sanitized.isNotEmpty()) ValidationUtils.getEmailError(sanitized) else null
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as ForgotPasswordUiState.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetState()
            }
            is ForgotPasswordUiState.EmailSent -> {
                // Stay on screen to show success message
            }
            else -> {}
        }
    }

    val isFormValid = emailError == null && email.isNotBlank() && ValidationUtils.isEmailValid(email)

    ForgotPasswordScreenContent(
        email = email,
        onEmailChange = onEmailChange,
        emailError = emailError,
        isFormValid = isFormValid,
        isLoading = isLoading,
        isEmailSent = uiState is ForgotPasswordUiState.EmailSent,
        snackbarHostState = snackbarHostState,
        onBackClick = onNavigateBack,
        onSendClick = {
            emailError = ValidationUtils.getEmailError(email)
            if (emailError == null) {
                viewModel.sendPasswordResetEmail(email)
            }
        },
        onBackToLoginClick = {
            viewModel.clearData()
            onNavigateToLogin()
        }
    )
}

@Composable
fun ForgotPasswordScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isFormValid: Boolean,
    isLoading: Boolean,
    isEmailSent: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val debouncedBackClick = debounceBackClick(scope) { onBackClick() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFE57373), // Red for errors
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
                // Back Arrow
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = UIBlack,
                        modifier = Modifier
                            .size(29.dp)
                            .clickable { debouncedBackClick() }
                    )
                }

                // Main Content
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
                        if (isEmailSent) {
                            // Success State - Email Sent
                            EmailSentContent(
                                email = email,
                                onBackToLoginClick = onBackToLoginClick,
                                onResendClick = onSendClick,
                                isLoading = isLoading
                            )
                        } else {
                            // Initial State - Enter Email
                            EnterEmailContent(
                                email = email,
                                onEmailChange = onEmailChange,
                                emailError = emailError,
                                isFormValid = isFormValid,
                                isLoading = isLoading,
                                onSendClick = onSendClick
                            )
                        }
                    }
                }

                // Footer
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
private fun EnterEmailContent(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isFormValid: Boolean,
    isLoading: Boolean,
    onSendClick: () -> Unit
) {
    // Title
    Text(
        text = "Forgot Password?",
        style = AppFont.SemiBold,
        fontSize = 32.sp,
        color = UIBlack,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "No worries! Enter your email address and we'll send you a link to reset your password.",
        style = AppFont.Medium,
        fontSize = 14.sp,
        color = UIBlack.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(50.dp))

    // Email Input
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

    Spacer(modifier = Modifier.height(49.dp))

    // Send Button
    Button(
        onClick = onSendClick,
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
                text = "Send Reset Link",
                style = AppFont.Medium,
                fontSize = 14.sp,
                color = UIBlack,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun EmailSentContent(
    email: String,
    onBackToLoginClick: () -> Unit,
    onResendClick: () -> Unit,
    isLoading: Boolean
) {
    // Success Icon
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_email_form),
            contentDescription = "Email Sent",
            tint = UIAccentYellow,
            modifier = Modifier.size(80.dp)
        )
    }

    Spacer(modifier = Modifier.height(30.dp))

    // Title
    Text(
        text = "Check Your Email",
        style = AppFont.SemiBold,
        fontSize = 32.sp,
        color = UIBlack,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "We've sent a password reset link to:",
        style = AppFont.Medium,
        fontSize = 14.sp,
        color = UIBlack.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = email,
        style = AppFont.SemiBold,
        fontSize = 16.sp,
        color = UIBlack,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Please check your inbox and click the link to reset your password. The link will expire in 1 hour.",
        style = AppFont.Medium,
        fontSize = 14.sp,
        color = UIBlack.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Important: Spam folder warning
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = UIAccentYellow.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ Important!",
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "If you don't see the email in your inbox, please check your SPAM/JUNK folder. Mark it as \"Not Spam\" if found.",
                style = AppFont.Medium,
                fontSize = 13.sp,
                color = UIBlack.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Back to Login Button
    Button(
        onClick = onBackToLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(23.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UIAccentYellow,
            contentColor = UIBlack
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "Back to Login",
            style = AppFont.Medium,
            fontSize = 14.sp,
            color = UIBlack,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(15.dp))

    // Resend Link
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't receive the email? ",
                style = AppFont.Medium,
                fontSize = 14.sp,
                color = UIBlack
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = UIAccentYellow,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Resend",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIAccentYellow,
                    modifier = Modifier.clickable { onResendClick() }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun ForgotPasswordScreenPreview() {
    LucaTheme {
        ForgotPasswordScreenContent(
            email = "",
            onEmailChange = {},
            emailError = null,
            isFormValid = false,
            isLoading = false,
            isEmailSent = false,
            onBackClick = {},
            onSendClick = {},
            onBackToLoginClick = {}
        )
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun ForgotPasswordEmailSentPreview() {
    LucaTheme {
        ForgotPasswordScreenContent(
            email = "user@example.com",
            onEmailChange = {},
            emailError = null,
            isFormValid = true,
            isLoading = false,
            isEmailSent = true,
            onBackClick = {},
            onSendClick = {},
            onBackToLoginClick = {}
        )
    }
}
