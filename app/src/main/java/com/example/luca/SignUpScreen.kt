package com.example.luca

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun SignUpScreen() {
    // State untuk input user
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
                // Padding Start 33dp sesuai permintaan (semua elemen mulai dari x=33)
                .padding(start = 33.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            // --- 1. BACK ARROW ---
            // Posisi Y=57
            Spacer(modifier = Modifier.height(57.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = UIBlack,
                modifier = Modifier
                    .size(29.dp)
                    .clickable { /* TODO: Back Action */ }
            )

            // --- 2. HEADER (TITLE & LOGO) ---
            // Posisi Y=192
            // Arrow (57) + Height (29) = 86.
            // Target 192. Gap = 106.dp
            Spacer(modifier = Modifier.height(106.dp))

            // Menggunakan Box untuk menempatkan logo secara presisi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Estimasi tinggi area judul
            ) {
                // Title Text (x=33 relative global, x=0 relative local)
                Text(
                    text = "Welcome to Luca!",
                    style = AppFont.SemiBold,
                    fontSize = 28.sp,
                    color = UIBlack,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Logo (x=272 global).
                // Karena parent sudah padding 33, maka posisi x lokal = 272 - 33 = 239.
                Image(
                    painter = painterResource(id = R.drawable.ic_luca_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(start = 239.dp) // Geser manual sesuai koordinat
                        .size(34.5.dp, 34.dp)
                        .align(Alignment.TopStart) // Align top agar sejajar Y=192
                )
            }

            // --- 3. SUBTITLE ---
            // Posisi Y=222
            // Title Y=192. Font 28sp makan tempat visual ~30dp.
            // Jadi jaraknya sangat dekat (nyaris nempel).
            Text(
                text = "Splitting bills made easy.",
                style = AppFont.Medium,
                fontSize = 14.sp,
                color = UIBlack.copy(alpha = 0.6f) // Sedikit pudar opsional
            )

            // --- 4. FORM EMAIL ---
            // Posisi Y=296
            // Subtitle Y=222 + Height(~20) = 242.
            // Target 296. Gap = 54.dp
            Spacer(modifier = Modifier.height(54.dp))

            SignUpInputForm(
                text = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                iconRes = R.drawable.ic_email_form
            )

            // --- 5. FORM PASSWORD ---
            // Posisi Y=361 (Hitungan: Email Y=296 + Tinggi 50 + Gap 15)
            // Sesuai logika urutan karena Confirm ada di 426.
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

            // --- 6. FORM CONFIRM PASSWORD ---
            // Posisi Y=426
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

            // --- 7. BUTTON CONTINUE ---
            // Posisi Y=533
            // Confirm Box Bottom = 426 + 50 = 476.
            // Target 533. Gap = 57.dp
            Spacer(modifier = Modifier.height(57.dp))

            Button(
                onClick = { /* TODO: Continue Action */ },
                modifier = Modifier
                    .size(308.dp, 50.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                // Text "Continue" posisi x=157 global.
                // Lebar tombol 308. Tengahnya 154.
                // Posisi 157 berarti hampir persis di tengah (centered).
                Text(
                    text = "Continue",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIBlack,
                    textAlign = TextAlign.Center
                )
            }

            // --- 8. FOOTER ---
            // Posisi Y=760
            // Button Bottom = 533 + 50 = 583.
            // Target 760. Gap = 177.dp
            Spacer(modifier = Modifier.height(177.dp))

            Text(
                text = "Privacy Policy   ·   Terms of Service",
                style = AppFont.SemiBold,
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f),
                // X=94 global. Padding Global=33.
                // Selisih Padding lokal = 94 - 33 = 61.dp
                modifier = Modifier.padding(start = 61.dp)
            )
        }
    }
}

// --- REUSABLE COMPONENT KHUSUS SIGN UP ---
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
            .width(308.dp)
            .height(50.dp)
            .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Icon Utama (Email/Gembok)
            // Box x=33. Icon x=60. Gap = 27dp.
            Spacer(modifier = Modifier.width(27.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(15.dp, 10.dp) // Dimensi 15x10 sesuai request
            )

            // 2. Input Field
            // Text mulai x=87. Icon end di x=75. Gap = 12dp.
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

            // 3. Icon Mata (Eye) - Khusus Password
            if (isPasswordField) {
                // Icon Mata posisi x=307.
                // Box end di x=341 (33+308).
                // Jarak dari kanan = 341 - 307 - lebar icon(15.92) = ~18dp
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