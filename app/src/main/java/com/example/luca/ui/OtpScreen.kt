package com.example.luca.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noir.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.AuthViewModel

@Composable
fun OtpScreen(
    emailTujuan: String,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }

    // Logic saat OTP sukses/gagal
    LaunchedEffect(authViewModel.otpVerificationStatus) {
        if (authViewModel.otpVerificationStatus == true) {
            Toast.makeText(context, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show()
            onVerificationSuccess()
            authViewModel.resetOtpStatus()
        } else if (authViewModel.otpVerificationStatus == false) {
            Toast.makeText(context, "Kode Salah! Coba lagi.", Toast.LENGTH_SHORT).show()
            authViewModel.resetOtpStatus()
        }
    }

    Scaffold(containerColor = UIWhite) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Back
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = UIBlack,
                    modifier = Modifier.size(29.dp).clickable { onBackClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("Verification Code", style = AppFont.SemiBold, fontSize = 24.sp, color = UIBlack)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Kami mengirimkan kode ke email:", style = AppFont.Regular, fontSize = 14.sp, color = UIBlack.copy(alpha = 0.6f))
            Text(emailTujuan, style = AppFont.Medium, fontSize = 14.sp, color = UIBlack)

            Spacer(modifier = Modifier.height(50.dp))

            // --- KOTAK INPUT ANGKA ---
            BasicTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) otpInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        repeat(4) { index ->
                            val char = if (index < otpInput.length) otpInput[index].toString() else ""
                            val isActive = index == otpInput.length

                            Box(
                                modifier = Modifier
                                    .width(60.dp).height(60.dp).padding(4.dp)
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) UIAccentYellow else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(char, style = AppFont.Bold, fontSize = 24.sp, color = UIBlack)
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Tombol Verifikasi (Style sama kayak Continue Button kamu)
            Button(
                onClick = { authViewModel.verifyOtp(otpInput) },
                enabled = otpInput.length == 4 && !authViewModel.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack,
                    disabledContainerColor = UIAccentYellow.copy(alpha = 0.5f)
                )
            ) {
                if (authViewModel.isLoading) {
                    CircularProgressIndicator(color = UIBlack, modifier = Modifier.size(20.dp))
                } else {
                    Text("Verify", style = AppFont.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}