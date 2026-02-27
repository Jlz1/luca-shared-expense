package com.luca.shared.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.luca.shared.ui.theme.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBugScreen(
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    // State UI
    var subjectText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // State Dialog & Loading
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Report a Bug",
                        style = AppFont.Bold,
                        fontSize = 20.sp,
                        color = UIBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UIBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UIWhite,
                    scrolledContainerColor = UIWhite
                )
            )
        },
        containerColor = UIBackground,
        bottomBar = {
            // Sticky Bottom Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UIBackground)
                    .padding(20.dp)
            ) {
                Button(
                    // Disable button saat sedang loading agar tidak spam klik
                    enabled = !isLoading,
                    onClick = {
                        // 1. Validasi Input
                        if (subjectText.trim().isNotEmpty() && descriptionText.trim().isNotEmpty()) {

                            // Mulai Loading
                            isLoading = true

                            // Instance Firebase
                            val db = FirebaseFirestore.getInstance()
                            val storage = FirebaseStorage.getInstance()

                            // Data Dasar
                            val reportData = hashMapOf(
                                "subject" to subjectText,
                                "description" to descriptionText,
                                "deviceModel" to android.os.Build.MODEL,
                                "androidVersion" to android.os.Build.VERSION.RELEASE,
                                "timestamp" to Timestamp.now(),
                                "imageUrls" to mutableListOf<String>()
                            )

                            // Helper Function: Simpan ke Firestore
                            fun saveToFirestore(data: HashMap<String, Any>) {
                                db.collection("bug_reports")
                                    .add(data)
                                    .addOnSuccessListener {
                                        isLoading = false // Stop loading
                                        showSuccessDialog = true
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false // Stop loading (Error)
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }

                            // 2. Logic Upload Gambar vs Tanpa Gambar
                            if (selectedImageUris.isNotEmpty()) {
                                // Upload gambar pertama
                                val imageUri = selectedImageUris.first()
                                val filename = "bugs/${UUID.randomUUID()}.jpg"
                                val ref = storage.reference.child(filename)

                                ref.putFile(imageUri)
                                    .addOnSuccessListener {
                                        // Upload Sukses -> Ambil URL
                                        ref.downloadUrl.addOnSuccessListener { uri ->
                                            reportData["imageUrls"] = listOf(uri.toString())
                                            saveToFirestore(reportData)
                                        }.addOnFailureListener {
                                            // Gagal ambil URL
                                            isLoading = false
                                            Toast.makeText(context, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        // Upload Gagal
                                        isLoading = false
                                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Tidak ada gambar, langsung simpan teks
                                saveToFirestore(reportData)
                            }

                        } else {
                            Toast.makeText(context, "Please fill in the Title and Description", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UIAccentYellow,
                        contentColor = UIBlack,
                        disabledContainerColor = UIAccentYellow.copy(alpha = 0.7f),
                        disabledContentColor = UIBlack.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (isLoading) {
                        // Tampilkan Loading Spinner
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = UIBlack,
                            strokeWidth = 3.dp
                        )
                    } else {
                        // Teks Normal
                        Text(
                            text = "Submit Report",
                            style = AppFont.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            // ===== 1. HEADER ILUSTRASI =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(UIAccentYellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = UIBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Found an issue?",
                    style = AppFont.Bold,
                    fontSize = 18.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please describe the bug you encountered. Your feedback helps us make Luca better.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ===== 2. FORM SECTION =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                // Input: Subject
                Text(
                    text = "Subject",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                LucaTextField(
                    value = subjectText,
                    onValueChange = { subjectText = it },
                    placeholder = "e.g., App crashes on split screen"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "*Required",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input: Description
                Text(
                    text = "Description",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                LucaTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = "Tell us what happened, steps to reproduce, etc.",
                    singleLine = false,
                    minLines = 5
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "*Required",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )
            }

            // ===== 3. ATTACHMENT SECTION =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Screenshots (Optional)",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload images to help us understand the issue.",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Upload
                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(UIGrey.copy(alpha = 0.3f))
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                }
                                .border(1.dp, UIDarkGrey.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = UIDarkGrey
                                )
                                Text(
                                    text = "Add",
                                    style = AppFont.Regular,
                                    fontSize = 10.sp,
                                    color = UIDarkGrey
                                )
                            }
                        }
                    }

                    // Display selected images
                    items(selectedImageUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(UIGrey)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Delete button
                            IconButton(
                                onClick = {
                                    selectedImageUris = selectedImageUris.filterNot { it == uri }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp)
                                    .background(UIBlack.copy(alpha = 0.7f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = UIWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedImageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${selectedImageUris.size} image(s) selected",
                        style = AppFont.Regular,
                        fontSize = 12.sp,
                        color = UIDarkGrey
                    )
                }
            }

            // Spacer untuk memberi ruang scroll bottom bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ===== SUCCESS DIALOG =====
    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(UIWhite, RoundedCornerShape(20.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(UIAccentYellow.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = UIAccentYellow,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Report received!",
                        style = AppFont.Bold,
                        fontSize = 18.sp,
                        color = UIBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thank you for reporting the bug. Our team will review your report immediately.",
                        style = AppFont.Regular,
                        fontSize = 14.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onSubmitSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        )
                    ) {
                        Text(
                            text = "Back To Home",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// Helper Text Field (Sama seperti sebelumnya)
@Composable
fun LucaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = AppFont.Regular,
                color = UIDarkGrey.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = AppFont.Regular.fontFamily,
            fontSize = 14.sp,
            color = UIBlack
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UIAccentYellow,
            unfocusedBorderColor = UIGrey,
            focusedContainerColor = UIWhite,
            unfocusedContainerColor = UIWhite,
            cursorColor = UIBlack
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        singleLine = singleLine,
        minLines = minLines
    )
}