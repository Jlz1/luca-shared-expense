package com.example.luca.ui

import android.Manifest
import android.content.Context
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
    onBackClick: () -> Unit,
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current
    val scanResult by viewModel.scanState.collectAsState()
    val parsedReceiptData by viewModel.parsedReceiptData.collectAsState()

    var showResultScreen by remember { mutableStateOf(false) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var cameraLaunched by remember { mutableStateOf(false) }

    // Show ScanResultScreen when parsed data is available
    if (showResultScreen && parsedReceiptData != null) {
        ScanResultScreen(
            parsedData = parsedReceiptData!!,
            onBackClick = {
                showResultScreen = false
                viewModel.resetScan()
            },
            onScanAgain = {
                showResultScreen = false
                viewModel.resetScan()
                cameraLaunched = false
            },
            onContinue = {
                showResultScreen = false
                viewModel.resetScan()
                onContinue()
            }
        )
        return
    }

    // 1. CAMERA LAUNCHER (definisi dulu)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            viewModel.uploadImage(tempPhotoFile!!)
            showResultScreen = true
        } else {
            // User cancelled the camera, go back
            onBackClick()
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
                onBackClick()
            }
        } else {
            Toast.makeText(context, "Permission kamera ditolak", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    // 3. FUNCTION TO LAUNCH CAMERA
    fun launchCamera() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 4. LAUNCH CAMERA AUTOMATICALLY ON SCREEN LOAD
    LaunchedEffect(Unit) {
        if (!cameraLaunched) {
            cameraLaunched = true
            launchCamera()
        }
    }

    // Show loading state immediately while camera is being launched/used
    if (cameraLaunched && scanResult == "Menunggu Scan...") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UIAccentYellow)
                .statusBarsPadding()
        ) {
            // Header - menggunakan HeaderSection dari MainActivity
            HeaderSection(
                currentState = HeaderState.DETAILS,
                onLeftIconClick = onBackClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(UIBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = UIAccentYellow,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Siap memotret struk...", style = AppFont.Bold, fontSize = 18.sp)
                Text("Buka kamera sekarang", style = AppFont.Regular, color = UIDarkGrey)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // Header - menggunakan HeaderSection dari MainActivity
        HeaderSection(
            currentState = HeaderState.DETAILS,
            onLeftIconClick = onBackClick
        )

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
                            onClick = {
                                if (parsedReceiptData != null) {
                                    showResultScreen = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = parsedReceiptData != null
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