package com.example.luca.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.ui.viewmodel.ScanViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scanResult by viewModel.scanState.collectAsState()

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // 1. CAMERA LAUNCHER (definisi dulu)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            viewModel.uploadImage(tempPhotoFile!!)
        }
    }

    // 2. PERMISSION LAUNCHER (baru pakai cameraLauncher)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = createImageFile(context)
                tempPhotoFile = file
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permission kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. FUNCTION TO LAUNCH CAMERA
    fun launchCamera() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = UIBlack,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Scan Receipt",
                style = AppFont.Bold,
                fontSize = 20.sp,
                color = UIBlack,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(UIBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                scanResult.contains("Sedang", ignoreCase = true) -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = UIAccentYellow,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Menganalisa Struk...", style = AppFont.Bold, fontSize = 18.sp)
                        Text("Mohon tunggu sebentar", style = AppFont.Regular, color = UIDarkGrey)
                    }
                }

                scanResult != "Menunggu Scan..." -> {
                    Text("Hasil Scan", style = AppFont.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = scanResult,
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { launchCamera() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan Ulang", color = UIBlack)
                        }

                        Button(
                            onClick = { /* TODO: Navigate */ },
                            colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Proses Item", color = UIBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scan_button),
                            contentDescription = "Camera",
                            tint = Color.LightGray,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Foto Struk Belanjaan",
                            style = AppFont.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Pastikan tulisan terlihat jelas\nagar AI bisa membacanya.",
                            style = AppFont.Regular,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = { launchCamera() },
                            colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus_button),
                                contentDescription = null,
                                tint = UIBlack
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Buka Kamera",
                                style = AppFont.Bold,
                                fontSize = 16.sp,
                                color = UIBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function
fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        context.externalCacheDir
    )
}