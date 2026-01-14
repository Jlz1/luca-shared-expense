package com.example.luca.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.luca.ui.theme.LucaTheme
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus enableEdgeToEdge() jika menyebabkan masalah atau pastikan sudah di-import
        setContent {
            GreetingScreen(
                onNavigateToLogin = {
                    // Logika pindah ke LoginScreen manual
                    Toast.makeText(this, "Pindah ke Manual Login", Toast.LENGTH_SHORT).show()
                },
                onNavigateToHome = {
                    // Logika kalau sukses login
                    Toast.makeText(this, "Masuk ke Home...", Toast.LENGTH_SHORT).show()
                }
            )
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                0x00000000, // Warna background scrim (transparan aja)
                0x00000000
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TemplatePreview() {
    LucaTheme {
        DetailedEventScreen()
    }
}
