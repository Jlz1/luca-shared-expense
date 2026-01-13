package com.example.luca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UITransparent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus enableEdgeToEdge() jika menyebabkan masalah atau pastikan sudah di-import
        setContent {
            LucaTheme {
                HomeScreen()
            }
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
//        TemplateScreen()
        FillProfileScreen()
    }
}
