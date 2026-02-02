package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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

    // --- STATE: Event Members from Activity (exclude paidBy) ---
    // Ambil participant dari activity.participants, exclude paidBy
    val eventMembers = remember(activity) {
        if (activity != null) {
            // Konversi ParticipantData ke Contact, exclude paidBy
            activity.participants
                .filter { participantData ->
                    // Exclude paidBy jika ada
                    val paidByName = activity.paidBy?.name
                    paidByName == null || participantData.name != paidByName
                }
                .map { participantData ->
                    Contact(
                        name = participantData.name,
                        avatarName = participantData.avatarName
                    )
                }
        } else {
            // Fallback ke ViewModel selectedParticipants
            emptyList()
        }
    }

    // --- STATE: Receipt Items ---
    var receiptItems by remember {
        mutableStateOf(listOf<ReceiptItem>())
    }

    // Load items for this Activity when the screen opens
    LaunchedEffect(eventId, activityId) {
        if (eventId.isNotEmpty() && activityId.isNotEmpty()) {
            val repo = LucaFirebaseRepository()
            val items = withContext(Dispatchers.IO) {
                repo.getActivityItems(eventId, activityId)
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
                ReceiptItem(
                    quantity = qty,
                    itemName = name,
                    price = price,
                    members = memberNames.map { UIAccentYellow }, // color placeholder not used in save
                    memberNames = memberNames
                )
            }
        }
    }

    // --- STATE: Tax and Discount yang diperbaiki ---
    var globalTaxPercentage by remember { mutableStateOf(0.0) }
    var globalDiscountAmount by remember { mutableStateOf(0.0) }

    // --- STATE: Dialog ---
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf(-1) }

    // Monitor save success state
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(isSuccess) {
        if (isSuccess && eventId.isNotEmpty()) {
            android.util.Log.d("NewActivityScreen2", "✅✅✅ SUCCESS! Navigating back to DetailedEventScreen")
            onSaveSuccess(eventId)
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
            onLeftIconClick = onBackClick
        )

        // 3. KONTEN AREA (PUTIH & ROUNDED)
        Box(
            modifier = Modifier
                .weight(1f) // Mengisi sisa ruang ke bawah
                .fillMaxWidth()
                .background(UIBackground)
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (eventMembers.isNotEmpty()) {
                                    items(eventMembers) { member ->
                                        ParticipantAvatarItemSmall(member)
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
                                onCheckedChange = { isSplitEqual = it }, // Update state saat diklik
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
                        val taxAmount = subtotal * globalTaxPercentage / 100
                        val totalBill = subtotal + taxAmount - globalDiscountAmount

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                            Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", subtotal)}", fontSize = 12.sp, color = UIDarkGrey)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Tax (${globalTaxPercentage.toInt()}%)", fontSize = 12.sp, color = UIDarkGrey)
                            Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", taxAmount)}", fontSize = 12.sp, color = UIDarkGrey)
                        }
                        if (globalDiscountAmount > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Discount", fontSize = 12.sp, color = UIDarkGrey)
                                Text(text = "-Rp${String.format(Locale.getDefault(), "%,.0f", globalDiscountAmount)}", fontSize = 12.sp, color = UIDarkGrey)
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

                    FabCircleButton(size = 56.dp) {
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
                            // Save items to database
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

                            // Only save if there are items
                            if (receiptItems.isEmpty()) {
                                android.util.Log.w("NewActivityScreen2", "⚠️ No items to save, proceeding without items")
                                // Still navigate even if no items
                                onSaveSuccess(eventId)
                                return@Button
                            }

                            // Convert ReceiptItem to Map<String, Any> for Firestore
                            val itemsForDb = receiptItems.map { item ->
                                mapOf<String, Any>(
                                    "itemName" to item.itemName,
                                    "price" to item.price,
                                    "quantity" to item.quantity,
                                    "memberNames" to item.memberNames,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            }

                            // Log untuk debugging
                            android.util.Log.d("NewActivityScreen2", "Saving ${itemsForDb.size} items...")
                            android.util.Log.d("NewActivityScreen2", "Tax: $globalTaxPercentage%, Discount: ${globalDiscountAmount}")
                            itemsForDb.forEachIndexed { index, item ->
                                android.util.Log.d("NewActivityScreen2", "Item[$index]: $item")
                            }

                            viewModel.saveActivityItems(
                                eventId = eventId,
                                activityId = activityId,
                                items = itemsForDb,
                                taxPercentage = globalTaxPercentage,
                                discountAmount = globalDiscountAmount
                            )
                            android.util.Log.d("NewActivityScreen2", "======== saveActivityItems CALLED ========")
                            // isSuccess LaunchedEffect akan handle navigation
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
            onDismiss = { showAddItemDialog = false },
            onAddItem = { newItem ->
                receiptItems = receiptItems + newItem
                showAddItemDialog = false
            },
            onTaxChanged = { globalTaxPercentage = it },
            onDiscountChanged = { globalDiscountAmount = it }
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
            },
            onTaxChanged = { globalTaxPercentage = it },
            onDiscountChanged = { globalDiscountAmount = it }
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
            maxLines = 2,
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
            maxLines = 2,
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
    Row(
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    eventMembers: List<Contact>,
    taxPercentage: Double,
    discountAmount: Double,
    onDismiss: () -> Unit,
    onAddItem: (ReceiptItem) -> Unit,
    onTaxChanged: (Double) -> Unit,
    onDiscountChanged: (Double) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var tempTax by remember { mutableStateOf(taxPercentage.toString()) }
    var tempDiscount by remember { mutableStateOf(discountAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                Text(
                    text = "Shared by:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = UIBlack
                )
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
                                    .clickable {
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
                                            } catch (e: Exception) {
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
                    val tax = tempTax.toDoubleOrNull() ?: 0.0
                    val discount = tempDiscount.toDoubleOrNull() ?: 0.0

                    if (itemName.isNotBlank() && price > 0 && selectedMembers.isNotEmpty()) {
                        val newItem = ReceiptItem(
                            quantity = quantity,
                            itemName = itemName,
                            price = price.toLong(),
                            members = selectedMembers.map { UIAccentYellow },
                            memberNames = selectedMembers.toList()
                        )

                        onTaxChanged(tax)
                        onDiscountChanged(discount)
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
    onDismiss: () -> Unit,
    onSaveItem: (ReceiptItem) -> Unit,
    onDeleteItem: () -> Unit,
    onTaxChanged: (Double) -> Unit,
    onDiscountChanged: (Double) -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var itemPrice by remember { mutableStateOf(item.price.toString()) }
    var itemQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var selectedMembers by remember { mutableStateOf(item.memberNames.toSet()) }
    var tempTax by remember { mutableStateOf(taxPercentage.toString()) }
    var tempDiscount by remember { mutableStateOf(discountAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                Text(
                    text = "Shared by:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = UIBlack
                )
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
                                    .clickable {
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
                                    // Avatar dengan warna berdasarkan participant
                                    val avatarColor = when (member.name.lowercase()) {
                                        "you" -> Color(0xFF4A90E2)
                                        "jeremy e" -> Color(0xFFE27D60)
                                        "abel m" -> Color(0xFF85C1E9)
                                        "test" -> Color(0xFF58D68D)
                                        "endi ganteng" -> Color(0xFFEC7063)
                                        "john" -> Color(0xFFAF7AC5)
                                        "penis" -> Color(0xFFF7DC6F)
                                        else -> UIDarkGrey
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(avatarColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = UIWhite,
                                            modifier = Modifier.size(24.dp)
                                        )
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
                    val tax = tempTax.toDoubleOrNull() ?: 0.0
                    val discount = tempDiscount.toDoubleOrNull() ?: 0.0

                    if (itemName.isNotBlank() && price > 0 && selectedMembers.isNotEmpty()) {
                        val updatedItem = ReceiptItem(
                            quantity = quantity,
                            itemName = itemName,
                            price = price,
                            members = selectedMembers.map { UIAccentYellow }, // Placeholder colors for selected members
                            memberNames = selectedMembers.toList() // Store actual member names
                        )

                        onTaxChanged(tax)
                        onDiscountChanged(discount)
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

@Preview(showBackground = true)
@Composable
fun AddActivity2Preview() {
    LucaTheme {
        AddActivityScreen2()
    }
}



