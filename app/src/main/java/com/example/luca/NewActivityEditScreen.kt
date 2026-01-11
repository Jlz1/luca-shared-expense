package com.example.luca

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityEditScreen() {
    // State for the editable title
    var activityTitle by remember { mutableStateOf("Dinner at Floating Resto") }

    Scaffold(
        containerColor = UIBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Activity",
                        style = AppFont.SemiBold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = UIBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UIBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = UIWhite
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Participants section and equal split toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Participants list box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(UIWhite)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Using helpers from NewActivityScreen2.kt
                                item { GreyAvatarItem("You") }
                                item { GreyAvatarItem("Jeremy E") }
                                item { GreyAvatarItem("Abel M") }
                            }
                        }
                        // Small edit icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            SmallEditCircle()
                        }
                    }

                    // Equal split toggle box
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
                            checked = false,
                            onCheckedChange = {},
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

                // Receipt card section (Wavy shape)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ReceiptWaveShape(waveWidth = 45.dp, waveHeight = 25.dp))
                        .background(UIWhite)
                        .padding(20.dp)
                ) {
                    // Editable Receipt Header
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
                            SmallEditCircle()
                        }
                        Text(
                            text = "Paid by Abel M",
                            style = AppFont.Regular,
                            fontSize = 12.sp,
                            color = UIDarkGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Editable Item List
                    // Only showing one item as requested
                    EditableItemRow(
                        initialQty = "1x",
                        initialName = "Gurame Bakar Kecap",
                        initialPrice = "Rp120.000"
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(thickness = 2.dp, color = UIGrey)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Bill section (Separate Card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(16.dp)
                ) {
                    // Subtotal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp120.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tax (Percentage editable, result disabled)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tax", fontSize = 12.sp, color = UIDarkGrey)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SmallInputBox(value = "10", onValueChange = {}, suffix = "%", width = 50.dp, enabled = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallInputBox(value = "Rp12.000", onValueChange = {}, suffix = "", width = 75.dp, enabled = false)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Global Discount (Both disabled/gray for now)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Discount", fontSize = 12.sp, color = UIDarkGrey)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SmallInputBox(value = "0", onValueChange = {}, suffix = "%", width = 50.dp, enabled = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallInputBox(value = "Rp0", onValueChange = {}, suffix = "", width = 75.dp, enabled = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        Text(text = "Rp132.000", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            // Floating Check/Save Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 34.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(59.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(UIAccentYellow)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = UIBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Component for a single editable item row
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
        // Quantity Box (Left side)
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

        // Middle Column (Name and Discount)
        // Wraps name and discount to allow full alignment control
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name Row with Price
            Row(modifier = Modifier.fillMaxWidth()) {
                // Name Input
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

                // Price Input (Fixed width on the right)
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

            // Discount Row
            // Uses SpaceBetween to push label left and inputs right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Discount Label
                Text(
                    text = "Discount",
                    fontSize = 12.sp,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(start = 8.dp) // Aligns with text in the box above
                )

                // Discount Inputs
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallInputBox(value = discPercent, onValueChange = { discPercent = it }, suffix = "%", width = 50.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallInputBox(value = discAmount, onValueChange = { discAmount = it }, suffix = "", width = 75.dp)
                }
            }
        }
    }
}

// Component for small numerical input boxes (used for tax, discount)
@Composable
fun SmallInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    width: androidx.compose.ui.unit.Dp,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 12.sp,
            color = if (enabled) UIBlack else UIDarkGrey, // Grey text when disabled
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
fun SmallEditCircle() {
    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(CircleShape)
            .background(UIAccentYellow),
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