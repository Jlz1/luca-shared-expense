package com.example.luca

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color // Perlu import ini jika pakai Color.Transparent langsung
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen2() {
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
            // --- MAIN CONTENT (SCROLLABLE) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- BAGIAN ATAS: Participants & Equal Split ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Box Kiri: Participants List
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

                    // Box Kanan: Equal Split
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

                        // --- SWITCH FIX ---
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
                                // HILANGKAN BORDER:
                                uncheckedBorderColor = UITransparent,
                                checkedBorderColor = UITransparent
                            )
                        )
                        // ------------------
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- RECEIPT CARD ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(20.dp)
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

                    // Item 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Text(text = "1x   ", color = UIDarkGrey, fontSize = 14.sp)
                                Text(
                                    text = "Gurame Bakar Kecap",
                                    color = UIBlack,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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

                // --- TOTAL BILL CARD ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UIWhite)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Subtotal", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp120.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tax (10%)", fontSize = 12.sp, color = UIDarkGrey)
                        Text(text = "Rp12.000", fontSize = 12.sp, color = UIDarkGrey)
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                        Text(text = "Rp132.000", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            // --- FLOATING BUTTONS ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabCircleButton(size = 45.dp, icon = Icons.Default.Edit) { }
                FabCircleButton(size = 56.dp, icon = null) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_camera),
                        contentDescription = "Scan",
                        tint = UIBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- CONTINUE BUTTON ---
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

// ... Sisa komponen helper tetap sama ...
@Composable
fun FabCircleButton(size: androidx.compose.ui.unit.Dp, icon: androidx.compose.ui.graphics.vector.ImageVector?, content: @Composable (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(UIWhite)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = UIBlack, modifier = Modifier.size(20.dp))
        } else {
            content?.invoke()
        }
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

@Preview(showBackground = true)
@Composable
fun AddActivity2Preview() {
    LucaTheme {
        AddActivityScreen2()
    }
}