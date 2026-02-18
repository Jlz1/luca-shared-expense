package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.model.Activity
import com.example.luca.model.Contact
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite
import com.example.luca.viewmodel.AddEventViewModel
import java.util.Locale
import com.example.luca.data.LucaFirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.luca.ui.viewmodel.ScanViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen2(
    viewModel: AddEventViewModel = viewModel(),
    eventId: String = "",
    activityId: String = "",
    activity: Activity? = null,
    onBackClick: () -> Unit = {},
    onSaveSuccess: (eventId: String) -> Unit = {}
) {
    // --- STATE: Agar Switch Equal Split bisa Nyala/Mati ---
    var isSplitEqual by remember { mutableStateOf(false) }

    // --- STATE: Event Members from Activity (INCLUDE paidBy) ---
    // Use state that can be populated from repo when activity is null
    var eventMembers by remember { mutableStateOf<List<Contact>>(emptyList()) }

    // --- STATE: Edit Participants Dialog ---
    var showEditParticipantsDialog by remember { mutableStateOf(false) }

    // Guard untuk mencegah klik back berkali-kali yang menyebabkan bug navigation
    val backClicked = remember { mutableStateOf(false) }
    val handleBackClick: () -> Unit = {
        if (!backClicked.value) {
            backClicked.value = true
            onBackClick()
        }
    }

    // Initialize from provided activity if available (INCLUDE paidBy)
    LaunchedEffect(activity) {
        if (activity != null) {
            val baseMembers = activity.participants.map { pd -> Contact(name = pd.name, avatarName = pd.avatarName) }
            val payerContact = activity.paidBy?.let { Contact(name = it.name, avatarName = it.avatarName) }
            eventMembers = if (payerContact != null) {
                // Avoid duplicates by name
                val exists = baseMembers.any { it.name == payerContact.name }
                if (exists) baseMembers else baseMembers + payerContact
            } else baseMembers
        }
    }

    // If activity is null, fetch from repository using eventId & activityId (INCLUDE paidBy)
    LaunchedEffect(eventId, activityId) {
        if (activity == null && eventId.isNotEmpty() && activityId.isNotEmpty()) {
            try {
                val repo = LucaFirebaseRepository()
                val loaded = withContext(Dispatchers.IO) { repo.getActivityById(eventId, activityId) }
                loaded?.let { act ->
                    val baseMembers = act.participants.map { pd -> Contact(name = pd.name, avatarName = pd.avatarName) }
                    val payerContact = act.paidBy?.let { Contact(name = it.name, avatarName = it.avatarName) }
                    eventMembers = if (payerContact != null) {
                        val exists = baseMembers.any { it.name == payerContact.name }
                        if (exists) baseMembers else baseMembers + payerContact
                    } else baseMembers
                }
            } catch (e: Exception) {
                android.util.Log.e("NewActivityScreen2", "Failed to load activity participants: ${e.message}")
            }
        }
    }

    // --- STATE: Receipt Items ---
    var receiptItems by remember {
        mutableStateOf(listOf<ReceiptItem>())
    }

    // --- STATE: Backup of items before Equal Split (to restore when toggled OFF) ---
    var itemsBackupBeforeEqualSplit by remember {
        mutableStateOf<List<ReceiptItem>?>(null)
    }

    // --- STATE: Tax and Discount yang diperbaiki ---
    var globalTaxPercentage by remember { mutableStateOf(0.0) }
    var globalDiscountAmount by remember { mutableStateOf(0.0) }

    // Load items for this Activity when the screen opens
    LaunchedEffect(eventId, activityId) {
        if (eventId.isNotEmpty() && activityId.isNotEmpty()) {
            android.util.Log.d("NewActivityScreen2", "🔄 Loading items for EventID: $eventId, ActivityID: $activityId")
            val repo = LucaFirebaseRepository()

            // Load Activity document to get tax and discount
            val activityData = withContext(Dispatchers.IO) {
                repo.getActivityData(eventId, activityId)
            }

            // Restore tax and discount from Activity document
            if (activityData != null) {
                val taxFromActivity = activityData["taxPercentage"]
                val discFromActivity = activityData["discountAmount"]

                android.util.Log.d("NewActivityScreen2", "🔍 Activity data - Tax: $taxFromActivity, Discount: $discFromActivity")

                globalTaxPercentage = when (taxFromActivity) {
                    is Double -> taxFromActivity
                    is Int -> taxFromActivity.toDouble()
                    is Long -> taxFromActivity.toDouble()
                    is String -> taxFromActivity.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                globalDiscountAmount = when (discFromActivity) {
                    is Double -> discFromActivity
                    is Int -> discFromActivity.toDouble()
                    is Long -> discFromActivity.toDouble()
                    is String -> discFromActivity.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                android.util.Log.d("NewActivityScreen2", "✅ Restored from Activity doc - tax: $globalTaxPercentage%, discount: $globalDiscountAmount")
            }

            // Load items
            val items = withContext(Dispatchers.IO) {
                repo.getActivityItems(eventId, activityId)
            }

            android.util.Log.d("NewActivityScreen2", "📥 Loaded ${items.size} items from Firestore")

            // Fallback: if Activity document doesn't have tax/discount, try to get from first item
            if (globalTaxPercentage == 0.0 && globalDiscountAmount == 0.0 && items.isNotEmpty()) {
                val firstItem = items.first()
                android.util.Log.d("NewActivityScreen2", "🔍 Fallback: Reading from first item")

                val taxValue = firstItem["taxPercentage"]
                val discValue = firstItem["discountAmount"]

                globalTaxPercentage = when (taxValue) {
                    is Double -> taxValue
                    is Int -> taxValue.toDouble()
                    is Long -> taxValue.toDouble()
                    is String -> taxValue.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                globalDiscountAmount = when (discValue) {
                    is Double -> discValue
                    is Int -> discValue.toDouble()
                    is Long -> discValue.toDouble()
                    is String -> discValue.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                android.util.Log.d("NewActivityScreen2", "✅ Restored from item - tax: $globalTaxPercentage%, discount: $globalDiscountAmount")
            }

            // Map Firestore item documents to UI ReceiptItem
            receiptItems = items.map { data ->
                val name = (data["itemName"] as? String) ?: ""
                val price = when (val p = data["price"]) {
                    is Long -> p
                    is Int -> p.toLong()
                    is String -> p.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val qty = when (val q = data["quantity"]) {
                    is Int -> q
                    is Long -> q.toInt()
                    is String -> q.toIntOrNull() ?: 1
                    else -> 1
                }
                val memberNames = (data["memberNames"] as? List<*>)?.map { it.toString() } ?: emptyList()

                // Load itemTax and itemDiscount
                val itemTax = when (val tax = data["itemTax"]) {
                    is Double -> tax
                    is Int -> tax.toDouble()
                    is Long -> tax.toDouble()
                    is String -> tax.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val itemDiscount = when (val disc = data["itemDiscount"]) {
                    is Double -> disc
                    is Int -> disc.toDouble()
                    is Long -> disc.toDouble()
                    is String -> disc.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                ReceiptItem(
                    quantity = qty,
                    itemName = name,
                    price = price,
                    members = emptyList(),
                    memberNames = memberNames,
                    itemTax = itemTax,
                    itemDiscount = itemDiscount
                )
            }
            android.util.Log.d("NewActivityScreen2", "✅ Mapped ${receiptItems.size} ReceiptItems for UI")
        }
    }


    // --- STATE: Dialog ---
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf(-1) }

    // --- STATE: Scan Camera Integration ---
    val scanViewModel: ScanViewModel = viewModel()
    val context = LocalContext.current
    val parsedReceiptData by scanViewModel.parsedReceiptData.collectAsState()
    val scanState by scanViewModel.scanState.collectAsState()

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var showScanLoading by remember { mutableStateOf(false) }

    // Monitor save success state
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    // Log every time isSuccess changes
    LaunchedEffect(isSuccess) {
        android.util.Log.d("NewActivityScreen2", "🔄 LaunchedEffect triggered! isSuccess=$isSuccess, eventId=$eventId")
        if (isSuccess) {
            if (eventId.isNotEmpty()) {
                android.util.Log.d("NewActivityScreen2", "✅✅✅ SUCCESS! Calling onSaveSuccess with eventId=$eventId")
                onSaveSuccess(eventId)
                android.util.Log.d("NewActivityScreen2", "✅ onSaveSuccess called, resetting success state...")
                // Reset the success state so it can trigger again next time
                viewModel.resetSuccessState()
            } else {
                android.util.Log.e("NewActivityScreen2", "❌ ERROR: isSuccess=true but eventId is EMPTY!")
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            scanViewModel.uploadImage(tempPhotoFile!!)
            showScanLoading = true
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = createImageFileForCamera(context)
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

    // Function to launch camera
    fun launchCamera() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Monitor scan result and add items when scan completes
    LaunchedEffect(parsedReceiptData) {
        parsedReceiptData?.let { data ->
            if (showScanLoading) {
                showScanLoading = false

                // Add scanned items to receipt items
                val scannedItems = data.items.map { item ->
                    ReceiptItem(
                        quantity = item.itemQuantity,
                        itemName = item.itemName,
                        price = item.itemPrice.toLong(),
                        members = emptyList(),
                        // Jika equal split aktif, assign semua members
                        memberNames = if (isSplitEqual && eventMembers.isNotEmpty()) {
                            eventMembers.map { it.name }
                        } else {
                            emptyList() 
                        }
                    )
                }
                receiptItems = receiptItems + scannedItems
                globalTaxPercentage = data.tax
                globalDiscountAmount = data.discount

                // Show success message
                Toast.makeText(
                    context,
                    "✅ Receipt scanned! ${scannedItems.size} items added",
                    Toast.LENGTH_SHORT
                ).show()

                // Reset scan state
                scanViewModel.resetScan()
            }
        }
    }

    // Monitor scan state for errors
    LaunchedEffect(scanState) {
        if (scanState.startsWith("❌") && showScanLoading) {
            showScanLoading = false
            Toast.makeText(context, scanState, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {

        // 2. HEADER
        HeaderSection(
            currentState = HeaderState.EDIT_ACTIVITY,
            onLeftIconClick = handleBackClick
        )

        // 3. KONTEN AREA (PUTIH & ROUNDED)
        Box(
            modifier = Modifier
                .weight(1f) // Mengisi sisa ruang ke bawah
                .fillMaxWidth()
                .background(UIBackground)
                .imePadding() // Prevent buttons from being pushed up by keyboard
        ) {

            // A. SCROLLABLE CONTENT (FORM)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Scrollable main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Participants and Split toggle section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Participants list - SCROLLABLE HORIZONTAL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(UIWhite)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp), // Reduced from 12.dp to 6.dp
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (eventMembers.isNotEmpty()) {
                                    items(eventMembers) { member ->
                                        ParticipantAvatarItemSmall(member)
                                    }
                                    // Add "Edit" button at the end
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp) // Same size as avatar
                                                .clip(CircleShape)
                                                .background(UIAccentYellow.copy(alpha = 0.2f))
                                                .clickable { showEditParticipantsDialog = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Participants",
                                                tint = UIAccentYellow,
                                                modifier = Modifier.size(16.dp) // Proportional icon size
                                            )
                                        }
                                    }
                                } else {
                                    // Fallback display jika tidak ada participants
                                    item { ParticipantAvatarItemSmall(Contact(name = "You", avatarName = "avatar_1")) }
                                    item { ParticipantAvatarItemSmall(Contact(name = "Jeremy E", avatarName = "avatar_2")) }
                                    item { ParticipantAvatarItemSmall(Contact(name = "Abel M", avatarName = "avatar_3")) }
                                }
                            }
                        }

                        // Equal Split toggle
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(UIWhite),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Equal Split",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = UIBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // --- SWITCH YANG SUDAH DIPERBAIKI ---
                            Switch(
                                checked = isSplitEqual, // Menggunakan variable state
                                onCheckedChange = { isChecked ->
                                    isSplitEqual = isChecked

                                    if (isChecked) {
                                        // === EQUAL SPLIT ON ===
                                        // Backup current items state before applying equal split
                                        if (receiptItems.isNotEmpty()) {
                                            itemsBackupBeforeEqualSplit = receiptItems
                                        }

                                        // Apply equal split: assign all participants to all items
                                        if (eventMembers.isNotEmpty()) {
                                            receiptItems = receiptItems.map { item ->
                                                item.copy(
                                                    memberNames = eventMembers.map { it.name }
                                                )
                                            }
                                        }
                                    } else {
                                        // === EQUAL SPLIT OFF ===
                                        // Restore from backup if available
                                        if (itemsBackupBeforeEqualSplit != null) {
                                            receiptItems = itemsBackupBeforeEqualSplit!!
                                            itemsBackupBeforeEqualSplit = null // Clear backup after restore
                                        }
                                        // If no backup, clear all member assignments (reset to empty)
                                        else {
                                            receiptItems = receiptItems.map { item ->
                                                item.copy(memberNames = emptyList())
                                            }
                                        }
                                    }
                                }, // Update state saat diklik
                                modifier = Modifier
                                    .scale(1.2f)
                                    .height(30.dp),
                                colors = SwitchDefaults.colors(
                                    uncheckedThumbColor = UIWhite,
                                    uncheckedTrackColor = UIGrey,
                                    checkedTrackColor = UIAccentYellow, // Warna saat ON
                                    uncheckedBorderColor = Color.Transparent,
                                    checkedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receipt Card section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bg_receipt_wave),
                            contentDescription = "Receipt Wave",
                            modifier = Modifier.fillMaxWidth().height(30.dp),
                            alignment = Alignment.BottomCenter
                        )
                        
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp).background(UIWhite))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(UIWhite)
                                .padding(horizontal = 20.dp)
                        ) {
                            // Receipt Header
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = activity?.title ?: "New Activity",
                                    style = AppFont.SemiBold,
                                    fontSize = 16.sp,
                                    color = UIBlack
                                )
                                Text(
                                    text = "Paid by ${activity?.payerName ?: "Unknown"}",
                                    style = AppFont.Regular,
                                    fontSize = 12.sp,
                                    color = UIDarkGrey
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Instruction hint for editing items
                            if (receiptItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap any item to edit",
                                        style = AppFont.Regular,
                                        fontSize = 12.sp,
                                        color = UIDarkGrey
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = UIDarkGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Receipt Items (Dynamic) - Unified scrollable container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(UIWhite)
                                    .padding(16.dp)
                            ) {
                                if (receiptItems.isEmpty()) {
                                    // Show loading animation when processing scan, otherwise show empty state
                                    if (showScanLoading) {
                                        // Loading state with animation
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(48.dp),
                                                color = UIAccentYellow,
                                                strokeWidth = 4.dp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Processing receipt...",
                                                style = AppFont.SemiBold,
                                                fontSize = 16.sp,
                                                color = UIBlack
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Please wait while we analyze your receipt",
                                                style = AppFont.Regular,
                                                fontSize = 12.sp,
                                                color = UIDarkGrey.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        // Empty state with helpful instructions
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                tint = UIDarkGrey.copy(alpha = 0.5f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No items yet",
                                                style = AppFont.SemiBold,
                                                fontSize = 16.sp,
                                                color = UIDarkGrey
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tap 'Add Item' below to start adding receipt items",
                                                style = AppFont.Regular,
                                                fontSize = 12.sp,
                                                color = UIDarkGrey.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    // Scrollable receipt items in unified container
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp), // Fixed height for scrollable area
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(receiptItems.size) { index ->
                                            val item = receiptItems[index]
                                            ReceiptItemRow(
                                                item = item,
                                                eventMembers = eventMembers,
                                                onItemClick = {
                                                    editingItemIndex = index
                                                    showEditItemDialog = true
                                                }
                                            )
                                        }
                                    }

                                    // Scrolling indicator when there are multiple items
                                    if (receiptItems.size > 3) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Scroll to see all items",
                                                style = AppFont.Regular,
                                                fontSize = 10.sp,
                                                color = UIDarkGrey.copy(alpha = 0.6f),
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }

                                // Add Item Button integrated within the container
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            UIAccentYellow.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(UIAccentYellow.copy(alpha = 0.1f))
                                        .clickable { showAddItemDialog = true }
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Item",
                                        tint = UIBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Add Item",
                                        color = UIBlack,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 2.dp, color = UIGrey)
                            Spacer(modifier = Modifier.fillMaxWidth().height(10.dp).background(UIWhite))
                        }

                        Image(
                            painter = painterResource(R.drawable.bg_receipt_wave),
                            contentDescription = "Receipt Wave",
                            modifier = Modifier.fillMaxWidth().height(30.dp).rotate(180f),
                            alignment = Alignment.BottomCenter
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Total Bill section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(UIWhite)
                            .padding(16.dp)
                    ) {
                        // Calculate subtotal with qty * price
                        val subtotal = receiptItems.sumOf { (it.price.toDouble() * it.quantity) }
                        val totalItemTax = receiptItems.sumOf { it.itemTax }
                        val totalItemDiscount = receiptItems.sumOf { it.itemDiscount }
                        // Total Bill hanya dari sum per-item tax dan discount
                        val totalBill = subtotal + totalItemTax - totalItemDiscount

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                            Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", subtotal)}", fontSize = 12.sp, color = UIDarkGrey)
                        }
                        // Tampilkan Tax hanya jika ada
                        if (totalItemTax > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tax (Total)", fontSize = 12.sp, color = UIDarkGrey)
                                Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalItemTax)}", fontSize = 12.sp, color = UIDarkGrey)
                            }
                        }
                        // Tampilkan Discount hanya jika ada
                        if (totalItemDiscount > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Discount (Total)", fontSize = 12.sp, color = UIDarkGrey)
                                Text(text = "-Rp${String.format(Locale.getDefault(), "%,.0f", totalItemDiscount)}", fontSize = 12.sp, color = UIDarkGrey)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                            Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalBill)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                // Floating action buttons - Only scan button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 34.dp)
                ) {

                    FabCircleButton(size = 56.dp, onClick = { launchCamera() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scan_button),
                            contentDescription = "Scan",
                            tint = Color.Unspecified, // Use Unspecified if the svg/xml already has colors
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Continue button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 34.dp)
                ) {
                    Button(
                        onClick = {
                            android.util.Log.d("NewActivityScreen2", "======== CONTINUE BUTTON CLICKED ========")
                            // Validate IDs
                            if (eventId.isEmpty()) {
                                android.util.Log.e("NewActivityScreen2", "❌ ERROR: EventID is EMPTY!")
                                return@Button
                            }
                            if (activityId.isEmpty()) {
                                android.util.Log.e("NewActivityScreen2", "❌ ERROR: ActivityID is EMPTY!")
                                return@Button
                            }

                            android.util.Log.d("NewActivityScreen2", "✅ EventID: $eventId")
                            android.util.Log.d("NewActivityScreen2", "✅ ActivityID: $activityId")
                            android.util.Log.d("NewActivityScreen2", "✅ Items count: ${receiptItems.size}")

                            // Convert ReceiptItem to Map<String, Any> for Firestore
                            val itemsForDb = receiptItems.map { item ->
                                mapOf<String, Any>(
                                    "itemName" to item.itemName,
                                    "price" to item.price,
                                    "quantity" to item.quantity,
                                    "memberNames" to item.memberNames,
                                    "itemTax" to item.itemTax,
                                    "itemDiscount" to item.itemDiscount,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            }

                            // Log untuk debugging
                            android.util.Log.d("NewActivityScreen2", "💾 Saving ${itemsForDb.size} items...")
                            android.util.Log.d("NewActivityScreen2", "💾 Tax: $globalTaxPercentage% (type: ${globalTaxPercentage.javaClass.simpleName})")
                            android.util.Log.d("NewActivityScreen2", "💾 Discount: $globalDiscountAmount (type: ${globalDiscountAmount.javaClass.simpleName})")
                            itemsForDb.forEachIndexed { index, item ->
                                android.util.Log.d("NewActivityScreen2", "Item[$index]: $item")
                            }

                            // Save to database (even if empty list)
                            viewModel.saveActivityItems(
                                eventId = eventId,
                                activityId = activityId,
                                items = itemsForDb,
                                taxPercentage = globalTaxPercentage,
                                discountAmount = globalDiscountAmount
                            )
                            android.util.Log.d("NewActivityScreen2", "======== saveActivityItems CALLED ========")
                            // LaunchedEffect(isSuccess) will automatically navigate back to DetailedEventScreen
                        },
                        modifier = Modifier.size(width = 188.dp, height = 50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        ),
                        enabled = isLoading.not()  // Disable button saat loading
                    ) {
                        Text(text = if (isLoading) "Saving..." else "Continue", style = AppFont.SemiBold, fontSize = 16.sp)
                    }
                }
            }

            // Full screen loading overlay when processing scan
            if (showScanLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) { }, // Prevent clicks while loading
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(UIWhite)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            color = UIAccentYellow,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Processing Receipt...",
                            style = AppFont.SemiBold,
                            fontSize = 18.sp,
                            color = UIBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Analyzing your receipt\nThis may take 30-60 seconds",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            eventMembers = if (eventMembers.isNotEmpty()) eventMembers else listOf(
                Contact(name = "You"),
                Contact(name = "Jeremy E"),
                Contact(name = "Abel M")
            ),
            taxPercentage = globalTaxPercentage,
            discountAmount = globalDiscountAmount,
            isSplitEqual = isSplitEqual,
            onDismiss = { showAddItemDialog = false },
            onAddItem = { newItem ->
                receiptItems = receiptItems + newItem
                showAddItemDialog = false
            },
            onTaxChanged = { newTaxPercentage ->
                // Simpan tax percentage untuk item berikutnya
                globalTaxPercentage = newTaxPercentage
            },
            onDiscountChanged = { newDiscountAmount ->
                // Simpan discount amount untuk item berikutnya
                globalDiscountAmount = newDiscountAmount
            }
        )
    }

    // Edit Item Dialog
    if (showEditItemDialog && editingItemIndex >= 0 && editingItemIndex < receiptItems.size) {
        val editingItem = receiptItems[editingItemIndex]
        EditItemDialog(
            item = editingItem,
            eventMembers = if (eventMembers.isNotEmpty()) eventMembers else listOf(
                Contact(name = "You"),
                Contact(name = "Jeremy E"),
                Contact(name = "Abel M")
            ),
            taxPercentage = globalTaxPercentage,
            discountAmount = globalDiscountAmount,
            isSplitEqual = isSplitEqual,
            onDismiss = {
                showEditItemDialog = false
                editingItemIndex = -1
            },
            onSaveItem = { updatedItem ->
                val updatedList = receiptItems.toMutableList()
                updatedList[editingItemIndex] = updatedItem
                receiptItems = updatedList
                showEditItemDialog = false
                editingItemIndex = -1
            },
            onDeleteItem = {
                val updatedList = receiptItems.toMutableList()
                updatedList.removeAt(editingItemIndex)
                receiptItems = updatedList
                showEditItemDialog = false
                editingItemIndex = -1
                // Tax dan discount item yang dihapus otomatis hilang dari perhitungan total
                // karena totalItemTax dan totalItemDiscount menggunakan sumOf dari receiptItems
            },
            onTaxChanged = { newTaxPercentage ->
                // Simpan tax percentage untuk referensi
                globalTaxPercentage = newTaxPercentage
            },
            onDiscountChanged = { newDiscountAmount ->
                // Simpan discount amount untuk referensi
                globalDiscountAmount = newDiscountAmount
            }
        )
    }

    // Edit Participants Dialog
    if (showEditParticipantsDialog) {
        EditParticipantsDialog(
            currentParticipants = eventMembers,
            availableContacts = viewModel.availableContacts.collectAsState().value,
            onDismiss = { showEditParticipantsDialog = false },
            onSave = { updatedParticipants ->
                eventMembers = updatedParticipants
                showEditParticipantsDialog = false

                // If Equal Split is active, update all items with new participants
                if (isSplitEqual && receiptItems.isNotEmpty()) {
                    receiptItems = receiptItems.map { item ->
                        item.copy(memberNames = updatedParticipants.map { it.name })
                    }
                }
            }
        )
    }
}

// Custom shape for the receipt card
class ReceiptWaveShape(
    val waveWidth: Dp,
    val waveHeight: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val waveWidthPx = with(density) { waveWidth.toPx() }
        val waveHeightPx = with(density) { waveHeight.toPx() }

        val path = Path().apply {
            moveTo(0f, 0f)

            // Top edge waves
            var currentX = 0f
            while (currentX < size.width) {
                val nextX = currentX + waveWidthPx
                quadraticTo(
                    currentX + waveWidthPx / 2, waveHeightPx,
                    nextX, 0f
                )
                currentX = nextX
            }

            // Right edge
            lineTo(size.width, size.height)

            // Bottom edge waves (reverse direction)
            currentX = size.width
            while (currentX > 0) {
                val nextX = currentX - waveWidthPx
                quadraticTo(
                    currentX - waveWidthPx / 2,
                    size.height - waveHeightPx,
                    nextX,
                    size.height
                )
                currentX = nextX
            }

            // Left edge
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun FabCircleButton(size: Dp, onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(UIWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ParticipantAvatarItem(contact: Contact) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(60.dp)
    ) {
        // Avatar dengan profile picture atau fallback ke initial
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(UIDarkGrey),
            contentAlignment = Alignment.Center
        ) {
            // Load profile picture dari database jika ada
            if (contact.avatarName.isNotBlank()) {
                // Cek resource ID tanpa try-catch di dalam composable
                val resourceId = getDrawableResourceId(contact.avatarName)
                if (resourceId != 0) {
                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = contact.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback ke initial jika resource tidak ditemukan
                    val initial = contact.name.firstOrNull()?.uppercaseChar() ?: "?"
                    Text(
                        text = initial.toString(),
                        color = UIWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Fallback ke initial jika tidak ada profile picture
                val initial = contact.name.firstOrNull()?.uppercaseChar() ?: "?"
                Text(
                    text = initial.toString(),
                    color = UIWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Nama dengan text yang kecil
        Text(
            text = contact.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = UIBlack,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ParticipantAvatarItemSmall(contact: Contact) {
    // Hitung resourceId di luar composable
    val resourceId = remember(contact.avatarName) {
        if (contact.avatarName.isNotBlank()) {
            try {
                getDrawableResourceId(contact.avatarName)
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(65.dp)
    ) {
        // Avatar dengan profile picture atau fallback ke initial
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(UIDarkGrey),
            contentAlignment = Alignment.Center
        ) {
            // Load profile picture dari database jika ada
            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = contact.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback ke initial
                val initial = contact.name.firstOrNull()?.uppercaseChar() ?: "?"
                Text(
                    text = initial.toString(),
                    color = UIWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        // Nama dengan text yang kecil
        Text(
            text = contact.name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = UIBlack,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper function untuk mendapatkan resource ID dari nama file
@Suppress("DiscouragedPrivateApi")
fun getDrawableResourceId(resourceName: String): Int {
    return try {
        val rClass = Class.forName("com.example.luca.R\$drawable")
        val field = rClass.getField(resourceName)
        field.getInt(null)
    } catch (e: Exception) {
        0
    }
}

@Composable
fun GreyAvatarItem(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(UIDarkGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = UIWhite, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UIBlack)
    }
}

@Composable
fun MiniAvatar() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(UIDarkGrey),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, null, tint = UIWhite, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun MiniAvatarWithImage(contact: Contact) {
    val resourceId = remember(contact.avatarName) {
        if (contact.avatarName.isNotBlank()) {
            try {
                getDrawableResourceId(contact.avatarName)
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(UIDarkGrey),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = contact.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.Person, null, tint = UIWhite, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun ReceiptItemRow(
    item: ReceiptItem,
    eventMembers: List<Contact>,
    onItemClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(UIWhite)
            .border(
                width = 1.dp,
                color = UIGrey.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onItemClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${item.quantity}x",
                    color = UIDarkGrey,
                    fontSize = 14.sp,
                    modifier = Modifier.width(28.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        color = UIBlack,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Tampilkan participant icons dari database
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(item.memberNames.size) { index ->
                            val memberName = item.memberNames[index]
                            val participant = eventMembers.find { it.name == memberName }
                            if (participant != null) {
                                MiniAvatarWithImage(participant)
                            } else {
                                MiniAvatar()
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tampilkan total price (quantity * price)
                val totalPrice = item.price.toDouble() * item.quantity
                Text(
                    text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalPrice)}",
                    color = UIBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Edit indicator icon
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Tap to edit",
                    tint = UIDarkGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Show item-specific tax and discount if they exist
        if (item.itemTax > 0 || item.itemDiscount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = UIGrey.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (item.itemTax > 0) {
                        Text(
                            text = "Tax: +Rp${String.format(Locale.getDefault(), "%,.0f", item.itemTax)}",
                            color = UIDarkGrey,
                            fontSize = 11.sp
                        )
                    }
                    if (item.itemDiscount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Discount: -Rp${String.format(Locale.getDefault(), "%,.0f", item.itemDiscount)}",
                            color = UIDarkGrey,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    eventMembers: List<Contact>,
    taxPercentage: Double,
    discountAmount: Double,
    isSplitEqual: Boolean = false,
    onDismiss: () -> Unit,
    onAddItem: (ReceiptItem) -> Unit,
    onTaxChanged: (Double) -> Unit,
    onDiscountChanged: (Double) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    // Jika equal split aktif, otomatis pilih semua member
    var selectedMembers by remember {
        mutableStateOf(
            if (isSplitEqual) eventMembers.map { it.name }.toSet()
            else setOf<String>()
        )
    }
    var tempTax by remember { mutableStateOf(taxPercentage.toString()) }
    var tempDiscount by remember { mutableStateOf(discountAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = "Add New Item",
                style = AppFont.SemiBold,
                fontSize = 18.sp,
                color = UIBlack
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Item Name with rounded corners
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price and Quantity Row with rounded corners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price (Rp)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Members Selection seperti Add Participants screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shared by:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )
                    if (isSplitEqual) {
                        Text(
                            text = "Equal Split Active",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = UIAccentYellow
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Container dengan background seperti Add Participants - maksimal 3 items visible
                val containerHeight = if (eventMembers.size > 3) 180.dp else 120.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UIBackground)
                        .padding(8.dp)
                        .height(containerHeight)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(eventMembers) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSplitEqual) {
                                        selectedMembers = if (selectedMembers.contains(member.name)) {
                                            selectedMembers - member.name
                                        } else {
                                            selectedMembers + member.name
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Avatar dengan profile picture dari database
                                    val resourceId = remember(member.avatarName) {
                                        if (member.avatarName.isNotBlank()) {
                                            try {
                                                getDrawableResourceId(member.avatarName)
                                            } catch (_: Exception) {
                                                0
                                            }
                                        } else {
                                            0
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(UIDarkGrey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (resourceId != 0) {
                                            Image(
                                                painter = painterResource(id = resourceId),
                                                contentDescription = member.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            // Fallback ke initial letter
                                            val initial = member.name.firstOrNull()?.uppercaseChar() ?: "?"
                                            Text(
                                                text = initial.toString(),
                                                color = UIWhite,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = member.name,
                                        fontSize = 16.sp,
                                        color = UIBlack,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Selection indicator - checkmark or plus
                                if (selectedMembers.contains(member.name)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = UIWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(UIGrey.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = UIDarkGrey,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Divider between participants (except for last item)
                            if (member != eventMembers.last()) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = UIGrey.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tax and Discount with rounded corners - OPTIONAL FIELDS
                Text(
                    text = "Optional:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempTax,
                        onValueChange = { tempTax = it },
                        label = { Text("Tax (%)") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = tempDiscount,
                        onValueChange = {
                            // Format as Rupiah input
                            val numbersOnly = it.replace("[^\\d]".toRegex(), "")
                            tempDiscount = numbersOnly
                        },
                        label = { Text("Discount (Rp)") },
                        placeholder = { Text("0") },
                        prefix = { Text("Rp") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = itemPrice.toDoubleOrNull() ?: 0.0
                    val quantity = itemQuantity.toIntOrNull() ?: 1
                    val taxPercentage = tempTax.toDoubleOrNull() ?: 0.0
                    val discount = tempDiscount.toDoubleOrNull() ?: 0.0

                    // Update global tax and discount values
                    onTaxChanged(taxPercentage)
                    onDiscountChanged(discount)

                    // Calculate item tax in Rupiah based on percentage
                    val itemTotalPrice = price * quantity
                    val itemTaxAmount = itemTotalPrice * taxPercentage / 100

                    if (itemName.isNotBlank() && price > 0 && selectedMembers.isNotEmpty()) {
                        val newItem = ReceiptItem(
                            quantity = quantity,
                            itemName = itemName,
                            price = price.toLong(),
                            members = selectedMembers.map { UIAccentYellow },
                            memberNames = selectedMembers.toList(),
                            itemTax = itemTaxAmount,
                            itemDiscount = discount
                        )

                        onAddItem(newItem)
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                )
            ) {
                Text("Add Item", style = AppFont.SemiBold, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIDarkGrey, style = AppFont.Medium)
            }
        },
        containerColor = UIWhite,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: ReceiptItem,
    eventMembers: List<Contact>,
    taxPercentage: Double,
    discountAmount: Double,
    isSplitEqual: Boolean = false,
    onDismiss: () -> Unit,
    onSaveItem: (ReceiptItem) -> Unit,
    onDeleteItem: () -> Unit,
    onTaxChanged: (Double) -> Unit,
    onDiscountChanged: (Double) -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var itemPrice by remember { mutableStateOf(item.price.toString()) }
    var itemQuantity by remember { mutableStateOf(item.quantity.toString()) }
    // Jika equal split aktif, otomatis gunakan semua member
    var selectedMembers by remember {
        mutableStateOf(
            if (isSplitEqual) eventMembers.map { it.name }.toSet()
            else item.memberNames.toSet()
        )
    }
    // Calculate tax percentage from item's tax amount for display
    val initialTaxPercentage = if (item.price.toDouble() * item.quantity > 0) {
        (item.itemTax / (item.price.toDouble() * item.quantity) * 100).toString()
    } else {
        "0"
    }
    var tempTax by remember { mutableStateOf(initialTaxPercentage) }
    var tempDiscount by remember { mutableStateOf(item.itemDiscount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Item",
                    style = AppFont.SemiBold,
                    fontSize = 18.sp,
                    color = UIBlack
                )
                TextButton(
                    onClick = onDeleteItem,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete", fontSize = 14.sp, style = AppFont.Medium)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Item Name with rounded corners
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price and Quantity Row with rounded corners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price (Rp)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Members Selection seperti Add Participants screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shared by:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )
                    if (isSplitEqual) {
                        Text(
                            text = "Equal Split Active",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = UIAccentYellow
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Container dengan background seperti Add Participants - maksimal 3 items visible
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UIBackground)
                        .padding(8.dp)
                        .height(120.dp) // Fixed height
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(eventMembers) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isSplitEqual) {
                                        selectedMembers = if (selectedMembers.contains(member.name)) {
                                            selectedMembers - member.name
                                        } else {
                                            selectedMembers + member.name
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Avatar with profile picture from resources if available
                                    val resourceId = remember(member.avatarName) {
                                        if (member.avatarName.isNotBlank()) {
                                            try { getDrawableResourceId(member.avatarName) } catch (_: Exception) { 0 }
                                        } else { 0 }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(UIDarkGrey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (resourceId != 0) {
                                            Image(
                                                painter = painterResource(id = resourceId),
                                                contentDescription = member.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            // Fallback to initial letter
                                            val initial = member.name.firstOrNull()?.uppercaseChar() ?: "?"
                                            Text(
                                                text = initial.toString(),
                                                color = UIWhite,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = member.name,
                                        fontSize = 16.sp,
                                        color = UIBlack,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Selection indicator - checkmark or plus
                                if (selectedMembers.contains(member.name)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = UIWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(UIGrey.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = UIDarkGrey,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Divider between participants (except for last item)
                            if (member != eventMembers.last()) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = UIGrey.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tax and Discount with rounded corners
                Text(
                    text = "Optional:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempTax,
                        onValueChange = { tempTax = it },
                        label = { Text("Tax (%)") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = tempDiscount,
                        onValueChange = {
                            // Format as Rupiah input
                            val numbersOnly = it.replace("[^\\d]".toRegex(), "")
                            tempDiscount = numbersOnly
                        },
                        label = { Text("Discount (Rp)") },
                        placeholder = { Text("0") },
                        prefix = { Text("Rp") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = itemPrice.toLongOrNull() ?: 0L
                    val quantity = itemQuantity.toIntOrNull() ?: 1
                    val taxPercentage = tempTax.toDoubleOrNull() ?: 0.0
                    val discount = tempDiscount.toDoubleOrNull() ?: 0.0

                    // Update global tax and discount values
                    onTaxChanged(taxPercentage)
                    onDiscountChanged(discount)

                    // Calculate item tax in Rupiah based on percentage
                    val itemTotalPrice = price.toDouble() * quantity
                    val itemTaxAmount = itemTotalPrice * taxPercentage / 100

                    if (itemName.isNotBlank() && price > 0 && selectedMembers.isNotEmpty()) {
                        val updatedItem = ReceiptItem(
                            quantity = quantity,
                            itemName = itemName,
                            price = price,
                            members = selectedMembers.map { UIAccentYellow }, // Placeholder colors for selected members
                            memberNames = selectedMembers.toList(), // Store actual member names
                            itemTax = itemTaxAmount,
                            itemDiscount = discount
                        )

                        onSaveItem(updatedItem)
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                )
            ) {
                Text("Save Changes", style = AppFont.SemiBold, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UIDarkGrey, style = AppFont.Medium)
            }
        },
        containerColor = UIWhite,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditParticipantsDialog(
    currentParticipants: List<Contact>,
    availableContacts: List<Contact>,
    onDismiss: () -> Unit,
    onSave: (List<Contact>) -> Unit
) {
    var selectedParticipants by remember { mutableStateOf(currentParticipants.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Participants",
                style = AppFont.SemiBold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Text(
                    text = "Select participants for this activity",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Scrollable list of available contacts
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(UIBackground)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableContacts) { contact ->
                        val isSelected = selectedParticipants.any { it.name == contact.name }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) UIAccentYellow.copy(alpha = 0.2f) else UIWhite)
                                .clickable {
                                    selectedParticipants = if (isSelected) {
                                        selectedParticipants.filterNot { it.name == contact.name }.toSet()
                                    } else {
                                        selectedParticipants + contact
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            val resourceId = remember(contact.avatarName) {
                                if (contact.avatarName.isNotBlank()) {
                                    try {
                                        getDrawableResourceId(contact.avatarName)
                                    } catch (_: Exception) {
                                        R.drawable.avatar_1
                                    }
                                } else {
                                    R.drawable.avatar_1
                                }
                            }

                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = contact.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name
                            Text(
                                text = contact.name,
                                style = AppFont.Medium,
                                fontSize = 16.sp,
                                color = UIBlack,
                                modifier = Modifier.weight(1f)
                            )

                            // Checkmark indicator
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(UIAccentYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        style = AppFont.Bold,
                                        fontSize = 14.sp,
                                        color = UIBlack
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected count
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${selectedParticipants.size} participant(s) selected",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = if (selectedParticipants.isNotEmpty()) UIAccentYellow else Color.Red
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedParticipants.isNotEmpty()) {
                        onSave(selectedParticipants.toList())
                    }
                },
                enabled = selectedParticipants.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", style = AppFont.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = AppFont.Medium, color = UIDarkGrey)
            }
        },
        containerColor = UIWhite,
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AddActivity2Preview() {
    LucaTheme {
        AddActivityScreen2()
    }
}

// Helper function
fun createImageFileForCamera(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        context.externalCacheDir
    )
}









