package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noir.luca.R
import com.example.luca.model.Contact
import com.example.luca.model.ParsedReceiptData
import com.example.luca.model.ParsedReceiptItem
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.ContactsViewModel
import java.util.Locale

@Composable
fun ScanResultScreen(
    parsedData: ParsedReceiptData,
    onBackClick: () -> Unit,
    onScanAgain: () -> Unit
) {
    val contactsViewModel: ContactsViewModel = viewModel()
    val eventMembers by contactsViewModel.contacts.collectAsState()

    var receiptItems by remember { mutableStateOf(parsedData.items) }
    var globalTax by remember { mutableStateOf(parsedData.tax) }
    var globalDiscount by remember { mutableStateOf(parsedData.discount) }
    var isSplitEqual by remember { mutableStateOf(false) }

    // Global Paid By - satu untuk seluruh receipt
    var globalPaidBy by remember { mutableStateOf<Contact?>(null) }
    var showPaidByDialog by remember { mutableStateOf(false) }

    // Calculate subtotal and total
    val subtotal = receiptItems.sumOf { it.itemPrice * it.itemQuantity - it.itemDiscount }
    val totalBill = subtotal + globalTax - globalDiscount

    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // Header
        HeaderSection(
            currentState = HeaderState.DETAILS,
            onLeftIconClick = onBackClick
        )

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(UIBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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
                        if (eventMembers.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(eventMembers) { member ->
                                    ParticipantAvatarItemSmall(member)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No members available",
                                    style = AppFont.Regular,
                                    fontSize = 12.sp,
                                    color = UIDarkGrey
                                )
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
                        Switch(
                            checked = isSplitEqual,
                            onCheckedChange = { isSplitEqual = it },
                            modifier = Modifier
                                .scale(1.2f)
                                .height(30.dp),
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = UIWhite,
                                uncheckedTrackColor = UIGrey,
                                checkedTrackColor = UIAccentYellow,
                                uncheckedBorderColor = Color.Transparent,
                                checkedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Paid By Section (Global - untuk seluruh receipt)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Paid By (Required):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = UIDarkGrey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UIBackground)
                            .clickable { showPaidByDialog = true }
                            .padding(16.dp)
                    ) {
                        if (globalPaidBy != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(UIAccentYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarRes = when (globalPaidBy!!.avatarName) {
                                        "avatar_1" -> R.drawable.avatar_1
                                        "avatar_2" -> R.drawable.avatar_2
                                        "avatar_3" -> R.drawable.avatar_3
                                        "avatar_4" -> R.drawable.avatar_4
                                        "avatar_5" -> R.drawable.avatar_5
                                        else -> R.drawable.avatar_1
                                    }
                                    Image(
                                        painter = painterResource(id = avatarRes),
                                        contentDescription = globalPaidBy!!.name,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Text(
                                    text = globalPaidBy!!.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = UIBlack
                                )
                            }
                        } else {
                            Text(
                                text = "Select who paid for this receipt",
                                fontSize = 14.sp,
                                color = UIDarkGrey
                            )
                        }
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Struk Belanja",
                            style = AppFont.Bold,
                            fontSize = 18.sp,
                            color = UIBlack
                        )
                        Text(
                            text = "Hasil Scan OCR",
                            style = AppFont.Regular,
                            fontSize = 12.sp,
                            color = UIDarkGrey
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Items Display
                        if (receiptItems.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(receiptItems.size) { index ->
                                    val item = receiptItems[index]
                                    ParsedReceiptItemRow(
                                        item = item,
                                        onItemClick = {
                                            editingItemIndex = index
                                            showEditItemDialog = true
                                        }
                                    )
                                }
                            }

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

                Spacer(modifier = Modifier.height(10.dp))

                // Total Bill section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(16.dp)
                ) {
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
                        Text(text = "Tax", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", globalTax)}", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    if (globalDiscount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Discount", fontSize = 12.sp, color = UIDarkGrey)
                            Text(text = "-Rp${String.format(Locale.getDefault(), "%,.0f", globalDiscount)}", fontSize = 12.sp, color = UIDarkGrey)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScanAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Scan Ulang", color = UIBlack, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Selesai", color = UIBlack, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Paid By Dialog
    if (showPaidByDialog) {
        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = { Text("Select Paid By", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UIBackground)
                        .padding(8.dp)
                        .height(240.dp) // Fixed height untuk scrollable
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventMembers.size) { index ->
                            val member = eventMembers[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (globalPaidBy == member) UIAccentYellow.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        globalPaidBy = member
                                        showPaidByDialog = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(UIAccentYellow),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val avatarRes = when (member.avatarName) {
                                            "avatar_1" -> R.drawable.avatar_1
                                            "avatar_2" -> R.drawable.avatar_2
                                            "avatar_3" -> R.drawable.avatar_3
                                            "avatar_4" -> R.drawable.avatar_4
                                            "avatar_5" -> R.drawable.avatar_5
                                            else -> R.drawable.avatar_1
                                        }
                                        Image(
                                            painter = painterResource(id = avatarRes),
                                            contentDescription = member.name,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                    Text(
                                        text = member.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = UIBlack
                                    )
                                }

                                // Checkmark untuk yang terpilih
                                if (globalPaidBy == member) {
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
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaidByDialog = false }) {
                    Text("Close", color = UIAccentYellow)
                }
            }
        )
    }

    // Edit Item Dialog
    if (showEditItemDialog && editingItemIndex >= 0 && editingItemIndex < receiptItems.size) {
        EditScanItemDialog(
            item = receiptItems[editingItemIndex],
            eventMembers = eventMembers,
            onDismiss = { showEditItemDialog = false },
            onSave = { updatedItem ->
                val mutableList = receiptItems.toMutableList()
                mutableList[editingItemIndex] = updatedItem
                receiptItems = mutableList
                showEditItemDialog = false
            },
            onDelete = {
                val mutableList = receiptItems.toMutableList()
                mutableList.removeAt(editingItemIndex)
                receiptItems = mutableList
                showEditItemDialog = false
            }
        )
    }
}

@Composable
fun EditScanItemDialog(
    item: ParsedReceiptItem,
    eventMembers: List<Contact>,
    onDismiss: () -> Unit,
    onSave: (ParsedReceiptItem) -> Unit,
    onDelete: () -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var itemPrice by remember { mutableStateOf(item.itemPrice.toString()) }
    var itemQuantity by remember { mutableStateOf(item.itemQuantity.toString()) }
    var itemDiscount by remember { mutableStateOf(item.itemDiscount.toString()) }
    var itemTax by remember { mutableStateOf(item.itemTax.toString()) }
    var selectedSharedBy by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item Name - Full Width
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Price + Qty Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Price (Rp)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it.filter { char -> char.isDigit() } },
                        label = { Text("Qty") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Shared By Selection - konsisten dengan NewActivityScreen2
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Shared by:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )

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
                            items(eventMembers.size) { index ->
                                val member = eventMembers[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSharedBy = if (selectedSharedBy.contains(member.name)) {
                                                selectedSharedBy - member.name
                                            } else {
                                                selectedSharedBy + member.name
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
                                        // Avatar dengan image dari drawable
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(UIAccentYellow),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val avatarRes = when (member.avatarName) {
                                                "avatar_1" -> R.drawable.avatar_1
                                                "avatar_2" -> R.drawable.avatar_2
                                                "avatar_3" -> R.drawable.avatar_3
                                                "avatar_4" -> R.drawable.avatar_4
                                                "avatar_5" -> R.drawable.avatar_5
                                                else -> R.drawable.avatar_1
                                            }
                                            Image(
                                                painter = painterResource(id = avatarRes),
                                                contentDescription = member.name,
                                                modifier = Modifier.size(40.dp)
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
                                    if (selectedSharedBy.contains(member.name)) {
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
                            }
                        }
                    }
                }

                // Optional Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Optional:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = UIDarkGrey
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = itemTax,
                            onValueChange = { itemTax = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Tax (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = itemDiscount,
                            onValueChange = { itemDiscount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Discount (Rp)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Text("Hapus", color = Color.Red)
                }
                TextButton(
                    onClick = {
                        val updatedItem = ParsedReceiptItem(
                            itemName = itemName,
                            itemPrice = itemPrice.toDoubleOrNull() ?: 0.0,
                            itemQuantity = itemQuantity.toIntOrNull() ?: 1,
                            itemDiscount = itemDiscount.toDoubleOrNull() ?: 0.0,
                            itemTax = itemTax.toDoubleOrNull() ?: 0.0
                        )
                        onSave(updatedItem)
                    }
                ) {
                    Text("Simpan", color = UIAccentYellow)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = UIDarkGrey)
            }
        }
    )
}

@Composable
fun ParsedReceiptItemRow(
    item: ParsedReceiptItem,
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
                text = "${item.itemQuantity}x",
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
                Text(
                    text = "Item from receipt",
                    color = UIDarkGrey,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalPrice = item.itemPrice * item.itemQuantity
            Text(
                text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalPrice)}",
                color = UIBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Icon(
                Icons.Default.Edit,
                contentDescription = "Tap to edit",
                tint = UIDarkGrey.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

