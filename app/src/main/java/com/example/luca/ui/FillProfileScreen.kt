package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
fun FillProfileScreen() {
    var username by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 30.dp),
            horizontalAlignment = Alignment.Start
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
                        .clickable { /* TODO: Back Action */ }
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
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(104.dp))

                    // --- TITLE SECTION ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "What’s your name?",
                            style = AppFont.SemiBold,
                            fontSize = 28.sp,
                            color = UIBlack
                        )
                        Text(
                            text = "We want to know you more!",
                            style = AppFont.Medium,
                            fontSize = 14.sp,
                            color = UIBlack.copy(alpha = 0.6f)
                        )
                    }

                    // --- PROFILE PICTURE UPLOAD ---
                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clickable { /* TODO: Pick Image */ }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_circle_profile),
                            contentDescription = "Profile Placeholder",
                            modifier = Modifier.fillMaxSize()
                        )

                        Image(
                            painter = painterResource(id = R.drawable.ic_camera_form),
                            contentDescription = "Upload Icon",
                            modifier = Modifier
                                .size(width = 24.44.dp, height = 20.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Profile Picture",
                        style = AppFont.SemiBold,
                        fontSize = 14.sp,
                        color = UIBlack,
                        textAlign = TextAlign.Center
                    )

                    // --- USERNAME FORM ---
                    Spacer(modifier = Modifier.height(30.dp))

                    ProfileInputForm(
                        text = username,
                        onValueChange = { username = it },
                        placeholder = "Username",
                        iconRes = R.drawable.ic_user_form,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- BUTTON CREATE ACCOUNT ---
                    Spacer(modifier = Modifier.height(29.dp))

                    Button(
                        onClick = { /* TODO: Create Account Action */ },
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
                            text = "Create Account",
                            style = AppFont.Medium,
                            fontSize = 14.sp,
                            color = UIBlack,
                            textAlign = TextAlign.Center
                        )
                    }

                    // --- DISCLAIMER TEXT ---
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "*Don’t worry, you can change them later!",
                        style = AppFont.Medium,
                        fontSize = 12.sp,
                        color = UIBlack.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

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

// --- KOMPONEN INPUT ---
@Composable
fun ProfileInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
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
            Spacer(modifier = Modifier.width(29.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(width = 11.dp, height = 13.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FillProfileScreenPreview() {
    LucaTheme {
        FillProfileScreen()
    }
}