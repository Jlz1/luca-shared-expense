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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen2(
    viewModel: AddEventViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    // --- STATE: Agar Switch Equal Split bisa Nyala/Mati ---
    var isSplitEqual by remember { mutableStateOf(false) }

    // --- STATE: Event Members from ViewModel ---
    val eventMembers by viewModel.selectedParticipants.collectAsState()

    // --- STATE: Receipt Items ---
    var receiptItems by remember {
        mutableStateOf(listOf<ReceiptItem>())
    }

    // Initialize default receipt item when eventMembers changes
    LaunchedEffect(eventMembers) {
        if (receiptItems.isEmpty()) {
            val defaultMemberNames = if (eventMembers.isNotEmpty()) {
                eventMembers.take(3).map { it.name }
            } else {
                listOf("You", "Jeremy E", "Abel M") // Fallback jika tidak ada participants
            }
            receiptItems = listOf(
                ReceiptItem(
                    quantity = 1,
                    itemName = "Gurame Bakar Kecap",
                    price = 120000L,
                    members = defaultMemberNames.map { UIAccentYellow }, // Placeholder colors for members
                    memberNames = defaultMemberNames
                )
            )
        }
    }

    // --- STATE: Tax and Discount yang diperbaiki ---
    var globalTaxPercentage by remember { mutableStateOf(0.0) }
    var globalDiscountAmount by remember { mutableStateOf(0.0) }

    // --- STATE: Dialog ---
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {

        // 2. HEADER
        HeaderSection(
            currentState = HeaderState.NEW_ACTIVITY,
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
                            .height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Participants list
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(UIWhite)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (eventMembers.isNotEmpty()) {
                                    items(eventMembers) { member ->
                                        GreyAvatarItem(member.name)
                                    }
                                } else {
                                    // Fallback display jika tidak ada participants
                                    item { GreyAvatarItem("You") }
                                    item { GreyAvatarItem("Jeremy E") }
                                    item { GreyAvatarItem("Abel M") }
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
                                    text = "Dinner at Floating Resto",
                                    style = AppFont.SemiBold,
                                    fontSize = 16.sp,
                                    color = UIBlack
                                )
                                Text(
                                    text = "Paid by Abel M",
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
                        val subtotal = receiptItems.sumOf { it.price.toDouble() * it.quantity }
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
                        onClick = {},
                        modifier = Modifier.size(width = 188.dp, height = 50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        )
                    ) {
                        Text(text = "Continue", style = AppFont.SemiBold, fontSize = 16.sp)
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

                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(item.memberNames.size) {
                        MiniAvatar()
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Rp${String.format(Locale.getDefault(), "%,.0f", item.price.toDouble())}",
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
