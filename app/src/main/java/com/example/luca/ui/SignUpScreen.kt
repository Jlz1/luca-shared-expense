package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                    Spacer(modifier = Modifier.height(54.dp))

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

                    Spacer(modifier = Modifier.height(57.dp))

                    Button(
                        onClick = onContinueClick,
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
                            text = "Continue",
                            style = AppFont.Medium,
                            fontSize = 14.sp,
                            color = UIBlack,
                            textAlign = TextAlign.Center
                        )
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
                        PasswordVisualTransformation() else VisualTransformation.None
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
        SignUpScreen()
    }
}