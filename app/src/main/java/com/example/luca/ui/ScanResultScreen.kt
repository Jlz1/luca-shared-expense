package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.model.ParsedReceiptData
import com.example.luca.model.ParsedReceiptItem
import com.example.luca.ui.theme.*
import com.example.luca.utils.ReceiptParser

@Composable
fun ScanResultScreen(
    parsedData: ParsedReceiptData,
    onBackClick: () -> Unit,
    onScanAgain: () -> Unit
) {
    var receiptItems by remember { mutableStateOf(parsedData.items) }
    var globalTax by remember { mutableStateOf(parsedData.tax) }
    var globalDiscount by remember { mutableStateOf(parsedData.discount) }

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
                Spacer(modifier = Modifier.height(24.dp))

                // Instructions
                Text(
                    text = "Hasil OCR telah diproses. Berikut adalah item yang terdeteksi:",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Receipt Card
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Struk Belanja",
                                style = AppFont.SemiBold,
                                fontSize = 16.sp,
                                color = UIBlack
                            )
                            Text(
                                text = "Hasil Scan OCR",
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
                                    text = "Tap item untuk mengedit",
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

                        // Receipt Items Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(UIWhite)
                                .padding(16.dp)
                        ) {
                            if (receiptItems.isEmpty()) {
                                // Empty state
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tidak ada item terdeteksi",
                                        style = AppFont.SemiBold,
                                        fontSize = 16.sp,
                                        color = UIDarkGrey
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Coba scan ulang dengan foto yang lebih jelas",
                                        style = AppFont.Regular,
                                        fontSize = 12.sp,
                                        color = UIDarkGrey.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // Scrollable receipt items
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

                                // Scrolling indicator
                                if (receiptItems.size > 3) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Scroll untuk melihat semua item",
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Bill Section
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
                        Text(
                            text = "Rp${ReceiptParser.formatPrice(subtotal)}",
                            fontSize = 12.sp,
                            color = UIDarkGrey
                        )
                    }

                    if (globalTax > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Tax", fontSize = 12.sp, color = UIDarkGrey)
                            Text(
                                text = "Rp${ReceiptParser.formatPrice(globalTax)}",
                                fontSize = 12.sp,
                                color = UIDarkGrey
                            )
                        }
                    }

                    if (globalDiscount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Discount", fontSize = 12.sp, color = UIDarkGrey)
                            Text(
                                text = "-Rp${ReceiptParser.formatPrice(globalDiscount)}",
                                fontSize = 12.sp,
                                color = UIDarkGrey
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Bill",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = UIBlack
                        )
                        Text(
                            text = "Rp${ReceiptParser.formatPrice(totalBill)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = UIBlack
                        )
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

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Edit Item Dialog
    if (showEditItemDialog && editingItemIndex >= 0 && editingItemIndex < receiptItems.size) {
        EditParsedItemDialog(
            item = receiptItems[editingItemIndex],
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
fun ParsedReceiptItemRow(
    item: ParsedReceiptItem,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(UIBackground)
            .clickable(onClick = onItemClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
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

                if (item.itemDiscount > 0 || item.itemTax > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (item.itemDiscount > 0) append("Disc: Rp${ReceiptParser.formatPrice(item.itemDiscount)} ")
                            if (item.itemTax > 0) append("Tax: Rp${ReceiptParser.formatPrice(item.itemTax)}")
                        },
                        color = UIDarkGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Rp${ReceiptParser.formatPrice(item.itemPrice)}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditParsedItemDialog(
    item: ParsedReceiptItem,
    onDismiss: () -> Unit,
    onSave: (ParsedReceiptItem) -> Unit,
    onDelete: () -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var itemPrice by remember { mutableStateOf(item.itemPrice.toString()) }
    var itemQuantity by remember { mutableStateOf(item.itemQuantity.toString()) }
    var itemDiscount by remember { mutableStateOf(item.itemDiscount.toString()) }
    var itemTax by remember { mutableStateOf(item.itemTax.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Item",
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
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Price") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it.filter { char -> char.isDigit() } },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemDiscount,
                        onValueChange = { itemDiscount = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Discount") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemTax,
                        onValueChange = { itemTax = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Tax") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
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

