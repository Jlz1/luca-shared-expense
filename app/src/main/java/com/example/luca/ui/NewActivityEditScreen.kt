package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityEditScreen(
    onBackClick: () -> Unit = {}
) {
    var activityTitle by remember { mutableStateOf("Dinner at Floating Resto") }
    var isEqualSplit by remember { mutableStateOf(false) }

    // State untuk Total Bill Card (Tax & Discount Global)
    var taxPercent by remember { mutableStateOf("10") }
    var taxAmount by remember { mutableStateOf("Rp12.000") }
    var globalDiscPercent by remember { mutableStateOf("0") }
    var globalDiscAmount by remember { mutableStateOf("Rp0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground) // Background Kuning
            .statusBarsPadding() // Hindari tabrakan dengan status bar
    ) {

        // 2. HEADER
        HeaderSection(
            currentState = HeaderState.EDIT_ACTIVITY, // Pastikan Enum ini ada di code kamu
            onLeftIconClick = onBackClick
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- 1. PARTICIPANTS & SPLIT ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Box Kiri
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(UIWhite)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                item { GreyAvatarItem("You") }
                                item { GreyAvatarItem("Jeremy E") }
                                item { GreyAvatarItem("Abel M") }
                            }
                        }
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                            SmallEditCircle(onClick = {})
                        }
                    }

                    // Box Kanan
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(UIWhite),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Equal Split", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        Spacer(modifier = Modifier.height(6.dp))
                        Switch(
                            checked = isEqualSplit,
                            onCheckedChange = { isEqualSplit = it },
                            modifier = Modifier.scale(1.2f).height(30.dp),
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

                // --- 2. RECEIPT CARD ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ReceiptWaveShape(waveWidth = 45.dp, waveHeight = 25.dp))
                        .background(UIWhite)
                        .padding(20.dp)
                ) {
                    // Header Editable
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = activityTitle,
                                onValueChange = { activityTitle = it },
                                textStyle = TextStyle(
                                    fontFamily = AppFont.SemiBold.fontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = UIBlack,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(UIAccentYellow),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallEditCircle(onClick = {})
                        }
                        Text("Paid by Abel M", style = AppFont.Regular, fontSize = 12.sp, color = UIDarkGrey)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ITEM LIST
                    EditableItemRow(initialQty = "1x", initialName = "Gurame Bakar Kecap", initialPrice = "Rp120.000")

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(thickness = 2.dp, color = UIGrey)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 3. TOTAL BILL CARD ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(16.dp)
                ) {
                    // Subtotal
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                        Text("Rp120.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(Modifier.height(8.dp))

                    // Tax (SEKARANG EDITABLE)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tax", fontSize = 12.sp, color = UIDarkGrey)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // enabled = true (default)
                            SmallInputBox(value = taxPercent, onValueChange = { taxPercent = it }, suffix = "%", width = 50.dp)
                            Spacer(Modifier.width(8.dp))
                            SmallInputBox(value = taxAmount, onValueChange = { taxAmount = it }, suffix = "", width = 75.dp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Global Discount (SEKARANG EDITABLE)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Discount", fontSize = 12.sp, color = UIDarkGrey)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // enabled = true (default)
                            SmallInputBox(value = globalDiscPercent, onValueChange = { globalDiscPercent = it }, suffix = "%", width = 50.dp)
                            Spacer(Modifier.width(8.dp))
                            SmallInputBox(value = globalDiscAmount, onValueChange = { globalDiscAmount = it }, suffix = "", width = 75.dp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Total Result
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Total Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        Text("Rp132.000", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            // Floating Check Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 34.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(59.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check_edit_activity),
                        contentDescription = "Check Edit Activity",
                    )
                }

            }
        }
    }
}

// --- EDITABLE ITEM ROW (Border 1dp) ---
@Composable
fun EditableItemRow(initialQty: String, initialName: String, initialPrice: String) {
    var qty by remember { mutableStateOf(initialQty) }
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }

    var discPercent by remember { mutableStateOf("0") }
    var discAmount by remember { mutableStateOf("Rp0") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 1. QTY BOX
        Box(
            modifier = Modifier
                .border(1.dp, UIDarkGrey, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = qty,
                onValueChange = { qty = it },
                textStyle = TextStyle(color = UIDarkGrey, fontSize = 12.sp, textAlign = TextAlign.Center),
                modifier = Modifier.width(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 2. MIDDLE COLUMN (Nama & Diskon)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Baris 1: Nama Makanan + Harga
            Row(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = TextStyle(color = UIBlack, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, UIDarkGrey, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = price,
                    onValueChange = { price = it },
                    textStyle = TextStyle(color = UIBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End),
                    modifier = Modifier
                        .width(95.dp)
                        .border(1.dp, UIDarkGrey, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baris 2: Discount Label + Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label Discount (Rata Kiri)
                Text(
                    text = "Discount",
                    fontSize = 12.sp,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(start = 8.dp)
                )

                // Inputs (Rata Kanan) - INI JUGA SAYA BUAT DISABLED UNTUK ITEM (Sesuai request terakhir)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallInputBox(value = discPercent, onValueChange = { discPercent = it }, suffix = "%", width = 50.dp, enabled = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallInputBox(value = discAmount, onValueChange = { discAmount = it }, suffix = "", width = 75.dp, enabled = true)
                }
            }
        }
    }
}

// --- SMALL INPUT BOX (Border 1dp) ---
@Composable
fun SmallInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    width: Dp,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 12.sp,
            color = if (enabled) UIBlack else UIDarkGrey, // Hitam jika aktif, Abu jika mati
            textAlign = TextAlign.End
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(28.dp)
                    .border(1.dp, UIDarkGrey, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        innerTextField()
                    }
                    if (suffix.isNotEmpty()) {
                        Text(suffix, fontSize = 12.sp, color = UIDarkGrey)
                    }
                }
            }
        }
    )
}

@Composable
fun SmallEditCircle(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(CircleShape)
            .background(UIAccentYellow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = UIBlack,
            modifier = Modifier.size(10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreview() {
    LucaTheme {
        NewActivityEditScreen()
    }
}