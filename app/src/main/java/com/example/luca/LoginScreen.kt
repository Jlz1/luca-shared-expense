package com.example.luca

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 33.dp), // Global start padding sesuai x=33
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            // --- 1. BACK ARROW ---
            Spacer(modifier = Modifier.height(57.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = UIBlack,
                modifier = Modifier
                    .size(29.dp)
                    .clickable { /* TODO: Back Action */ }
            )

            // --- 2. TEXT SECTION ---
            Spacer(modifier = Modifier.height(128.dp))
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

            // --- 3. FORMS ---
            // Email (y=323)
            Spacer(modifier = Modifier.height(50.dp))
            CustomInputForm(
                text = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                iconRes = R.drawable.ic_email_form,
                iconSizeWidth = 15.dp,
                iconSizeHeight = 10.dp
            )

            // Password (y=386) - calculated from eye icon
            Spacer(modifier = Modifier.height(13.dp))
            CustomInputForm(
                text = password,
                onValueChange = { password = it },
                placeholder = "Password",
                iconRes = R.drawable.ic_password_form,
                iconSizeWidth = 15.dp,
                iconSizeHeight = 10.dp,
                isPasswordField = true,
                isPasswordVisible = isPasswordVisible,
                onVisibilityChange = { isPasswordVisible = !isPasswordVisible }
            )

            // --- 4. LOG IN BUTTON ---
            // Password Box ends at y = 386 + 50 = 436
            // Target Button y = 485
            // Gap = 49.dp
            Spacer(modifier = Modifier.height(49.dp))

            Button(
                onClick = { /* TODO: Handle Login */ },
                modifier = Modifier
                    .size(width = 308.dp, height = 50.dp), // Dimensi 308x50
                shape = RoundedCornerShape(23.dp), // Radius 23
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Log in",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIBlack,
                    textAlign = TextAlign.Center
                )
            }

            // --- 5. SIGN UP LINK TEXT ---
            // Button ends at y = 485 + 50 = 535
            // Target Text y = 550
            // Gap = 15.dp
            Spacer(modifier = Modifier.height(15.dp))

            // Menggunakan AnnotatedString untuk mewarnai sebagian teks
            val signUpText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = UIBlack, fontSize = 14.sp, fontFamily = AppFont.Medium.fontFamily)) {
                    append("Don’t have an account? ")
                }

                // Bagian "Sign Up." yang bisa diklik
                pushStringAnnotation(tag = "SIGN_UP", annotation = "navigate_signup")
                withStyle(style = SpanStyle(color = UIAccentYellow, fontSize = 14.sp, fontFamily = AppFont.Medium.fontFamily)) {
                    append("Sign Up.")
                }
                pop()
            }

            // ClickableText deprecated di versi baru tapi masih standard digunakan untuk kasus ini
            // Kalau mau pakai Text biasa + Modifier.clickable, satu baris jadi kena click semua
            ClickableText(
                text = signUpText,
                onClick = { offset ->
                    signUpText.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset)
                        .firstOrNull()?.let {
                            // TODO: Navigasi ke SignUpPage di sini
                            println("Navigate to Sign Up")
                        }
                },
                // Menyesuaikan posisi x=83 (33 padding start + 50 spacer visual atau alignment center)
                // Karena kita pakai Column Align Start, text ini ada di kiri.
                // Untuk membuatnya agak ke tengah (x=83), kita bisa bungkus Box atau tambah padding start lokal.
                // x=83 - x=33 (global padding) = 50.dp padding tambahan.
                modifier = Modifier.padding(start = 50.dp)
            )

            // --- 6. FOOTER ---
            // Sign Up text (y=550) + height (~20) = 570
            // Target Footer y = 760
            // Gap = 190.dp
            Spacer(modifier = Modifier.height(190.dp))

            Text(
                text = "Privacy Policy   ·   Terms of Service",
                style = AppFont.SemiBold,
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f),
                // x=94 target. Global padding=33. Selisih=61.dp
                modifier = Modifier.padding(start = 61.dp)
            )
        }
    }
}

@Composable
fun CustomInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    iconSizeWidth: androidx.compose.ui.unit.Dp,
    iconSizeHeight: androidx.compose.ui.unit.Dp,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(308.dp)
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
        LoginScreen()
    }
}