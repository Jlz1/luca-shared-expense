package com.example.luca

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedActivityScreen() {
    // State agar switch bisa berubah warna saat diklik
    var isSplitEqual by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = UIBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Activity Details",
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

        // Root box with yellow background to fill the gap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UIAccentYellow)
        ) {
            // Main content container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Padding top creates the yellow gap effect
                    .padding(top = 16.dp)
                    // Rounded top corners for the content area
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(UIBackground)
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
                            item { GreyAvatarItem("You") }
                            item { GreyAvatarItem("Jeremy E") }
                            item { GreyAvatarItem("Abel M") }
                        }
                    }

                    // Equal split toggle
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

                        // Switch logic fixed: Now interactive
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

                // --- 2. RECEIPT CARD ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ReceiptWaveShape(waveWidth = 45.dp, waveHeight = 25.dp))
                        .background(UIWhite)
                        .padding(20.dp)
                ) {
                    // Receipt header
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

                    // Receipt item
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "1x",
                            color = UIDarkGrey,
                            fontSize = 14.sp,
                            modifier = Modifier.width(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gurame Bakar Kecap",
                                color = UIBlack,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                item { MiniAvatar() }
                                item { MiniAvatar() }
                                item { MiniAvatar() }
                            }
                        }

                        Text(
                            text = "Rp120.000",
                            color = UIBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp120.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tax (10%)", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp12.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        Text(text = "Rp132.000", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Floating action buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabCircleButton(size = 45.dp) {
                    Icon(Icons.Default.Edit, null, tint = UIBlack)
                }

                FabCircleButton(size = 56.dp) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scan_button),
                        contentDescription = "Scan",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailPreview() {
    LucaTheme {
        DetailedActivityScreen()
    }
}