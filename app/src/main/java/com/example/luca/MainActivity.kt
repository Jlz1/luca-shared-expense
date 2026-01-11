package com.example.luca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.luca.ui.theme.LucaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus enableEdgeToEdge() jika menyebabkan masalah atau pastikan sudah di-import
        setContent {
            LucaTheme {
                TemplateScreen()
                FillProfileScreen()
                FinalScreen()
                FinalSignUpScreen()
                GreetingScreen()
                LoginScreen()
                SignUpScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplatePreview() {
    LucaTheme {
//        TemplateScreen()
        FillProfileScreen()
    }
}
