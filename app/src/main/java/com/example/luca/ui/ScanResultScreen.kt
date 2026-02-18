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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.R
import com.example.luca.model.Contact
import com.example.luca.model.ParsedReceiptData
import com.example.luca.model.ParsedReceiptItem
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.ContactsViewModel
import java.util.Locale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale


@Composable
fun ScanResultScreen(
    parsedData: ParsedReceiptData,
    onBackClick: () -> Unit,
    onScanAgain: () -> Unit,
    onContinue: () -> Unit = {}
) {
    val contactsViewModel: ContactsViewModel = viewModel()
    val eventMembers by contactsViewModel.contacts.collectAsState()

    var receiptItems by remember { mutableStateOf(parsedData.items) }
    var globalTax by remember { mutableStateOf(parsedData.tax) }
    var globalServiceCharge by remember { mutableStateOf(parsedData.serviceCharge) }
    var globalDiscount by remember { mutableStateOf(parsedData.discount) }
    var isSplitEqual by remember { mutableStateOf(false) }

    // Global Paid By - satu untuk seluruh receipt
    var globalPaidBy by remember { mutableStateOf<Contact?>(null) }
    var showPaidByDialog by remember { mutableStateOf(false) }
    var showSummaryDialog by remember { mutableStateOf(false) }

    // Calculate subtotal and total
    val subtotal = receiptItems.sumOf { it.itemPrice * it.itemQuantity }
    val totalItemTax = receiptItems.sumOf { it.itemTax }
    val totalItemDiscount = receiptItems.sumOf { it.itemDiscount }
    // Total bill = subtotal + all taxes + global service charge - all discounts
    val totalBill = subtotal + totalItemTax + globalTax + globalServiceCharge - totalItemDiscount - globalDiscount

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
                                    // --- FIX: Ganti Image Lokal dengan AsyncImage (Coil) ---
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("https://api.dicebear.com/9.x/avataaars/png?seed=${globalPaidBy!!.avatarName}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = globalPaidBy!!.name,
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                        error = painterResource(android.R.drawable.ic_menu_report_image)
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
                                        eventMembers = eventMembers,
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
                    // Calculate totals from items
                    val totalItemTax = receiptItems.sumOf { it.itemTax }
                    val totalItemDiscount = receiptItems.sumOf { it.itemDiscount }

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
                        Text(text = "Tax (Total)", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalItemTax + globalTax)}", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Service Charge", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp${String.format(Locale.getDefault(), "%,.0f", globalServiceCharge)}", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    if (totalItemDiscount + globalDiscount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Discount (Total)", fontSize = 12.sp, color = UIDarkGrey)
                            Text(text = "-Rp${String.format(Locale.getDefault(), "%,.0f", totalItemDiscount + globalDiscount)}", fontSize = 12.sp, color = UIDarkGrey)
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
                        onClick = { showSummaryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = globalPaidBy != null && receiptItems.isNotEmpty()
                    ) {
                        Text("Lihat Summary", color = UIBlack, fontWeight = FontWeight.Bold)
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
                                        // --- FIX: Ganti Image Lokal dengan AsyncImage (Coil) ---
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data("https://api.dicebear.com/9.x/avataaars/png?seed=${member.avatarName}")
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = member.name,
                                            modifier = Modifier.size(40.dp),
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                            error = painterResource(android.R.drawable.ic_menu_report_image)
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

    // Summary Dialog - MATCHING SUMMARYSCREEN WITH DEBT CALCULATION
    if (showSummaryDialog) {
        // Calculate individual consumption and debts
        val memberConsumption = mutableMapOf<String, Double>()
        val allMembers = mutableSetOf<String>()

        // Collect all members who shared items
        receiptItems.forEach { item ->
            item.sharedBy.forEach { memberName ->
                allMembers.add(memberName)
            }
        }

        // Calculate how much each member consumed
        receiptItems.forEach { item ->
            val itemTotalCost = (item.itemPrice * item.itemQuantity) + item.itemTax - item.itemDiscount
            val shareCount = item.sharedBy.size

            if (shareCount > 0) {
                val costPerPerson = itemTotalCost / shareCount
                item.sharedBy.forEach { memberName ->
                    memberConsumption[memberName] = (memberConsumption[memberName] ?: 0.0) + costPerPerson
                }
            }
        }

        // Add shared charges (tax, service charge, discount) split equally
        val sharedCharges = globalTax + globalServiceCharge - globalDiscount
        val memberCount = if (allMembers.isNotEmpty()) allMembers.size else 1
        val sharedChargePerPerson = sharedCharges / memberCount

        allMembers.forEach { memberName ->
            memberConsumption[memberName] = (memberConsumption[memberName] ?: 0.0) + sharedChargePerPerson
        }

        // Calculate settlements (debts to payer)
        val settlements = mutableListOf<Triple<String, String, Double>>() // (from, to, amount)
        globalPaidBy?.let { payer ->
            memberConsumption.forEach { (memberName, amount) ->
                if (memberName != payer.name && amount > 0) {
                    settlements.add(Triple(memberName, payer.name, amount))
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title = {
                Text(
                    "Summary Hasil Scan",
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIBlack
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Total Expense Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = UIAccentYellow.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Expense",
                                style = AppFont.SemiBold,
                                fontSize = 14.sp,
                                color = UIBlack
                            )
                            Text(
                                text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalBill)}",
                                style = AppFont.Bold,
                                fontSize = 18.sp,
                                color = UIBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Paid By
                    globalPaidBy?.let { payer ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(UIAccentYellow.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Dibayar oleh:",
                                fontSize = 12.sp,
                                color = UIDarkGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(UIAccentYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("https://api.dicebear.com/9.x/avataaars/png?seed=${payer.avatarName}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = payer.name,
                                        modifier = Modifier.size(32.dp),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                                    )
                                }
                                Text(
                                    text = payer.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UIBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Settlements (Debts) Section - MATCHING SUMMARYSCREEN
                    Text(
                        text = "Settlements (${settlements.size})",
                        style = AppFont.SemiBold,
                        fontSize = 18.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (settlements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No settlements needed.\nEveryone is settled up! 🎉",
                                style = AppFont.Regular,
                                fontSize = 14.sp,
                                color = UIDarkGrey,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            settlements.forEach { (fromName, toName, amount) ->
                                val fromContact = eventMembers.find { it.name == fromName }
                                val toContact = eventMembers.find { it.name == toName }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(UIWhite)
                                        .border(1.dp, UIAccentYellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left: Avatar Flow
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        // From Avatar
                                        if (fromContact != null) {
                                            ParticipantAvatarItemSmall(fromContact)
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(UIGrey),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(fromName.first().toString(), color = UIWhite, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Arrow
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(UIGrey.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Pays to",
                                                tint = UIBlack,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // To Avatar
                                        if (toContact != null) {
                                            ParticipantAvatarItemSmall(toContact)
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(UIAccentYellow),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(toName.first().toString(), color = UIBlack, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Right: Amount
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "Rp${String.format(Locale.getDefault(), "%,.0f", amount)}",
                                            style = AppFont.Bold,
                                            fontSize = 16.sp,
                                            color = UIBlack
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Consumption Details
                    Text(
                        text = "Consumption by User",
                        style = AppFont.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UIBackground)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        memberConsumption.forEach { (memberName, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val member = eventMembers.find { it.name == memberName }
                                    if (member != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(UIAccentYellow)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data("https://api.dicebear.com/9.x/avataaars/png?seed=${member.avatarName}")
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = member.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Text(
                                        text = memberName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = UIBlack
                                    )
                                }
                                Text(
                                    text = "Rp${String.format(Locale.getDefault(), "%,.0f", amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UIBlack
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Info Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UIBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ℹ️ Informasi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = UIBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Settlements menunjukkan siapa yang harus membayar ke siapa. Tap edit item untuk assign shared by.",
                            fontSize = 11.sp,
                            color = UIDarkGrey
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSummaryDialog = false
                        onContinue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UIAccentYellow,
                        contentColor = UIBlack
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Continue to Main", style = AppFont.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryDialog = false }) {
                    Text("Close", color = UIDarkGrey, style = AppFont.Medium)
                }
            },
            containerColor = UIWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    // Calculate tax percentage from tax amount for display
    val initialTaxPercentage = if (item.itemPrice * item.itemQuantity > 0) {
        (item.itemTax / (item.itemPrice * item.itemQuantity) * 100).toString()
    } else {
        "0"
    }
    var itemTaxPercentage by remember { mutableStateOf(initialTaxPercentage) }
    var selectedSharedBy by remember { mutableStateOf(item.sharedBy.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text("Edit Item", style = AppFont.SemiBold, fontSize = 18.sp, color = UIBlack) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Item Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ITEM NAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UIDarkGrey
                    )
                    TextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        placeholder = { Text("e.g. Nasi Goreng", color = UIGrey, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = UIAccentYellow,
                            unfocusedIndicatorColor = UIGrey.copy(alpha = 0.5f),
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price and Qty Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(
                            text = "PRICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = UIDarkGrey
                        )
                        TextField(
                            value = itemPrice,
                            onValueChange = { itemPrice = it },
                            placeholder = { Text("Rp 0", color = UIGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = UIAccentYellow,
                                unfocusedIndicatorColor = UIGrey.copy(alpha = 0.5f),
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "QTY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = UIDarkGrey
                        )
                        TextField(
                            value = itemQuantity,
                            onValueChange = { itemQuantity = it },
                            placeholder = { Text("1", color = UIGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = UIAccentYellow,
                                unfocusedIndicatorColor = UIGrey.copy(alpha = 0.5f),
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Discount
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DISCOUNT (RP)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UIDarkGrey
                    )
                    TextField(
                        value = itemDiscount,
                        onValueChange = { itemDiscount = it },
                        placeholder = { Text("0", color = UIGrey) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = UIAccentYellow,
                            unfocusedIndicatorColor = UIGrey.copy(alpha = 0.5f),
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Tax
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TAX (%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UIDarkGrey
                    )
                    TextField(
                        value = itemTaxPercentage,
                        onValueChange = { itemTaxPercentage = it },
                        placeholder = { Text("0", color = UIGrey) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = UIAccentYellow,
                            unfocusedIndicatorColor = UIGrey.copy(alpha = 0.5f),
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shared by:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = UIBlack)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // HORIZONTAL LAZY ROW FOR AVATARS - matching NewActivityScreen2
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(eventMembers) { member ->
                        val isSelected = selectedSharedBy.contains(member.name)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                selectedSharedBy = if (isSelected) {
                                    selectedSharedBy - member.name
                                } else {
                                    selectedSharedBy + member.name
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) UIAccentYellow else UIGrey.copy(alpha = 0.3f))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) UIAccentYellow else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarName = if (member.avatarName.isNotBlank()) member.avatarName else "User"
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("https://api.dicebear.com/9.x/avataaars/png?seed=$avatarName")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = member.name,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (!isSelected) Modifier.scale(0.85f)
                                            else Modifier
                                        ),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                    alpha = if (isSelected) 1f else 0.4f
                                )
                            }

                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member.name,
                                    fontSize = 12.sp,
                                    color = UIBlack,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Button
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE57373),
                        contentColor = UIWhite
                    )
                ) {
                    Text("Delete Item", style = AppFont.SemiBold, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = itemPrice.toDoubleOrNull() ?: 0.0
                    val quantity = itemQuantity.toIntOrNull() ?: 1
                    val taxPercentage = itemTaxPercentage.toDoubleOrNull() ?: 0.0
                    val discount = itemDiscount.toDoubleOrNull() ?: 0.0

                    // Calculate tax amount in Rupiah from percentage
                    val itemTotalPrice = price * quantity
                    val taxAmount = itemTotalPrice * taxPercentage / 100

                    val updatedItem = ParsedReceiptItem(
                        itemName = itemName,
                        itemPrice = price,
                        itemQuantity = quantity,
                        itemDiscount = discount,
                        itemTax = taxAmount,
                        sharedBy = selectedSharedBy.toList()
                    )
                    onSave(updatedItem)
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow, contentColor = UIBlack)
            ) {
                Text("Save", style = AppFont.SemiBold, fontSize = 16.sp)
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
fun ParsedReceiptItemRow(
    item: ParsedReceiptItem,
    eventMembers: List<Contact> = emptyList(),
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
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Display shared by avatars - matching NewActivityScreen2
                    if (item.sharedBy.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(item.sharedBy) { memberName ->
                                val participant = eventMembers.find { it.name == memberName }
                                if (participant != null) {
                                    MiniAvatarWithImage(participant)
                                } else {
                                    MiniAvatar()
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Tap to assign members",
                            color = UIDarkGrey,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
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

