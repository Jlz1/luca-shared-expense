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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.data.AuthRepository
import com.example.luca.ui.theme.*
import com.example.luca.util.ValidationUtils
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }
    var isLoading by remember { mutableStateOf(false) }

    // State Form
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Error states for validation
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Validate on input change with sanitization
    val onEmailChangeWithValidation: (String) -> Unit = { input ->
        val sanitized = ValidationUtils.sanitizeInput(input)
        email = sanitized
        emailError = if (sanitized.isNotEmpty()) ValidationUtils.getEmailError(sanitized) else null
    }

    val onPasswordChangeWithValidation: (String) -> Unit = { input ->
        password = input
        passwordError = if (input.isNotEmpty()) ValidationUtils.getPasswordError(input) else null
        if (confirmPassword.isNotEmpty()) {
            confirmPasswordError = if (confirmPassword != input) "Password tidak cocok" else null
        }
    }

    val onConfirmPasswordChangeWithValidation: (String) -> Unit = { input ->
        confirmPassword = input
        confirmPasswordError = when {
            input.isEmpty() -> null
            input != password -> "Password tidak cocok"
            else -> null
        }
    }

    // Check if form is valid for enabling button
    val isFormValid = emailError == null && passwordError == null && confirmPasswordError == null &&
            email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() &&
            ValidationUtils.isEmailValid(email) && ValidationUtils.isPasswordValid(password) &&
            password == confirmPassword

    val handleSignUp: () -> Unit = {
        emailError = ValidationUtils.getEmailError(email)
        passwordError = ValidationUtils.getPasswordError(password)
        confirmPasswordError = if (confirmPassword != password) "Password tidak cocok" else null

        if (emailError == null && passwordError == null && confirmPasswordError == null) {
            isLoading = true
            scope.launch {
                val success = authRepo.signUpManual(email, password)
                isLoading = false

                if (success) {
                    Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    onContinueClick()
                } else {
                    Toast.makeText(context, "Gagal daftar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(UIWhite)) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = UIAccentYellow
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(30.dp)) {
            // Header Back
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = UIBlack,
                    modifier = Modifier.size(29.dp).clickable { onBackClick() }
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
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
                            modifier = Modifier.align(Alignment.CenterEnd).size(34.5.dp, 34.dp)
                        )
                    }

                    Text(
                        text = "Splitting bills made easy.",
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    SignUpInputForm(
                        text = email,
                        onValueChange = onEmailChangeWithValidation,
                        placeholder = "Email Address",
                        iconRes = R.drawable.ic_email_form,
                        errorMessage = emailError
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    SignUpInputForm(
                        text = password,
                        onValueChange = onPasswordChangeWithValidation,
                        placeholder = "Password",
                        iconRes = R.drawable.ic_password_form,
                        isPasswordField = true,
                        isPasswordVisible = isPasswordVisible,
                        onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                        errorMessage = passwordError
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    SignUpInputForm(
                        text = confirmPassword,
                        onValueChange = onConfirmPasswordChangeWithValidation,
                        placeholder = "Confirm Password",
                        iconRes = R.drawable.ic_password_form,
                        isPasswordField = true,
                        isPasswordVisible = isConfirmPasswordVisible,
                        onVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        errorMessage = confirmPasswordError
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = handleSignUp,
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
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
                                color = UIBlack,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Continue",
                                style = AppFont.Medium,
                                fontSize = 14.sp,
                                color = UIBlack,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Footer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
fun SignUpInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {},
    errorMessage: String? = null
) {
    val focusRequester = remember { FocusRequester() }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = if (errorMessage != null) Color.Red.copy(alpha = 0.08f) else UIGrey,
                    shape = RoundedCornerShape(23.dp)
                )
                .clickable { focusRequester.requestFocus() },
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
                    modifier = Modifier.size(15.dp, 10.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
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
                            .fillMaxSize()
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                                innerTextField()
                            }
                        }
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